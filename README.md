# Net Speed Indicator

[![Android](https://img.shields.io/badge/Platform-Android%2014%2B-green.svg)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20M3-blue.svg)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)

**Net Speed Indicator** is a modern, lightweight, and battery-efficient network traffic monitor and live speed indicator for Android. Built with 100% Jetpack Compose and Material 3, it provides real-time download and upload speeds, glanceable status bar indicators, floating speed bubbles, quick settings tile integration, and deep network diagnostics.

---

## Features

### ⚡ Real-Time Speed & Traffic Monitoring
- **Live Speed Gauge & Metrics**: Instant calculation and display of download and upload speeds.
- **Waveform History Graph**: 60-second real-time scrolling speed graph for network performance tracking.
- **Flexible Units**: Toggle between byte-based (`B/s`, `KB/s`, `MB/s`) and bit-based (`bps`, `Kbps`, `Mbps`) formats.
- **Display Modes**: Support for Dual (Download & Upload), Download Only, Upload Only, or Auto (Highest Speed).
- **VPN Deduplication**: Intelligently inspects physical network interfaces to prevent double-counting traffic when virtual VPN/tunnel adapters are active.

### 🔔 Status Bar & Promoted Notification (Android 16 Ready)
- **Glanceable Status Bar Chip**: Shows compact live network speeds directly in the status bar using Android 16 (`setShortCriticalText` and `setRequestPromotedOngoing`).
- **Notification Customization**:
  - **7 Color Themes**: System Dynamic, Electric Cyan, Emerald Green, Neon Purple, Solar Amber, Vibrant Rose, Slate Monochrome.
  - **4 Icon Styles**: Speedometer, Transfer Arrows, Signal Wave, Minimal Dot.
  - **Interactive Actions**: One-tap Pause/Resume and Session Reset directly from notification actions.
- **Idle Threshold**: Option to hide or mute indicator when network throughput drops below a defined threshold.

### 🎈 Floating Speed Bubble (Overlay)
- **Draggable Floating Widget**: Overlay live speeds on top of any app or game with smooth drag physics.
- **Customizable Appearance**: Adjustable background transparency, compact layout, and snap-to-edge docking.

### 📊 Historical Data Usage & Analytics
- **Daily Traffic Breakdown**: Separate tracking for Wi-Fi vs Cellular data consumption.
- **7-Day & 30-Day Analytics**: Interactive charts showing usage trends, daily averages, and peak consumption days.
- **Flash Storage Optimization**: In-memory delta batching flushed every 15 seconds to prevent continuous flash I/O wear.

### 🛠️ Network & System Diagnostics
- **Comprehensive Network Info**: Active interface type, SSID, Cellular generation, Band/Frequency, Signal Strength (dBm & %), IPv4/IPv6, Gateway, and DNS servers.
- **Live Ping & Latency Probe**: Sub-second socket latency checks to public DNS (active only when app is foregrounded).
- **Resource Diagnostics**: Real-time app CPU %, PSS RAM usage, heap allocation, and estimated battery drain rate (< 0.05%/hour).

### 📱 Android Integration
- **Quick Settings Tile**: Toggle speed monitoring on/off straight from your Android quick settings shade.
- **Home Screen App Widget**: Glanceable home screen widget showing live speed, network connection status, and daily usage.
- **Boot Startup**: Automatic background restart upon device reboot.
- **OLED / AMOLED Dark Mode**: True black theme for OLED screens to conserve power.

---

## Screenshots & Architecture

```
app/src/main/java/com/aistudio/netspeedindicator/
├── MainActivity.kt                      # Main compose host & lifecycle hooks
├── NetSpeedApp.kt                       # Application class & channel initialization
├── data/
│   └── SpeedSettingsRepository.kt       # Persistent settings & usage storage (batching)
├── model/
│   └── NetworkModels.kt                 # Data models, formatters, and state objects
├── monitor/
│   ├── TrafficMonitor.kt                # Network traffic polling & VPN dedup engine
│   ├── NetworkDiagnosticsHelper.kt      # Detailed network adapter & signal diagnostics
│   └── ProcessDiagnosticsHelper.kt      # CPU, RAM, & battery impact telemetry
├── notification/
│   └── NotificationHelper.kt            # Android 16 promoted notification builder
├── receiver/
│   └── BootReceiver.kt                  # Device boot & package update receiver
├── service/
│   ├── NetSpeedForegroundService.kt     # Background traffic monitoring service
│   ├── FloatingBubbleService.kt         # Draggable floating overlay window
│   └── SpeedTileService.kt              # Android Quick Settings dropdown tile
├── ui/
│   ├── MainScreen.kt                    # Primary Compose dashboard layout
│   ├── MainViewModel.kt                 # State management & coroutine orchestration
│   ├── components/                      # Gauge, graph, usage cards, analytics & diagnostics
│   └── theme/                           # Material 3 colors, typography, & OLED dark theme
└── widget/
    └── NetSpeedWidgetProvider.kt        # Home screen app widget provider
```

---

## Tech Stack & Requirements

- **Minimum SDK**: Android 7.0 (API level 24) / Android 14+ recommended for all features
- **Target / Compile SDK**: Android 16 (API level 36)
- **Language**: Kotlin 2.2+
- **UI Framework**: Jetpack Compose with Material Design 3
- **Concurrency**: Kotlin Coroutines & StateFlow / SharedFlow
- **Architecture**: MVVM with Clean Repository pattern

---

## Build & Installation

1. **Clone the repository**:
   ```bash
   git clone https://github.com/Pkok1024/net-speed.git
   cd net-speed
   ```

2. **Build debug APK with Gradle**:
   ```bash
   ./gradlew assembleDebug
   ```

3. **Install on connected device**:
   ```bash
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

---

## Permissions

| Permission | Purpose |
|------------|---------|
| `ACCESS_NETWORK_STATE` | Detect network connection changes and interfaces |
| `ACCESS_WIFI_STATE` | Read Wi-Fi SSID, frequency, and link speed |
| `POST_NOTIFICATIONS` | Display persistent live speed notifications |
| `POST_PROMOTED_NOTIFICATIONS` | Android 16 promoted status bar live chips |
| `FOREGROUND_SERVICE_SPECIAL_USE` | Maintain continuous background network tracking |
| `RECEIVE_BOOT_COMPLETED` | Resume indicator automatically upon device restart |
| `SYSTEM_ALERT_WINDOW` | Optional floating speed bubble overlay |

---

## License

```
Copyright 2026 Net Speed Indicator Contributors

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
