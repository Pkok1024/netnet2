package com.onlasdan.netnet.monitor

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import com.onlasdan.netnet.model.DetailedNetworkDiagnostics
import com.onlasdan.netnet.model.NetworkInterfaceItem
import com.onlasdan.netnet.model.NetworkType
import java.net.Inet4Address
import java.net.Inet6Address
import java.net.NetworkInterface
import java.util.Locale

object NetworkDiagnosticsHelper {

    fun getDiagnostics(context: Context): DetailedNetworkDiagnostics {
        return try {
            safeGetDiagnostics(context)
        } catch (_: Throwable) {
            DetailedNetworkDiagnostics(
                connectionStatus = "Available",
                networkType = NetworkType.WIFI,
                networkName = "Network Active"
            )
        }
    }

    private fun safeGetDiagnostics(context: Context): DetailedNetworkDiagnostics {
        val connectivityManager = try {
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        } catch (_: Throwable) { null } ?: return DetailedNetworkDiagnostics(connectionStatus = "Unavailable")

        val activeNetwork = try { connectivityManager.activeNetwork } catch (_: Throwable) { null }
        val caps = activeNetwork?.let { 
            try { connectivityManager.getNetworkCapabilities(it) } catch (_: Throwable) { null }
        }
        val linkProps = activeNetwork?.let { 
            try { connectivityManager.getLinkProperties(it) } catch (_: Throwable) { null }
        }

        val activeIfName = linkProps?.interfaceName ?: "Unknown"

        var networkType = NetworkType.OFFLINE
        var networkName = "No Active Connection"
        var isVpn = false
        var isValidated = false
        var isMetered = try { connectivityManager.isActiveNetworkMetered } catch (_: Throwable) { false }
        var downBandwidthMbps = 0
        var upBandwidthMbps = 0
        var signalDbm: Int? = null
        var signalPercent: Int? = null

        var wifiFreq: Int? = null
        var wifiBand: String? = null
        var wifiStandard: String? = null

        var cellularGen: String? = null
        var cellularCarrier: String? = null

        var connectionStatus = "Disconnected"

        if (caps != null && activeNetwork != null) {
            try {
                isValidated = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                val hasInternet = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                isVpn = caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
                downBandwidthMbps = (caps.linkDownstreamBandwidthKbps / 1000).coerceAtLeast(0)
                upBandwidthMbps = (caps.linkUpstreamBandwidthKbps / 1000).coerceAtLeast(0)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    try {
                        val dbm = caps.signalStrength
                        if (dbm != NetworkCapabilities.SIGNAL_STRENGTH_UNSPECIFIED) {
                            signalDbm = dbm
                            signalPercent = (((dbm + 100) / 50f) * 100).toInt().coerceIn(0, 100)
                        }
                    } catch (_: Throwable) {}
                }

                connectionStatus = when {
                    isValidated -> "Connected & Validated (Internet Active)"
                    hasInternet -> "Connected (Validating Internet...)"
                    else -> "Local Connection Only (No Internet)"
                }

                if (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    networkType = NetworkType.WIFI
                    try {
                        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
                        val info: WifiInfo? = try { wifiManager?.connectionInfo } catch (_: Throwable) { null }
                        val rawSsid = info?.ssid?.replace("\"", "").orEmpty()
                        networkName = if (rawSsid.isNotEmpty() && rawSsid != "<unknown ssid>") rawSsid else "Wi-Fi Network"

                        if (info != null) {
                            val freq = try { info.frequency } catch (_: Throwable) { 0 }
                            if (freq > 0) {
                                wifiFreq = freq
                                wifiBand = when {
                                    freq in 2400..2500 -> "2.4 GHz (Standard Range)"
                                    freq in 4900..5900 -> "5 GHz (High Throughput)"
                                    freq >= 5925 -> "6 GHz (Wi-Fi 6E/7 Ultra-Wide)"
                                    else -> "$freq MHz"
                                }
                            }

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                wifiStandard = try {
                                    when (info.wifiStandard) {
                                        8 -> "Wi-Fi 7 (802.11be)"
                                        6 -> "Wi-Fi 6 (802.11ax)"
                                        5 -> "Wi-Fi 5 (802.11ac)"
                                        4 -> "Wi-Fi 4 (802.11n)"
                                        3 -> "802.11g"
                                        1 -> "802.11a"
                                        2 -> "802.11b"
                                        else -> if (freq > 4900) "Wi-Fi 5/6 (5GHz)" else "Wi-Fi 4 (2.4GHz)"
                                    }
                                } catch (_: Throwable) {
                                    if (freq > 4900) "Wi-Fi 5/6 (5GHz)" else "Wi-Fi 4 (2.4GHz)"
                                }
                            } else {
                                wifiStandard = if (freq > 4900) "802.11ac (5GHz)" else "802.11n (2.4GHz)"
                            }

                            val rssi = try { info.rssi } catch (_: Throwable) { -60 }
                            if (rssi in -120..0) {
                                signalDbm = rssi
                                signalPercent = (((rssi + 100) / 50f) * 100).toInt().coerceIn(0, 100)
                            }
                        }
                    } catch (_: Throwable) {
                        networkName = "Wi-Fi Network"
                    }
                } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    networkType = NetworkType.CELLULAR
                    networkName = "Cellular Mobile Network"

                    try {
                        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
                        val opName = try {
                            telephonyManager?.networkOperatorName?.ifEmpty {
                                telephonyManager.simOperatorName
                            }
                        } catch (_: Throwable) { null }
                        cellularCarrier = if (!opName.isNullOrEmpty()) opName else "Mobile Carrier"
                        networkName = "$cellularCarrier (Mobile Data)"
                    } catch (_: Throwable) {
                        networkName = "Cellular (Mobile Data)"
                    }

                    cellularGen = when {
                        downBandwidthMbps >= 100 -> "5G NR (High Speed Sub-6/mmWave)"
                        downBandwidthMbps >= 25 -> "4G LTE-Advanced"
                        downBandwidthMbps >= 8 -> "4G LTE"
                        downBandwidthMbps >= 1 -> "3G HSPA+"
                        else -> "Mobile Cellular"
                    }
                } else if (caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    networkType = NetworkType.ETHERNET
                    networkName = "Ethernet (LAN)"
                } else if (isVpn) {
                    networkType = NetworkType.VPN
                    networkName = "Encrypted VPN Tunnel"
                }
            } catch (_: Throwable) {}
        }

        // Extract IP Addresses, Gateway, DNS from LinkProperties
        var ipv4Addr = "127.0.0.1"
        var ipv6Addr: String? = null
        var gateway: String? = null
        val dnsList = mutableListOf<String>()

        try {
            linkProps?.linkAddresses?.forEach { linkAddr ->
                val addr = linkAddr.address
                if (addr is Inet4Address && !addr.isLoopbackAddress) {
                    ipv4Addr = addr.hostAddress ?: ipv4Addr
                } else if (addr is Inet6Address && !addr.isLoopbackAddress && !addr.isLinkLocalAddress) {
                    if (ipv6Addr == null) {
                        ipv6Addr = addr.hostAddress
                    }
                }
            }

            linkProps?.routes?.forEach { route ->
                val gw = route.gateway
                if (gw != null && !gw.isAnyLocalAddress && route.isDefaultRoute) {
                    gateway = gw.hostAddress
                }
            }

            linkProps?.dnsServers?.forEach { dns ->
                dns.hostAddress?.let { dnsList.add(it) }
            }
        } catch (_: Throwable) {}

        // Enumerate all system interfaces (wlan0, rmnet, etc.)
        val ifItems = mutableListOf<NetworkInterfaceItem>()
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            if (interfaces != null) {
                while (interfaces.hasMoreElements()) {
                    val intf = interfaces.nextElement()
                    val name = intf.name
                    val isLoop = try { intf.isLoopback } catch (_: Throwable) { false }
                    val isUp = try { intf.isUp } catch (_: Throwable) { true }

                    val ifType = when {
                        name.startsWith("wlan") || name.startsWith("swlan") || name.startsWith("wl") -> NetworkType.WIFI
                        name.startsWith("rmnet") || name.startsWith("ccmni") || name.startsWith("pdp") || name.startsWith("wwan") -> NetworkType.CELLULAR
                        name.startsWith("eth") || name.startsWith("en") -> NetworkType.ETHERNET
                        name.startsWith("tun") || name.startsWith("tap") || name.startsWith("ipsec") || name.startsWith("ppp") -> NetworkType.VPN
                        isLoop -> NetworkType.OFFLINE
                        else -> NetworkType.OFFLINE
                    }

                    val isVirt = isVirtualIf(name)
                    val isActiveDef = name == activeIfName

                    val ips = mutableListOf<String>()
                    try {
                        val addrs = intf.inetAddresses
                        while (addrs.hasMoreElements()) {
                            val a = addrs.nextElement()
                            if (!a.isLoopbackAddress) {
                                a.hostAddress?.let { ips.add(it.substringBefore("%")) }
                            }
                        }
                    } catch (_: Throwable) {}

                    val dispName = when (ifType) {
                        NetworkType.WIFI -> "Wi-Fi ($name)"
                        NetworkType.CELLULAR -> "Cellular ($name)"
                        NetworkType.ETHERNET -> "Ethernet ($name)"
                        NetworkType.VPN -> "VPN Tunnel ($name)"
                        else -> if (isLoop) "Loopback ($name)" else "Interface ($name)"
                    }

                    ifItems.add(
                        NetworkInterfaceItem(
                            name = name,
                            displayName = dispName,
                            type = ifType,
                            isUp = isUp,
                            isLoopback = isLoop,
                            isVirtual = isVirt,
                            isActiveDefault = isActiveDef,
                            ipAddresses = ips,
                            mtu = try { intf.mtu } catch (_: Throwable) { 1500 }
                        )
                    )
                }
            }
        } catch (_: Throwable) {}

        // Sort interfaces: Active Default first, then Up interfaces, then Wi-Fi/Cellular, then others
        val sortedInterfaces = ifItems.sortedWith(
            compareByDescending<NetworkInterfaceItem> { it.isActiveDefault }
                .thenByDescending { it.isUp && !it.isLoopback }
                .thenBy { it.isVirtual }
                .thenBy { it.name }
        )

        return DetailedNetworkDiagnostics(
            activeInterfaceName = activeIfName,
            networkType = networkType,
            networkName = networkName,
            connectionStatus = connectionStatus,
            isValidated = isValidated,
            isMetered = isMetered,
            isVpn = isVpn,
            downstreamBandwidthMbps = downBandwidthMbps,
            upstreamBandwidthMbps = upBandwidthMbps,
            signalStrengthDbm = signalDbm,
            signalLevelPercent = signalPercent,
            wifiFrequencyMhz = wifiFreq,
            wifiBand = wifiBand,
            wifiStandard = wifiStandard,
            cellularGeneration = cellularGen,
            cellularOperatorName = cellularCarrier,
            ipv4Address = ipv4Addr,
            ipv6Address = ipv6Addr,
            gatewayAddress = gateway,
            dnsServers = dnsList.distinct(),
            interfaceList = sortedInterfaces
        )
    }

    private fun isVirtualIf(name: String): Boolean {
        val lower = name.lowercase(Locale.US)
        return lower.startsWith("tun") ||
                lower.startsWith("tap") ||
                lower.startsWith("p2p") ||
                lower.startsWith("dummy") ||
                lower.startsWith("lo") ||
                lower.startsWith("sit") ||
                lower.startsWith("ipsec") ||
                lower.startsWith("ifb") ||
                lower.startsWith("ppp") ||
                lower.startsWith("vbox") ||
                lower.startsWith("swlan") ||
                lower.contains("vpn")
    }
}
