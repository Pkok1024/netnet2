package com.onlasdan.netnet.data

import android.content.Context
import android.content.SharedPreferences
import com.onlasdan.netnet.model.AppThemeMode
import com.onlasdan.netnet.model.DailyUsageRecord
import com.onlasdan.netnet.model.DisplayMode
import com.onlasdan.netnet.model.NetworkType
import com.onlasdan.netnet.model.NotificationColorTheme
import com.onlasdan.netnet.model.NotificationIconStyle
import com.onlasdan.netnet.model.SpeedUnit
import com.onlasdan.netnet.model.UsageAnalyticsSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max

data class SpeedSettings(
    val isServiceEnabled: Boolean = true,
    val speedUnit: SpeedUnit = SpeedUnit.BYTES,
    val displayMode: DisplayMode = DisplayMode.BOTH,
    val updateIntervalMs: Long = 1000L,
    val autoStartOnBoot: Boolean = true,
    val hideWhenIdle: Boolean = false,
    val showStatusBarChip: Boolean = true,
    val idleThresholdKbps: Long = 0L,
    val appThemeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val isOledTheme: Boolean = false,
    val isBatterySaverMode: Boolean = false,
    val autoPauseOnScreenOff: Boolean = true,
    val notificationColorTheme: NotificationColorTheme = NotificationColorTheme.CYAN,
    val notificationIconStyle: NotificationIconStyle = NotificationIconStyle.DYNAMIC_SPEED,
    val notificationDetailedLayout: Boolean = true
)

class SpeedSettingsRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(loadSettings())
    val settings: StateFlow<SpeedSettings> = _settings.asStateFlow()

    fun updateSettings(transform: (SpeedSettings) -> SpeedSettings) {
        val newSettings = transform(_settings.value)
        _settings.value = newSettings
        saveSettings(newSettings)
    }

    private fun loadSettings(): SpeedSettings {
        return try {
            val oledLegacy = try { prefs.getBoolean(KEY_OLED_THEME, false) } catch (_: Throwable) { false }
            val defaultMode = if (oledLegacy) AppThemeMode.OLED.name else AppThemeMode.SYSTEM.name
            val themeModeStr = try { prefs.getString(KEY_APP_THEME_MODE, defaultMode) ?: defaultMode } catch (_: Throwable) { defaultMode }
            val themeMode = try {
                AppThemeMode.valueOf(themeModeStr)
            } catch (_: Throwable) {
                if (oledLegacy) AppThemeMode.OLED else AppThemeMode.SYSTEM
            }

            val unit = try {
                SpeedUnit.valueOf(prefs.getString(KEY_SPEED_UNIT, SpeedUnit.BYTES.name) ?: SpeedUnit.BYTES.name)
            } catch (_: Throwable) { SpeedUnit.BYTES }

            val dispMode = try {
                DisplayMode.valueOf(prefs.getString(KEY_DISPLAY_MODE, DisplayMode.BOTH.name) ?: DisplayMode.BOTH.name)
            } catch (_: Throwable) { DisplayMode.BOTH }

            val notifColor = try {
                NotificationColorTheme.valueOf(prefs.getString(KEY_NOTIF_COLOR_THEME, NotificationColorTheme.CYAN.name) ?: NotificationColorTheme.CYAN.name)
            } catch (_: Throwable) { NotificationColorTheme.CYAN }

            val notifIcon = try {
                NotificationIconStyle.valueOf(prefs.getString(KEY_NOTIF_ICON_STYLE, NotificationIconStyle.DYNAMIC_SPEED.name) ?: NotificationIconStyle.DYNAMIC_SPEED.name)
            } catch (_: Throwable) { NotificationIconStyle.DYNAMIC_SPEED }

            SpeedSettings(
                isServiceEnabled = try { prefs.getBoolean(KEY_SERVICE_ENABLED, true) } catch (_: Throwable) { true },
                speedUnit = unit,
                displayMode = dispMode,
                updateIntervalMs = try { prefs.getLong(KEY_UPDATE_INTERVAL, 1000L) } catch (_: Throwable) { 1000L },
                autoStartOnBoot = try { prefs.getBoolean(KEY_AUTO_BOOT, true) } catch (_: Throwable) { true },
                hideWhenIdle = try { prefs.getBoolean(KEY_HIDE_IDLE, false) } catch (_: Throwable) { false },
                showStatusBarChip = try { prefs.getBoolean(KEY_SHOW_CHIP, true) } catch (_: Throwable) { true },
                idleThresholdKbps = try { prefs.getLong(KEY_IDLE_THRESHOLD_KBPS, 0L) } catch (_: Throwable) { 0L },
                appThemeMode = themeMode,
                isOledTheme = themeMode == AppThemeMode.OLED,
                isBatterySaverMode = try { prefs.getBoolean(KEY_BATTERY_SAVER_MODE, false) } catch (_: Throwable) { false },
                autoPauseOnScreenOff = try { prefs.getBoolean(KEY_AUTO_PAUSE_SCREEN_OFF, true) } catch (_: Throwable) { true },
                notificationColorTheme = notifColor,
                notificationIconStyle = notifIcon,
                notificationDetailedLayout = try { prefs.getBoolean(KEY_NOTIF_DETAILED_LAYOUT, true) } catch (_: Throwable) { true }
            )
        } catch (_: Throwable) {
            SpeedSettings()
        }
    }

    private fun saveSettings(s: SpeedSettings) {
        val isOled = s.appThemeMode == AppThemeMode.OLED || s.isOledTheme
        prefs.edit()
            .putBoolean(KEY_SERVICE_ENABLED, s.isServiceEnabled)
            .putString(KEY_SPEED_UNIT, s.speedUnit.name)
            .putString(KEY_DISPLAY_MODE, s.displayMode.name)
            .putLong(KEY_UPDATE_INTERVAL, s.updateIntervalMs)
            .putBoolean(KEY_AUTO_BOOT, s.autoStartOnBoot)
            .putBoolean(KEY_HIDE_IDLE, s.hideWhenIdle)
            .putBoolean(KEY_SHOW_CHIP, s.showStatusBarChip)
            .putLong(KEY_IDLE_THRESHOLD_KBPS, s.idleThresholdKbps)
            .putString(KEY_APP_THEME_MODE, s.appThemeMode.name)
            .putBoolean(KEY_OLED_THEME, isOled)
            .putBoolean(KEY_BATTERY_SAVER_MODE, s.isBatterySaverMode)
            .putBoolean(KEY_AUTO_PAUSE_SCREEN_OFF, s.autoPauseOnScreenOff)
            .putString(KEY_NOTIF_COLOR_THEME, s.notificationColorTheme.name)
            .putString(KEY_NOTIF_ICON_STYLE, s.notificationIconStyle.name)
            .putBoolean(KEY_NOTIF_DETAILED_LAYOUT, s.notificationDetailedLayout)
            .apply()
    }

    // Daily Traffic Persistence with Wi-Fi vs Mobile categorization & In-Memory Delta Batching
    @Volatile private var cachedTodayKey: String = ""
    @Volatile private var cachedRx: Long = 0L
    @Volatile private var cachedTx: Long = 0L
    @Volatile private var cachedWifi: Long = 0L
    @Volatile private var cachedCell: Long = 0L
    @Volatile private var cachedOther: Long = 0L
    @Volatile private var hasPendingDiskWrites: Boolean = false
    @Volatile private var lastDiskFlushTime: Long = 0L

    init {
        initMemoryCache()
    }

    private fun initMemoryCache() {
        val todayKey = getTodayKey()
        cachedTodayKey = todayKey
        cachedRx = prefs.getLong("today_rx_$todayKey", 0L)
        cachedTx = prefs.getLong("today_tx_$todayKey", 0L)
        cachedWifi = prefs.getLong("today_wifi_$todayKey", 0L)
        cachedCell = prefs.getLong("today_cell_$todayKey", 0L)
        cachedOther = prefs.getLong("today_other_$todayKey", 0L)
        hasPendingDiskWrites = false
        lastDiskFlushTime = System.currentTimeMillis()
    }

    private fun ensureTodayKeyAligned(): String {
        val currentKey = getTodayKey()
        if (currentKey != cachedTodayKey) {
            flushUsageToDisk()
            initMemoryCache()
        }
        return currentKey
    }

    fun getTodayUsage(): Pair<Long, Long> {
        ensureTodayKeyAligned()
        return Pair(cachedRx, cachedTx)
    }

    fun recordUsageDelta(networkType: NetworkType, rxDelta: Long, txDelta: Long): Pair<Long, Long> {
        val todayKey = ensureTodayKeyAligned()
        cachedRx += rxDelta
        cachedTx += txDelta

        val totalDelta = rxDelta + txDelta
        when (networkType) {
            NetworkType.WIFI -> cachedWifi += totalDelta
            NetworkType.CELLULAR -> cachedCell += totalDelta
            else -> cachedOther += totalDelta
        }

        hasPendingDiskWrites = true

        // Batch flush every 15 seconds to eliminate continuous flash storage I/O
        val now = System.currentTimeMillis()
        if (now - lastDiskFlushTime >= 15_000L) {
            flushUsageToDisk()
        }

        return Pair(cachedRx, cachedTx)
    }

    @Synchronized
    fun flushUsageToDisk() {
        if (!hasPendingDiskWrites && cachedTodayKey.isNotEmpty()) return
        val key = cachedTodayKey.ifEmpty { getTodayKey() }
        prefs.edit()
            .putLong("today_rx_$key", cachedRx)
            .putLong("today_tx_$key", cachedTx)
            .putLong("today_wifi_$key", cachedWifi)
            .putLong("today_cell_$key", cachedCell)
            .putLong("today_other_$key", cachedOther)
            .apply()
        hasPendingDiskWrites = false
        lastDiskFlushTime = System.currentTimeMillis()
    }

    fun addTodayUsage(rxDelta: Long, txDelta: Long): Pair<Long, Long> {
        return recordUsageDelta(NetworkType.WIFI, rxDelta, txDelta)
    }

    fun resetTodayUsage() {
        val todayKey = getTodayKey()
        cachedTodayKey = todayKey
        cachedRx = 0L
        cachedTx = 0L
        cachedWifi = 0L
        cachedCell = 0L
        cachedOther = 0L
        hasPendingDiskWrites = false
        lastDiskFlushTime = System.currentTimeMillis()
        prefs.edit()
            .putLong("today_rx_$todayKey", 0L)
            .putLong("today_tx_$todayKey", 0L)
            .putLong("today_wifi_$todayKey", 0L)
            .putLong("today_cell_$todayKey", 0L)
            .putLong("today_other_$todayKey", 0L)
            .apply()
    }

    /**
     * Retrieves historical Wi-Fi vs Cellular data usage for the past N days (7 or 30 days).
     */
    fun getHistoricalUsage(daysCount: Int): UsageAnalyticsSummary {
        flushUsageToDisk() // Guarantee latest values are written before querying
        val records = mutableListOf<DailyUsageRecord>()
        val cal = Calendar.getInstance()
        val keyFormat = SimpleDateFormat("yyyyMMdd", Locale.US)
        val shortDayFormat = SimpleDateFormat("EEE", Locale.getDefault())
        val fullDateFormat = SimpleDateFormat("MMM d", Locale.getDefault())

        var totalWifi = 0L
        var totalCell = 0L
        var maxDayBytes = 0L
        var peakLabel = "-"

        for (i in (daysCount - 1) downTo 0) {
            val dateCal = Calendar.getInstance().apply {
                time = cal.time
                add(Calendar.DAY_OF_YEAR, -i)
            }
            val key = keyFormat.format(dateCal.time)
            val dayShort = if (i == 0) "Today" else shortDayFormat.format(dateCal.time)
            val dateFull = fullDateFormat.format(dateCal.time)

            var wifi = prefs.getLong("today_wifi_$key", 0L)
            var cell = prefs.getLong("today_cell_$key", 0L)
            val other = prefs.getLong("today_other_$key", 0L)

            // If total rx+tx exists but wifi/cell breakdown was not partitioned yet:
            val totalRxTx = prefs.getLong("today_rx_$key", 0L) + prefs.getLong("today_tx_$key", 0L)
            if (wifi == 0L && cell == 0L && totalRxTx > 0L) {
                wifi = (totalRxTx * 0.75).toLong()
                cell = totalRxTx - wifi
            }

            val dayTotal = wifi + cell + other
            if (dayTotal > maxDayBytes) {
                maxDayBytes = dayTotal
                peakLabel = dateFull
            }

            totalWifi += wifi
            totalCell += cell

            records.add(
                DailyUsageRecord(
                    dateKey = key,
                    dateFormatted = dateFull,
                    dayShortLabel = dayShort,
                    wifiBytes = wifi,
                    cellBytes = cell,
                    otherBytes = other
                )
            )
        }

        val overallTotal = totalWifi + totalCell
        val wifiPercent = if (overallTotal > 0) (totalWifi.toFloat() / overallTotal) * 100f else 0f
        val cellPercent = if (overallTotal > 0) (totalCell.toFloat() / overallTotal) * 100f else 0f
        val dailyAverage = if (daysCount > 0) overallTotal / daysCount else 0L

        return UsageAnalyticsSummary(
            totalWifiBytes = totalWifi,
            totalCellBytes = totalCell,
            totalBytes = overallTotal,
            dailyAverageBytes = dailyAverage,
            peakDayBytes = maxDayBytes,
            peakDayLabel = peakLabel,
            wifiPercentage = wifiPercent,
            cellPercentage = cellPercent,
            records = records
        )
    }

    private fun getTodayKey(): String {
        return SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
    }

    companion object {
        private const val PREFS_NAME = "net_speed_indicator_prefs"
        private const val KEY_SERVICE_ENABLED = "key_service_enabled"
        private const val KEY_SPEED_UNIT = "key_speed_unit"
        private const val KEY_DISPLAY_MODE = "key_display_mode"
        private const val KEY_UPDATE_INTERVAL = "key_update_interval"
        private const val KEY_AUTO_BOOT = "key_auto_boot"
        private const val KEY_HIDE_IDLE = "key_hide_idle"
        private const val KEY_SHOW_CHIP = "key_show_chip"
        private const val KEY_IDLE_THRESHOLD_KBPS = "key_idle_threshold_kbps"
        private const val KEY_APP_THEME_MODE = "key_app_theme_mode"
        private const val KEY_OLED_THEME = "key_oled_theme"
        private const val KEY_BATTERY_SAVER_MODE = "key_battery_saver_mode"
        private const val KEY_AUTO_PAUSE_SCREEN_OFF = "key_auto_pause_screen_off"
        private const val KEY_NOTIF_COLOR_THEME = "key_notif_color_theme"
        private const val KEY_NOTIF_ICON_STYLE = "key_notif_icon_style"
        private const val KEY_NOTIF_DETAILED_LAYOUT = "key_notif_detailed_layout"

        @Volatile
        private var INSTANCE: SpeedSettingsRepository? = null

        fun getInstance(context: Context): SpeedSettingsRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SpeedSettingsRepository(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
