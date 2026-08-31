package com.onlasdan.netnet

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.onlasdan.netnet.monitor.TrafficMonitor
import com.onlasdan.netnet.ui.MainScreen
import com.onlasdan.netnet.ui.MainViewModel
import com.onlasdan.netnet.ui.theme.NetSpeedTheme

import androidx.lifecycle.ViewModelProvider

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels {
        ViewModelProvider.AndroidViewModelFactory.getInstance(application)
    }
    private val trafficMonitor by lazy { 
        try {
            TrafficMonitor.getInstance(applicationContext)
        } catch (e: Throwable) {
            Log.e("MainActivity", "TrafficMonitor init error", e)
            null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            enableEdgeToEdge()
        } catch (_: Throwable) {}

        setContent {
            val settings by viewModel.settings.collectAsState()
            NetSpeedTheme(
                themeMode = settings.appThemeMode,
                isOled = settings.isOledTheme
            ) {
                MainScreen(viewModel = viewModel)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        try {
            trafficMonitor?.setAppForeground(true)
        } catch (_: Throwable) {}
    }

    override fun onResume() {
        super.onResume()
        try {
            viewModel.ensureServiceStarted()
        } catch (_: Throwable) {}
    }

    override fun onStop() {
        try {
            trafficMonitor?.setAppForeground(false)
        } catch (_: Throwable) {}
        super.onStop()
    }
}

