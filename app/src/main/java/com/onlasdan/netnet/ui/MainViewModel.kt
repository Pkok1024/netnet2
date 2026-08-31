package com.onlasdan.netnet.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.onlasdan.netnet.data.SpeedSettings
import com.onlasdan.netnet.data.SpeedSettingsRepository
import com.onlasdan.netnet.model.SpeedPoint
import com.onlasdan.netnet.model.SpeedSnapshot
import com.onlasdan.netnet.monitor.TrafficMonitor
import com.onlasdan.netnet.notification.NotificationHelper
import com.onlasdan.netnet.service.FloatingBubbleService
import com.onlasdan.netnet.service.SpeedTileService
import com.onlasdan.netnet.service.NetSpeedForegroundService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import com.onlasdan.netnet.model.ProcessResourceUsage
import com.onlasdan.netnet.model.DetailedNetworkDiagnostics
import com.onlasdan.netnet.monitor.ProcessDiagnosticsHelper
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val context: Context = application.applicationContext
    private val trafficMonitor = TrafficMonitor.getInstance(context)
    private val settingsRepo = SpeedSettingsRepository.getInstance(context)

    val snapshot: StateFlow<SpeedSnapshot> = trafficMonitor.snapshot
    val history: StateFlow<List<SpeedPoint>> = trafficMonitor.history
    val settings: StateFlow<SpeedSettings> = settingsRepo.settings

    val isServiceRunning: StateFlow<Boolean> = NetSpeedForegroundService.isRunning
    val isPaused: StateFlow<Boolean> = NetSpeedForegroundService.isPausedState
    val isFloatingBubbleActive: StateFlow<Boolean> = FloatingBubbleService.isFloatingActive

    private val _processUsage = MutableStateFlow(ProcessResourceUsage())
    val processUsage: StateFlow<ProcessResourceUsage> = _processUsage.asStateFlow()

    private val _networkDiagnostics = MutableStateFlow(DetailedNetworkDiagnostics())
    val networkDiagnostics: StateFlow<DetailedNetworkDiagnostics> = _networkDiagnostics.asStateFlow()

    private val _pingDiagnosticState = MutableStateFlow(com.onlasdan.netnet.model.PingDiagnosticState())
    val pingDiagnosticState: StateFlow<com.onlasdan.netnet.model.PingDiagnosticState> = _pingDiagnosticState.asStateFlow()

    private val _oneTimeDiagnosticState = MutableStateFlow(com.onlasdan.netnet.model.OneTimeDiagnosticState())
    val oneTimeDiagnosticState: StateFlow<com.onlasdan.netnet.model.OneTimeDiagnosticState> = _oneTimeDiagnosticState.asStateFlow()

    private val _isTestingSpeed = MutableStateFlow(false)
    val isTestingSpeed: StateFlow<Boolean> = _isTestingSpeed.asStateFlow()

    private var speedTestJob: Job? = null
    private var pingDiagnosticJob: Job? = null
    private var oneTimeDiagnosticJob: Job? = null

    init {
        // Start periodic process and network diagnostics sampler (every 2 seconds)
        viewModelScope.launch(Dispatchers.Default) {
            while (isActive) {
                try {
                    val usage = ProcessDiagnosticsHelper.sampleProcessUsage(context, isServiceRunning.value)
                    val netDiag = com.onlasdan.netnet.monitor.NetworkDiagnosticsHelper.getDiagnostics(context)
                    _processUsage.value = usage
                    _networkDiagnostics.value = netDiag
                } catch (_: Throwable) {}
                delay(2000L)
            }
        }
    }

    fun ensureServiceStarted() {
        try {
            if (settings.value.isServiceEnabled && !isServiceRunning.value) {
                NetSpeedForegroundService.startService(context)
            }
        } catch (_: Throwable) {}
    }

    fun toggleService() {
        if (isServiceRunning.value) {
            NetSpeedForegroundService.stopService(context)
            settingsRepo.updateSettings { it.copy(isServiceEnabled = false) }
        } else {
            NetSpeedForegroundService.startService(context)
            settingsRepo.updateSettings { it.copy(isServiceEnabled = true) }
        }
    }

    fun togglePause() {
        NetSpeedForegroundService.togglePause(context, isPaused.value)
    }

    fun toggleFloatingBubble() {
        FloatingBubbleService.toggle(context)
    }

    fun resetSession() {
        trafficMonitor.resetSession()
        NetSpeedForegroundService.resetSession(context)
    }

    fun resetTodayUsage() {
        settingsRepo.resetTodayUsage()
    }

    fun updateSettings(transform: (SpeedSettings) -> SpeedSettings) {
        settingsRepo.updateSettings(transform)
    }

    fun canPostPromoted(): Boolean {
        return NotificationHelper.canPostPromotedNotifications(context)
    }

    fun runPingDiagnostic() {
        if (_pingDiagnosticState.value.status == com.onlasdan.netnet.model.PingDiagnosticStatus.RUNNING) {
            cancelPingDiagnostic()
            return
        }

        pingDiagnosticJob?.cancel()
        _pingDiagnosticState.value = com.onlasdan.netnet.model.PingDiagnosticState(
            status = com.onlasdan.netnet.model.PingDiagnosticStatus.RUNNING,
            progress = 0.05f,
            currentStep = "Initializing Ping Probes..."
        )

        pingDiagnosticJob = viewModelScope.launch {
            try {
                val gateway = _networkDiagnostics.value.gatewayAddress
                val result = com.onlasdan.netnet.monitor.PingDiagnosticRunner.runDiagnostic(
                    context = context,
                    gatewayIp = gateway,
                    onProgress = { progress, step ->
                        _pingDiagnosticState.value = _pingDiagnosticState.value.copy(
                            progress = progress,
                            currentStep = step
                        )
                    }
                )
                _pingDiagnosticState.value = com.onlasdan.netnet.model.PingDiagnosticState(
                    status = com.onlasdan.netnet.model.PingDiagnosticStatus.COMPLETED,
                    progress = 1.0f,
                    currentStep = "Diagnostic Completed",
                    result = result
                )
            } catch (e: Exception) {
                _pingDiagnosticState.value = com.onlasdan.netnet.model.PingDiagnosticState(
                    status = com.onlasdan.netnet.model.PingDiagnosticStatus.FAILED,
                    progress = 0f,
                    currentStep = "Test Failed",
                    errorMessage = e.message ?: "Network probe error"
                )
            }
        }
    }

    fun cancelPingDiagnostic() {
        pingDiagnosticJob?.cancel()
        _pingDiagnosticState.value = _pingDiagnosticState.value.copy(
            status = com.onlasdan.netnet.model.PingDiagnosticStatus.IDLE,
            progress = 0f,
            currentStep = "Cancelled"
        )
    }

    fun runOneTimeDiagnostic() {
        if (_oneTimeDiagnosticState.value.isRunning) {
            cancelOneTimeDiagnostic()
            return
        }

        oneTimeDiagnosticJob?.cancel()
        _oneTimeDiagnosticState.value = com.onlasdan.netnet.model.OneTimeDiagnosticState(
            stage = com.onlasdan.netnet.model.OneTimeDiagnosticStage.PING_PHASE,
            progress = 0.05f,
            statusMessage = "Starting diagnostic audit..."
        )

        oneTimeDiagnosticJob = viewModelScope.launch {
            try {
                val net = _networkDiagnostics.value
                val result = com.onlasdan.netnet.monitor.OneTimeDiagnosticRunner.runFullDiagnostic(
                    context = context,
                    gatewayIp = net.gatewayAddress,
                    networkType = net.networkType,
                    networkName = net.networkName,
                    ipAddress = net.ipv4Address,
                    onUpdate = { state ->
                        _oneTimeDiagnosticState.value = state
                    }
                )
            } catch (e: Exception) {
                _oneTimeDiagnosticState.value = com.onlasdan.netnet.model.OneTimeDiagnosticState(
                    stage = com.onlasdan.netnet.model.OneTimeDiagnosticStage.FAILED,
                    progress = 0f,
                    statusMessage = "Diagnostic failed",
                    errorMessage = e.message ?: "Failed to run test"
                )
            }
        }
    }

    fun cancelOneTimeDiagnostic() {
        oneTimeDiagnosticJob?.cancel()
        _oneTimeDiagnosticState.value = com.onlasdan.netnet.model.OneTimeDiagnosticState(
            stage = com.onlasdan.netnet.model.OneTimeDiagnosticStage.CANCELLED,
            progress = 0f,
            statusMessage = "Diagnostic cancelled"
        )
    }

    fun runSpeedBurstTest() {
        if (_isTestingSpeed.value) {
            speedTestJob?.cancel()
            _isTestingSpeed.value = false
            return
        }

        _isTestingSpeed.value = true
        speedTestJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                // Download a small chunk of test file (5MB or 10MB test data) from Cloudflare CDN to verify traffic meter
                val url = URL("https://speed.cloudflare.com/__down?bytes=15000000")
                val connection = url.openConnection() as HttpURLConnection
                connection.connectTimeout = 4000
                connection.readTimeout = 6000
                connection.connect()

                val inputStream: InputStream = connection.inputStream
                val buffer = ByteArray(16384)
                var bytesRead: Int
                while (inputStream.read(buffer).also { bytesRead = it } != -1 && _isTestingSpeed.value) {
                    // reading traffic generates rx stats
                }
                inputStream.close()
                connection.disconnect()
            } catch (_: Exception) {
                // network test completed or timed out
            } finally {
                withContext(Dispatchers.Main) {
                    _isTestingSpeed.value = false
                }
            }
        }
    }
}
