# AGENTS.md — Net Speed Indicator Codebase Guide

This document serves as the guide for AI agents working in the **Net Speed Indicator** codebase.

---

## 1. Project Overview

**Net Speed Indicator** is an Android application built with Kotlin and Jetpack Compose (Material 3) designed for real-time network throughput monitoring, data consumption analytics, and system integration (status bar chips, floating bubble overlays, quick setting tiles, and home screen widgets).

---

## 2. Architecture & Directory Structure

All source code is located under `app/src/main/java/com/aistudio/netspeedindicator/`:

- **`data/`**: `SpeedSettingsRepository.kt` manages user preferences and persisted usage records with in-memory delta batching (flushed every 15s) to reduce flash storage I/O.
- **`model/`**: `NetworkModels.kt` contains data models (`SpeedSnapshot`, `DailyUsageRecord`, `DetailedNetworkDiagnostics`, `ProcessResourceUsage`), formatting logic (`SpeedFormatter`), and enums (`SpeedUnit`, `DisplayMode`, `NotificationColorTheme`, `NotificationIconStyle`).
- **`monitor/`**: 
  - `TrafficMonitor.kt`: Core traffic polling engine with VPN deduplication, screen-off battery throttling, and latency probe.
  - `NetworkDiagnosticsHelper.kt`: Deep network interface, signal strength, and DNS querying.
  - `ProcessDiagnosticsHelper.kt`: CPU, PSS memory, and battery drain diagnostics.
- **`notification/`**: `NotificationHelper.kt` builds notifications with Android 16 promoted status bar live speed chips (`setShortCriticalText` and `setRequestPromotedOngoing`).
- **`receiver/`**: `BootReceiver.kt` restarts the foreground service on system boot.
- **`service/`**:
  - `NetSpeedForegroundService.kt`: Persistent foreground service for speed monitoring.
  - `FloatingBubbleService.kt`: Draggable system overlay bubble.
  - `SpeedTileService.kt`: Android Quick Settings toggle tile.
- **`ui/`**:
  - `MainActivity.kt`: Main activity host.
  - `MainScreen.kt`: Primary dashboard UI.
  - `MainViewModel.kt`: UI state holder.
  - `components/`: Modular Compose cards (`SpeedGauge`, `LiveSpeedGraph`, `DataUsageAnalyticsCard`, `NetworkInfoCard`, `ProcessResourceDiagnosticsCard`, etc.).
  - `theme/`: Theme, color palettes, and AMOLED dark mode support.
- **`widget/`**: `NetSpeedWidgetProvider.kt` provides the Home Screen App Widget.

---

## 3. Key Development Guidelines

1. **Jetpack Compose First**: All UI components are built using Material Design 3 and Jetpack Compose.
2. **Battery & I/O Efficiency**:
   - Screen-off state reduces polling frequency from 1s to 10s and cancels ping probes.
   - Traffic stats are batched in-memory before committing to `SharedPreferences`.
3. **Android 16 Compatibility**: Support both standard notification flows and Android 16 promoted ongoing status bar chips.
