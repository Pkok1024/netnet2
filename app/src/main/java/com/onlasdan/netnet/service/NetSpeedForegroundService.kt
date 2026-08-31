package com.onlasdan.netnet.service

import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import com.onlasdan.netnet.data.SpeedSettingsRepository
import com.onlasdan.netnet.monitor.TrafficMonitor
import com.onlasdan.netnet.notification.NotificationHelper
import com.onlasdan.netnet.widget.NetSpeedWidgetProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class NetSpeedForegroundService : Service() {
    private val scope = CoroutineScope(Dispatchers.Main)
    private var collectorJob: Job? = null

    private lateinit var trafficMonitor: TrafficMonitor
    private lateinit var settingsRepo: SpeedSettingsRepository
    private lateinit var notificationManager: NotificationManager

    private var isPaused = false
    private var isScreenOn = true

    // Track previous notification signature to avoid redundant IPC notify() calls when traffic or text is identical
    private var lastDlSpeed: Long = -1L
    private var lastUlSpeed: Long = -1L
    private var lastZeroSpeedCounter = 0
    private var lastNotificationSignature: String = ""

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val autoPause = settingsRepo.settings.value.autoPauseOnScreenOff
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    isScreenOn = false
                    // Flush any pending disk writes when screen turns off
                    settingsRepo.flushUsageToDisk()
                    if (autoPause) {
                        // Smart Battery Saver: Full pause when screen is off
                        trafficMonitor.stop()
                    } else {
                        // Standard low-frequency background sampling
                        trafficMonitor.setScreenState(false)
                    }
                }
                Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_PRESENT -> {
                    isScreenOn = true
                    if (autoPause && !isPaused) {
                        trafficMonitor.start(settingsRepo.settings.value.updateIntervalMs)
                    }
                    trafficMonitor.setScreenState(true)
                    // Immediately update notification & widget on screen wake
                    updateNotification()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        try {
            trafficMonitor = TrafficMonitor.getInstance(this)
            settingsRepo = SpeedSettingsRepository.getInstance(this)
            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            _isRunning.value = true
            _isPausedState.value = false

            // Ensure notification channel exists before starting foreground
            NotificationHelper.createNotificationChannel(this)

            // Immediately promote to foreground in onCreate to strictly satisfy Android foreground service lifecycle contracts
            val initialSnapshot = trafficMonitor.snapshot.value
            val initialSettings = settingsRepo.settings.value
            val initialNotification = NotificationHelper.buildSpeedNotification(
                this,
                initialSnapshot,
                initialSettings,
                isPaused = false
            )

            if (Build.VERSION.SDK_INT >= 34) {
                try {
                    startForeground(
                        NotificationHelper.NOTIFICATION_ID,
                        initialNotification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
                    )
                } catch (_: Throwable) {
                    try {
                        startForeground(
                            NotificationHelper.NOTIFICATION_ID,
                            initialNotification,
                            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
                        )
                    } catch (_: Throwable) {
                        try {
                            startForeground(NotificationHelper.NOTIFICATION_ID, initialNotification)
                        } catch (_: Throwable) {}
                    }
                }
            } else {
                try {
                    startForeground(NotificationHelper.NOTIFICATION_ID, initialNotification)
                } catch (_: Throwable) {}
            }
        } catch (e: Throwable) {
            android.util.Log.e("NetSpeedService", "Error in onCreate startForeground", e)
        }

        // Register screen state listener for battery conservation
        try {
            val screenFilter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_SCREEN_ON)
                addAction(Intent.ACTION_USER_PRESENT)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(screenReceiver, screenFilter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                registerReceiver(screenReceiver, screenFilter)
            }
        } catch (_: Throwable) {}
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopService()
                return START_NOT_STICKY
            }
            ACTION_PAUSE -> {
                isPaused = true
                _isPausedState.value = true
                trafficMonitor.stop()
                updateNotification()
            }
            ACTION_RESUME -> {
                isPaused = false
                _isPausedState.value = false
                trafficMonitor.start(settingsRepo.settings.value.updateIntervalMs)
                updateNotification()
            }
            ACTION_RESET_SESSION -> {
                trafficMonitor.resetSession()
                updateNotification()
            }
            else -> {
                startMonitoring()
            }
        }
        return START_STICKY
    }

    private fun startMonitoring() {
        try {
            isPaused = false
            _isPausedState.value = false

            val initialSettings = settingsRepo.settings.value
            trafficMonitor.start(initialSettings.updateIntervalMs)

            collectorJob?.cancel()
            collectorJob = scope.launch {
                combine(trafficMonitor.snapshot, settingsRepo.settings) { snapshot, settings ->
                    Pair(snapshot, settings)
                }.collect { (snapshot, settings) ->
                    try {
                        // Battery Optimization: Only post notification updates when screen is ON and not paused
                        if (!isPaused && isScreenOn) {
                            val dl = snapshot.downloadBytesPerSec
                            val ul = snapshot.uploadBytesPerSec

                            // Build compact signature to test if notification contents have changed
                            val signature = "${snapshot.downloadBytesPerSec}_${snapshot.uploadBytesPerSec}_${snapshot.networkName}_${settings.displayMode}_${settings.speedUnit}_${settings.notificationIconStyle}_${settings.notificationColorTheme}"

                            // If speed has stayed 0 B/s for several ticks, reduce notification spam
                            if (dl == 0L && ul == 0L && lastDlSpeed == 0L && lastUlSpeed == 0L) {
                                lastZeroSpeedCounter++
                                // Throttle redundant zero-speed notifications to once every 10 seconds unless signature changed
                                if (lastZeroSpeedCounter % 10 != 0 && signature == lastNotificationSignature) {
                                    return@collect
                                }
                            } else {
                                lastZeroSpeedCounter = 0
                            }

                            lastDlSpeed = dl
                            lastUlSpeed = ul
                            lastNotificationSignature = signature

                            val notif = NotificationHelper.buildSpeedNotification(
                                this@NetSpeedForegroundService,
                                snapshot,
                                settings,
                                isPaused = false
                            )
                            notificationManager.notify(NotificationHelper.NOTIFICATION_ID, notif)

                            // Real-time widget update
                            if (NetSpeedWidgetProvider.hasActiveWidgets(this@NetSpeedForegroundService)) {
                                NetSpeedWidgetProvider.updateAllWidgets(
                                    this@NetSpeedForegroundService,
                                    snapshot,
                                    settings
                                )
                            }
                        }
                    } catch (_: Throwable) {}
                }
            }
        } catch (_: Throwable) {}
    }

    private fun updateNotification() {
        try {
            val snapshot = trafficMonitor.snapshot.value
            val settings = settingsRepo.settings.value
            lastNotificationSignature = "" // Force update
            val notif = NotificationHelper.buildSpeedNotification(
                this,
                snapshot,
                settings,
                isPaused = isPaused
            )
            notificationManager.notify(NotificationHelper.NOTIFICATION_ID, notif)
            NetSpeedWidgetProvider.updateAllWidgets(this, snapshot, settings)
        } catch (_: Throwable) {}
    }

    private fun stopService() {
        try {
            _isRunning.value = false
            _isPausedState.value = false
            collectorJob?.cancel()
            settingsRepo.flushUsageToDisk()
            trafficMonitor.stop()
            stopForeground(STOP_FOREGROUND_REMOVE)
            NetSpeedWidgetProvider.updateAllWidgets(this)
            stopSelf()
        } catch (_: Throwable) {}
    }

    override fun onDestroy() {
        _isRunning.value = false
        _isPausedState.value = false
        collectorJob?.cancel()
        settingsRepo.flushUsageToDisk()
        try {
            unregisterReceiver(screenReceiver)
        } catch (_: Exception) {}
        trafficMonitor.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "com.onlasdan.netnet.action.START"
        const val ACTION_STOP = "com.onlasdan.netnet.action.STOP"
        const val ACTION_PAUSE = "com.onlasdan.netnet.action.PAUSE"
        const val ACTION_RESUME = "com.onlasdan.netnet.action.RESUME"
        const val ACTION_RESET_SESSION = "com.onlasdan.netnet.action.RESET_SESSION"

        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

        private val _isPausedState = MutableStateFlow(false)
        val isPausedState: StateFlow<Boolean> = _isPausedState.asStateFlow()

        fun startService(context: Context) {
            try {
                val intent = Intent(context, NetSpeedForegroundService::class.java).apply {
                    action = ACTION_START
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (_: Throwable) {}
        }

        fun stopService(context: Context) {
            try {
                val intent = Intent(context, NetSpeedForegroundService::class.java).apply {
                    action = ACTION_STOP
                }
                context.startService(intent)
            } catch (_: Throwable) {}
        }

        fun togglePause(context: Context, currentlyPaused: Boolean) {
            try {
                val intent = Intent(context, NetSpeedForegroundService::class.java).apply {
                    action = if (currentlyPaused) ACTION_RESUME else ACTION_PAUSE
                }
                context.startService(intent)
            } catch (_: Throwable) {}
        }

        fun resetSession(context: Context) {
            try {
                val intent = Intent(context, NetSpeedForegroundService::class.java).apply {
                    action = ACTION_RESET_SESSION
                }
                context.startService(intent)
            } catch (_: Throwable) {}
        }
    }
}
