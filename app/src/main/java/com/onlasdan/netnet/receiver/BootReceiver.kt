package com.onlasdan.netnet.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.onlasdan.netnet.data.SpeedSettingsRepository
import com.onlasdan.netnet.service.NetSpeedForegroundService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        try {
            val action = intent.action ?: return
            if (action == Intent.ACTION_BOOT_COMPLETED ||
                action == Intent.ACTION_LOCKED_BOOT_COMPLETED ||
                action == Intent.ACTION_MY_PACKAGE_REPLACED ||
                action == "android.intent.action.QUICKBOOT_POWERON" ||
                action == "com.htc.intent.action.QUICKBOOT_POWERON"
            ) {
                val settings = SpeedSettingsRepository.getInstance(context).settings.value
                if (settings.isServiceEnabled && settings.autoStartOnBoot) {
                    NetSpeedForegroundService.startService(context)
                }
            }
        } catch (_: Throwable) {}
    }
}
