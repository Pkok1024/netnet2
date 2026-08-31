package com.onlasdan.netnet.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import com.onlasdan.netnet.MainActivity
import com.onlasdan.netnet.R
import com.onlasdan.netnet.data.SpeedSettings
import com.onlasdan.netnet.model.DisplayMode
import com.onlasdan.netnet.model.SpeedFormatter
import com.onlasdan.netnet.model.SpeedSnapshot
import com.onlasdan.netnet.service.NetSpeedForegroundService

object NotificationHelper {
    private const val TAG = "NotificationHelper"
    const val CHANNEL_ID = "net_speed_indicator_channel"
    const val NOTIFICATION_ID = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                if (manager != null) {
                    val name = context.getString(R.string.notification_channel_name)
                    val descriptionText = context.getString(R.string.notification_channel_desc)
                    val importance = NotificationManager.IMPORTANCE_LOW
                    val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                        description = descriptionText
                        setShowBadge(false)
                        enableVibration(false)
                        enableLights(false)
                        setSound(null, null)
                    }
                    manager.createNotificationChannel(channel)
                }
            } catch (e: Throwable) {
                Log.e(TAG, "Failed to create notification channel: ${e.message}")
            }
        }
    }

    /**
     * Checks if the app is currently allowed to post promoted notifications in Android 16 (API 36 / 36.1).
     * Evaluates NotificationManager.canPostPromotedNotifications() accounting for user settings.
     */
    fun canPostPromotedNotifications(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < 36) return false
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return false
        return try {
            val method = manager.javaClass.getMethod("canPostPromotedNotifications")
            (method.invoke(manager) as? Boolean) ?: true
        } catch (_: Throwable) {
            true
        }
    }

    /**
     * Checks whether a built notification meets the technical criteria to be promoted by Android 16.
     * (e.g., ongoing=true, has contentTitle, valid style, no custom views, not colorized, not group summary).
     */
    fun hasPromotableCharacteristics(notification: Notification): Boolean {
        if (Build.VERSION.SDK_INT < 36) return false
        return try {
            val method = notification.javaClass.getMethod("hasPromotableCharacteristics")
            (method.invoke(notification) as? Boolean) ?: true
        } catch (_: Throwable) {
            true
        }
    }

    /**
     * Creates an intent to navigate directly to the Promoted Notifications system settings page.
     */
    fun getPromotedNotificationSettingsIntent(context: Context): Intent {
        return try {
            Intent("android.settings.MANAGE_APP_PROMOTED_NOTIFICATIONS").apply {
                data = Uri.fromParts("package", context.packageName, null)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        } catch (_: Throwable) {
            try {
                Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            } catch (_: Throwable) {
                Intent(Settings.ACTION_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            }
        }
    }

    fun buildSpeedNotification(
        context: Context,
        snapshot: SpeedSnapshot,
        settings: SpeedSettings,
        isPaused: Boolean = false
    ): Notification {
        val dlFormatted = SpeedFormatter.formatSpeed(snapshot.downloadBytesPerSec, settings.speedUnit)
        val ulFormatted = SpeedFormatter.formatSpeed(snapshot.uploadBytesPerSec, settings.speedUnit)

        val thresholdBytes = settings.idleThresholdKbps * 1024L
        val isBelowThreshold = settings.idleThresholdKbps > 0 && (snapshot.downloadBytesPerSec + snapshot.uploadBytesPerSec) < thresholdBytes

        val chipText = when {
            isPaused -> "Paused"
            isBelowThreshold && settings.hideWhenIdle -> ""
            isBelowThreshold -> "Idle"
            else -> SpeedFormatter.formatShortChip(snapshot, settings.displayMode, settings.speedUnit)
        }

        // Open App Intent
        val appIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingAppIntent = PendingIntent.getActivity(
            context,
            0,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Pause / Resume
        val toggleActionIntent = Intent(context, NetSpeedForegroundService::class.java).apply {
            action = if (isPaused) NetSpeedForegroundService.ACTION_RESUME else NetSpeedForegroundService.ACTION_PAUSE
        }
        val pendingToggleIntent = PendingIntent.getService(
            context,
            1,
            toggleActionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val toggleActionTitle = if (isPaused) "Resume" else "Pause"

        // Action: Reset Session
        val resetActionIntent = Intent(context, NetSpeedForegroundService::class.java).apply {
            action = NetSpeedForegroundService.ACTION_RESET_SESSION
        }
        val pendingResetIntent = PendingIntent.getService(
            context,
            2,
            resetActionIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = when {
            isPaused -> "Network Monitor Paused"
            isBelowThreshold -> "Network Idle (< ${settings.idleThresholdKbps} KB/s)"
            else -> when (settings.displayMode) {
                DisplayMode.BOTH -> "↓ $dlFormatted   •   ↑ $ulFormatted"
                DisplayMode.DOWNLOAD_ONLY -> "↓ Download: $dlFormatted"
                DisplayMode.UPLOAD_ONLY -> "↑ Upload: $ulFormatted"
                DisplayMode.AUTO_HIGHEST -> {
                    if (snapshot.uploadBytesPerSec > snapshot.downloadBytesPerSec) {
                        "↑ Upload: $ulFormatted"
                    } else {
                        "↓ Download: $dlFormatted"
                    }
                }
            }
        }

        val sessionFormatted = SpeedFormatter.formatDataSize(snapshot.sessionTotalBytes)
        val todayFormatted = SpeedFormatter.formatDataSize(snapshot.todayTotalBytes)
        val subtitle = "Session: $sessionFormatted  |  Today: $todayFormatted  •  ${snapshot.networkName}"

        val builder = Notification.Builder(context, CHANNEL_ID)

        try {
            if (settings.notificationIconStyle == com.onlasdan.netnet.model.NotificationIconStyle.DYNAMIC_SPEED) {
                val speedIcon = DynamicSpeedIconRenderer.createSpeedIcon(
                    snapshot = snapshot,
                    displayMode = settings.displayMode,
                    speedUnit = settings.speedUnit,
                    colorTheme = settings.notificationColorTheme,
                    isPaused = isPaused,
                    isIdle = isBelowThreshold
                )
                builder.setSmallIcon(speedIcon)
            } else {
                val iconRes = when (settings.notificationIconStyle) {
                    com.onlasdan.netnet.model.NotificationIconStyle.SPEEDOMETER -> R.drawable.ic_speed_indicator
                    com.onlasdan.netnet.model.NotificationIconStyle.ARROWS -> R.drawable.ic_notif_arrows
                    com.onlasdan.netnet.model.NotificationIconStyle.SIGNAL -> R.drawable.ic_notif_signal
                    com.onlasdan.netnet.model.NotificationIconStyle.MINIMAL_DOT -> R.drawable.ic_notif_dot
                    else -> R.drawable.ic_speed_indicator
                }
                builder.setSmallIcon(iconRes)
            }
        } catch (_: Throwable) {
            builder.setSmallIcon(R.drawable.ic_speed_indicator)
        }

        builder
            .setContentTitle(title)
            .setContentText(subtitle)
            .setSubText(snapshot.networkType.title)
            .setColor(settings.notificationColorTheme.colorInt)
            // CRITICAL (Android 16 Official Requirement): Promoted Ongoing notifications / Live Updates
            // MUST NOT be colorized (setColorized(true) strictly disqualifies the notification from being promoted).
            .setColorized(false)
            .setContentIntent(pendingAppIntent)
            .setOngoing(!isPaused)
            .setGroupSummary(false)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .setCategory(Notification.CATEGORY_STATUS)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
        }

        val actionIcon = android.graphics.drawable.Icon.createWithResource(context, R.drawable.ic_speed_indicator)
        builder
            .addAction(
                Notification.Action.Builder(actionIcon, toggleActionTitle, pendingToggleIntent).build()
            )
            .addAction(
                Notification.Action.Builder(actionIcon, "Reset Session", pendingResetIntent).build()
            )

        if (settings.notificationDetailedLayout) {
            builder.setStyle(
                Notification.BigTextStyle()
                    .setBigContentTitle(title)
                    .bigText(
                        "Download: $dlFormatted\n" +
                        "Upload: $ulFormatted\n" +
                        "Session Usage: $sessionFormatted\n" +
                        "Today's Usage: $todayFormatted\n" +
                        "Network: ${snapshot.networkName} (${snapshot.networkType.title}) • IP: ${snapshot.ipAddress}"
                    )
            )
        }

        // Android 16 (API 36 / 36.1) Live Updates / Promoted Ongoing Notification APIs
        if (Build.VERSION.SDK_INT >= 36) {
            try {
                // Set Promoted Ongoing: builder.setRequestPromotedOngoing(true)
                val setPromotedMethod = builder.javaClass.getMethod("setRequestPromotedOngoing", Boolean::class.javaPrimitiveType)
                setPromotedMethod.invoke(builder, true)

                // Set Status Bar Glanceable Chip: builder.setShortCriticalText(CharSequence)
                if (settings.showStatusBarChip && chipText.isNotEmpty()) {
                    val setChipMethod = builder.javaClass.getMethod("setShortCriticalText", CharSequence::class.java)
                    setChipMethod.invoke(builder, chipText)
                }
            } catch (e: Throwable) {
                Log.d(TAG, "Promoted notification API invocation fallback: ${e.message}")
            }
        }

        val notification = try {
            builder.build()
        } catch (_: Throwable) {
            Notification.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_speed_indicator)
                .setContentTitle(title)
                .setContentText(subtitle)
                .setOngoing(!isPaused)
                .build()
        }

        if (Build.VERSION.SDK_INT >= 36) {
            try {
                val isEligible = hasPromotableCharacteristics(notification)
                Log.d(TAG, "Notification promotable characteristics check: $isEligible")
            } catch (_: Throwable) {}
        }

        return notification
    }
}
