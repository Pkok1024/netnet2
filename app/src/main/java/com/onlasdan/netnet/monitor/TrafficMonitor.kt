package com.onlasdan.netnet.monitor

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.TrafficStats
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import com.onlasdan.netnet.data.SpeedSettingsRepository
import com.onlasdan.netnet.model.NetworkType
import com.onlasdan.netnet.model.SpeedPoint
import com.onlasdan.netnet.model.SpeedSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.Socket
import kotlin.math.max

class TrafficMonitor(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val settingsRepo = SpeedSettingsRepository.getInstance(context)

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    private val wifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

    private var monitorJob: Job? = null
    private var pingJob: Job? = null

    private var lastRxBytes: Long = 0L
    private var lastTxBytes: Long = 0L
    private var lastTimestamp: Long = 0L

    private var sessionRxAccumulator: Long = 0L
    private var sessionTxAccumulator: Long = 0L

    private var peakRxSpeed: Long = 0L
    private var peakTxSpeed: Long = 0L

    private val historyPoints = ArrayDeque<SpeedPoint>(60)

    private val _snapshot = MutableStateFlow(SpeedSnapshot())
    val snapshot: StateFlow<SpeedSnapshot> = _snapshot.asStateFlow()

    private val _history = MutableStateFlow<List<SpeedPoint>>(emptyList())
    val history: StateFlow<List<SpeedPoint>> = _history.asStateFlow()

    private var currentPingMs: Long = -1L
    private var isScreenOn: Boolean = true
    private var isAppInForeground: Boolean = false
    private var currentIntervalMs: Long = 1000L

    @Volatile private var cachedNetworkDetails: Triple<NetworkType, String, Int> = Triple(NetworkType.OFFLINE, "Checking...", 0)
    @Volatile private var cachedIpAddress: String = "127.0.0.1"

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            updateNetworkInfo()
        }

        override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
            updateNetworkInfo()
        }

        override fun onLost(network: Network) {
            updateNetworkInfo()
        }
    }

    init {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                connectivityManager?.registerDefaultNetworkCallback(networkCallback)
            } else {
                val request = NetworkRequest.Builder().build()
                connectivityManager?.registerNetworkCallback(request, networkCallback)
            }
        } catch (_: Throwable) {}

        scope.launch {
            try {
                updateNetworkInfo()
            } catch (_: Throwable) {}
        }

        scope.launch {
            try {
                var lastSaverMode = settingsRepo.settings.value.isBatterySaverMode
                settingsRepo.settings.collect { settings ->
                    val intervalChanged = currentIntervalMs != settings.updateIntervalMs
                    val saverChanged = lastSaverMode != settings.isBatterySaverMode
                    if (intervalChanged || saverChanged) {
                        currentIntervalMs = settings.updateIntervalMs
                        lastSaverMode = settings.isBatterySaverMode
                        if (monitorJob?.isActive == true) {
                            restartMonitorJob()
                        }
                        updatePingProbeState()
                    }
                }
            } catch (_: Throwable) {}
        }
    }

    fun start(intervalMs: Long = 1000L) {
        currentIntervalMs = intervalMs
        if (monitorJob?.isActive == true) return

        val (initialRx, initialTx) = getEffectiveRxTxBytes(isVpnActive())
        lastRxBytes = initialRx
        lastTxBytes = initialTx
        lastTimestamp = System.currentTimeMillis()

        restartMonitorJob()
        updatePingProbeState()
    }

    fun stop() {
        monitorJob?.cancel()
        monitorJob = null
        pingJob?.cancel()
        pingJob = null
    }

    /**
     * Battery Saver: Notify monitor of screen state changes.
     * When screen is off, slows down traffic sampling and stops ping probing to save battery.
     */
    fun setScreenState(screenOn: Boolean) {
        if (isScreenOn == screenOn) return
        isScreenOn = screenOn

        if (screenOn) {
            // Screen turned back ON: take a fresh sample immediately and resume normal interval
            sampleTraffic()
            restartMonitorJob()
            updatePingProbeState()
        } else {
            // Screen turned OFF: stop ping probe and switch monitor to low-frequency battery-saver interval
            pingJob?.cancel()
            pingJob = null
            restartMonitorJob()
        }
    }

    /**
     * In-App UI state: ping probe is only active when the app UI is open or foreground.
     */
    fun setAppForeground(inForeground: Boolean) {
        isAppInForeground = inForeground
        updatePingProbeState()
    }

    private fun restartMonitorJob() {
        monitorJob?.cancel()
        monitorJob = scope.launch {
            while (isActive) {
                sampleTraffic()
                // If screen is OFF, poll only every 10 seconds to conserve battery while tracking daily usage.
                // If Battery Saver is active, reduce polling frequency to at least 3.0 seconds (3000ms).
                val isBatterySaver = settingsRepo.settings.value.isBatterySaverMode
                val delayMs = when {
                    !isScreenOn -> 10000L
                    isBatterySaver -> max(currentIntervalMs, 3000L)
                    else -> currentIntervalMs
                }
                delay(delayMs)
            }
        }
    }

    fun resetSession() {
        sessionRxAccumulator = 0L
        sessionTxAccumulator = 0L
        peakRxSpeed = 0L
        peakTxSpeed = 0L
        historyPoints.clear()
        _history.value = emptyList()
        sampleTraffic()
    }

    /**
     * Reads effective network traffic with automatic VPN deduplication.
     * When a VPN is active, Android creates a virtual 'tun' interface which causes TrafficStats.getTotalRxBytes()
     * to double-count traffic (1x on tun0 + 1x on wlan0/rmnet).
     * We filter out virtual interfaces and calculate exclusively on physical network interfaces.
     */
    private fun getEffectiveRxTxBytes(isVpn: Boolean): Pair<Long, Long> {
        if (!isVpn) {
            val rx = TrafficStats.getTotalRxBytes()
            val tx = TrafficStats.getTotalTxBytes()
            if (rx != TrafficStats.UNSUPPORTED.toLong() && tx != TrafficStats.UNSUPPORTED.toLong()) {
                return Pair(rx, tx)
            }
        }

        // When VPN is active, sum bytes across physical non-virtual network interfaces
        try {
            var physicalRx = 0L
            var physicalTx = 0L
            var hasPhysicalStats = false

            val interfaces = NetworkInterface.getNetworkInterfaces()
            if (interfaces != null) {
                while (interfaces.hasMoreElements()) {
                    val intf = interfaces.nextElement()
                    val name = intf.name.lowercase()

                    if (intf.isLoopback || !intf.isUp) continue
                    if (isVirtualInterface(name)) continue

                    val rx = TrafficStats.getRxBytes(intf.name)
                    val tx = TrafficStats.getTxBytes(intf.name)

                    if (rx > 0 && rx != TrafficStats.UNSUPPORTED.toLong()) {
                        physicalRx += rx
                        hasPhysicalStats = true
                    }
                    if (tx > 0 && tx != TrafficStats.UNSUPPORTED.toLong()) {
                        physicalTx += tx
                        hasPhysicalStats = true
                    }
                }
            }

            if (hasPhysicalStats) {
                return Pair(physicalRx, physicalTx)
            }
        } catch (_: Exception) {}

        // Fallback calculation if interface enumeration is restricted:
        val rawRx = TrafficStats.getTotalRxBytes()
        val rawTx = TrafficStats.getTotalTxBytes()
        return if (isVpn && rawRx > 0) {
            Pair(rawRx / 2, rawTx / 2)
        } else {
            Pair(rawRx, rawTx)
        }
    }

    private fun isVirtualInterface(name: String): Boolean {
        return name.startsWith("tun") ||
                name.startsWith("tap") ||
                name.startsWith("p2p") ||
                name.startsWith("dummy") ||
                name.startsWith("lo") ||
                name.startsWith("sit") ||
                name.startsWith("ipsec") ||
                name.startsWith("ifb") ||
                name.startsWith("ppp") ||
                name.startsWith("vbox") ||
                name.startsWith("swlan") ||
                name.contains("vpn")
    }

    private fun isVpnActive(): Boolean {
        return try {
            val cm = connectivityManager ?: return false
            val activeNetwork = cm.activeNetwork ?: return false
            val caps = cm.getNetworkCapabilities(activeNetwork) ?: return false
            caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
        } catch (_: Throwable) {
            false
        }
    }

    private fun sampleTraffic() {
        val isVpn = isVpnActive()
        val (currentRx, currentTx) = getEffectiveRxTxBytes(isVpn)
        val now = System.currentTimeMillis()

        val timeDiffSec = if (lastTimestamp > 0 && now > lastTimestamp) {
            (now - lastTimestamp) / 1000.0
        } else {
            1.0
        }

        var rxSpeed = 0L
        var txSpeed = 0L

        val netInfo = cachedNetworkDetails

        if (lastRxBytes > 0 && currentRx >= lastRxBytes) {
            val deltaRx = currentRx - lastRxBytes
            rxSpeed = (deltaRx / max(timeDiffSec, 0.1)).toLong()
            sessionRxAccumulator += deltaRx
            settingsRepo.recordUsageDelta(netInfo.first, deltaRx, 0L)
        }

        if (lastTxBytes > 0 && currentTx >= lastTxBytes) {
            val deltaTx = currentTx - lastTxBytes
            txSpeed = (deltaTx / max(timeDiffSec, 0.1)).toLong()
            sessionTxAccumulator += deltaTx
            settingsRepo.recordUsageDelta(netInfo.first, 0L, deltaTx)
        }

        lastRxBytes = currentRx
        lastTxBytes = currentTx
        lastTimestamp = now

        peakRxSpeed = max(peakRxSpeed, rxSpeed)
        peakTxSpeed = max(peakTxSpeed, txSpeed)

        val (todayRx, todayTx) = settingsRepo.getTodayUsage()

        // Only update waveform history points when screen is active to save memory & allocations
        if (isScreenOn) {
            val point = SpeedPoint(now, rxSpeed, txSpeed)
            if (historyPoints.size >= 60) {
                historyPoints.removeFirst()
            }
            historyPoints.addLast(point)
            _history.value = historyPoints.toList()
        }

        _snapshot.value = SpeedSnapshot(
            downloadBytesPerSec = rxSpeed,
            uploadBytesPerSec = txSpeed,
            peakDownloadBytesPerSec = peakRxSpeed,
            peakUploadBytesPerSec = peakTxSpeed,
            sessionRxBytes = sessionRxAccumulator,
            sessionTxBytes = sessionTxAccumulator,
            todayRxBytes = todayRx,
            todayTxBytes = todayTx,
            totalDeviceRxBytes = currentRx,
            totalDeviceTxBytes = currentTx,
            networkType = netInfo.first,
            networkName = netInfo.second,
            ipAddress = cachedIpAddress,
            linkSpeedMbps = netInfo.third,
            pingMs = currentPingMs,
            timestamp = now
        )
    }

    private fun updatePingProbeState() {
        pingJob?.cancel()
        pingJob = null

        val isBatterySaver = settingsRepo.settings.value.isBatterySaverMode
        // Ping only runs if screen is ON, app UI is in foreground, and Battery Saver is OFF to prevent battery drain
        if (isScreenOn && isAppInForeground && !isBatterySaver) {
            pingJob = scope.launch {
                while (isActive) {
                    currentPingMs = measurePingLatency()
                    delay(5000L) // Relaxed 5-second interval
                }
            }
        } else {
            currentPingMs = -1L
        }
    }

    private suspend fun measurePingLatency(): Long = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress("8.8.8.8", 53), 1200)
                System.currentTimeMillis() - start
            }
        } catch (_: Exception) {
            try {
                Socket().use { socket ->
                    socket.connect(InetSocketAddress("1.1.1.1", 53), 1200)
                    System.currentTimeMillis() - start
                }
            } catch (_: Exception) {
                -1L
            }
        }
    }

    private fun updateNetworkInfo() {
        val netInfo = getNetworkDetails()
        val ip = getLocalIpAddress()
        cachedNetworkDetails = netInfo
        cachedIpAddress = ip
        val current = _snapshot.value
        _snapshot.value = current.copy(
            networkType = netInfo.first,
            networkName = netInfo.second,
            linkSpeedMbps = netInfo.third,
            ipAddress = ip
        )
    }

    private fun getNetworkDetails(): Triple<NetworkType, String, Int> {
        val cm = connectivityManager ?: return Triple(NetworkType.OFFLINE, "No Network", 0)
        val activeNetwork = cm.activeNetwork ?: return Triple(NetworkType.OFFLINE, "No Network", 0)
        val caps = cm.getNetworkCapabilities(activeNetwork) ?: return Triple(NetworkType.OFFLINE, "Disconnected", 0)

        return when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> {
                Triple(NetworkType.VPN, "VPN Active", caps.linkDownstreamBandwidthKbps / 1000)
            }
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                val ssid = try {
                    val info: WifiInfo? = wifiManager?.connectionInfo
                    val name = info?.ssid?.replace("\"", "").orEmpty()
                    if (name.isNotEmpty() && name != "<unknown ssid>") name else "Wi-Fi"
                } catch (_: Exception) {
                    "Wi-Fi"
                }
                val speed = caps.linkDownstreamBandwidthKbps / 1000
                Triple(NetworkType.WIFI, ssid, speed)
            }
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                val speed = caps.linkDownstreamBandwidthKbps / 1000
                Triple(NetworkType.CELLULAR, "Cellular (Mobile Data)", speed)
            }
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                val speed = caps.linkDownstreamBandwidthKbps / 1000
                Triple(NetworkType.ETHERNET, "Ethernet", speed)
            }
            else -> Triple(NetworkType.OFFLINE, "Unknown Network", 0)
        }
    }

    private fun getLocalIpAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces() ?: return "Unavailable"
            while (interfaces.hasMoreElements()) {
                val intf = interfaces.nextElement()
                if (intf.isLoopback || !intf.isUp) continue
                val addresses = intf.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress ?: "127.0.0.1"
                    }
                }
            }
        } catch (_: Exception) {}
        return "127.0.0.1"
    }

    companion object {
        @Volatile
        private var INSTANCE: TrafficMonitor? = null

        fun getInstance(context: Context): TrafficMonitor {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TrafficMonitor(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
