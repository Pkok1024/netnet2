package com.onlasdan.netnet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.filled.ViewStream
import com.onlasdan.netnet.model.AppThemeMode
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import com.onlasdan.netnet.R
import com.onlasdan.netnet.widget.NetSpeedWidgetProvider
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onlasdan.netnet.data.SpeedSettings
import com.onlasdan.netnet.model.DisplayMode
import com.onlasdan.netnet.model.NotificationColorTheme
import com.onlasdan.netnet.model.NotificationIconStyle
import com.onlasdan.netnet.model.SpeedUnit
import com.onlasdan.netnet.ui.theme.AppTheme
import com.onlasdan.netnet.ui.theme.CyanGlow
import com.onlasdan.netnet.ui.theme.CyanPrimary
import com.onlasdan.netnet.ui.theme.EmeraldGlow
import com.onlasdan.netnet.ui.theme.EmeraldSuccess
import com.onlasdan.netnet.ui.theme.PurpleAccent
import com.onlasdan.netnet.ui.theme.PurpleGlow
import com.onlasdan.netnet.ui.theme.RoseError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsBottomSheet(
    settings: SpeedSettings,
    onUpdateSettings: ((SpeedSettings) -> SpeedSettings) -> Unit,
    onResetTodayUsage: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val colors = AppTheme.colors
    val context = LocalContext.current

    var showCustomIntervalDialog by remember { mutableStateOf(false) }
    var showCustomThresholdDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surfaceElevated,
        contentColor = colors.textPrimary,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Indicator Settings",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = colors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. Appearance & Theme Mode Selector
            SettingSectionHeader(
                icon = Icons.Default.Brightness4,
                title = "Appearance & Theme Mode"
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surfaceHighlight)
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                AppThemeMode.entries.forEachIndexed { index, mode ->
                    val isSelected = settings.appThemeMode == mode
                    val modeIcon = when (mode) {
                        AppThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                        AppThemeMode.LIGHT -> Icons.Default.LightMode
                        AppThemeMode.DARK -> Icons.Default.DarkMode
                        AppThemeMode.OLED -> Icons.Default.Contrast
                        AppThemeMode.PINK -> Icons.Default.Palette
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) colors.accentPrimary.copy(alpha = 0.12f) else Color.Transparent)
                            .border(
                                width = if (isSelected) 1.dp else 0.dp,
                                color = if (isSelected) colors.accentPrimary.copy(alpha = 0.4f) else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                onUpdateSettings {
                                    it.copy(
                                        appThemeMode = mode,
                                        isOledTheme = mode == AppThemeMode.OLED
                                    )
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                onUpdateSettings {
                                    it.copy(
                                        appThemeMode = mode,
                                        isOledTheme = mode == AppThemeMode.OLED
                                    )
                                }
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = colors.accentPrimary,
                                unselectedColor = colors.textTertiary
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = modeIcon,
                            contentDescription = mode.label,
                            tint = if (isSelected) colors.accentGlow else colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = mode.label,
                                fontSize = 13.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) colors.textPrimary else colors.textSecondary
                            )
                            Text(
                                text = mode.description,
                                fontSize = 11.sp,
                                color = colors.textTertiary
                            )
                        }
                    }
                    if (index < AppThemeMode.entries.size - 1) {
                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider(color = colors.cardBorder.copy(alpha = 0.5f), thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Android 16 Promoted Status Bar Chip
            SettingSectionHeader(
                icon = Icons.Default.NotificationsActive,
                title = "Android 16 Promoted Notification"
            )

            Spacer(modifier = Modifier.height(8.dp))

            SettingToggleItem(
                title = "Status Bar Live Speed Chip",
                subtitle = "Display live speed pill on Android 16 status bar",
                checked = settings.showStatusBarChip,
                testTag = "status_bar_chip_toggle",
                onCheckedChange = { checked ->
                    onUpdateSettings { it.copy(showStatusBarChip = checked) }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Speed Units
            SettingSectionHeader(
                icon = Icons.Default.Speed,
                title = "Speed Measurement Units"
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surfaceHighlight)
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp))
            ) {
                SpeedUnit.entries.forEachIndexed { index, unit ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onUpdateSettings { it.copy(speedUnit = unit) }
                            }
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = settings.speedUnit == unit,
                            onClick = { onUpdateSettings { it.copy(speedUnit = unit) } },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = CyanPrimary,
                                unselectedColor = colors.textTertiary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = unit.label,
                            fontSize = 14.sp,
                            color = if (settings.speedUnit == unit) colors.textPrimary else colors.textSecondary,
                            fontWeight = if (settings.speedUnit == unit) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                    if (index < SpeedUnit.entries.size - 1) {
                        HorizontalDivider(color = colors.cardBorder, thickness = 1.dp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Notification Display Mode & Appearance Customization
            SettingSectionHeader(
                icon = Icons.Default.Palette,
                title = "Notification Appearance & Style"
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surfaceHighlight)
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                // Live Notification Preview Box
                Text(
                    text = "LIVE NOTIFICATION PREVIEW",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                val isDynamicSpeedIcon = settings.notificationIconStyle == NotificationIconStyle.DYNAMIC_SPEED
                val previewIconRes = when (settings.notificationIconStyle) {
                    NotificationIconStyle.DYNAMIC_SPEED -> R.drawable.ic_speed_indicator
                    NotificationIconStyle.SPEEDOMETER -> R.drawable.ic_speed_indicator
                    NotificationIconStyle.ARROWS -> R.drawable.ic_notif_arrows
                    NotificationIconStyle.SIGNAL -> R.drawable.ic_notif_signal
                    NotificationIconStyle.MINIMAL_DOT -> R.drawable.ic_notif_dot
                }
                val previewColor = Color(settings.notificationColorTheme.colorInt)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, previewColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(previewColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isDynamicSpeedIcon) {
                            Text(
                                text = "4.8M",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = previewColor
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = previewIconRes),
                                contentDescription = "Notification Icon",
                                tint = previewColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "↓ 4.8 MB/s   •   ↑ 1.2 MB/s",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = if (settings.notificationDetailedLayout) "Session: 24.5 MB  |  Today: 1.4 GB  •  Home_5G" else "NetSpeed Active • Wi-Fi",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = colors.cardBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Accent Color Theme Selector
                Text(
                    text = "Notification Accent Color",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    NotificationColorTheme.entries.forEach { theme ->
                        val isSelected = settings.notificationColorTheme == theme
                        val themeColor = Color(theme.colorInt)

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(themeColor.copy(alpha = if (isSelected) 0.3f else 0.12f))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) themeColor else colors.cardBorder,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    onUpdateSettings { it.copy(notificationColorTheme = theme) }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = themeColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(androidx.compose.foundation.shape.CircleShape)
                                        .background(themeColor)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = colors.cardBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Notification Icon Style
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Status Bar & Notification Icon",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                    if (settings.notificationIconStyle == NotificationIconStyle.DYNAMIC_SPEED) {
                        Text(
                            text = "LIVE DIGITS",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGlow,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(EmeraldSuccess.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Live Speed Number draws real-time speed values directly on the status bar icon for non-promoted status bars.",
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    NotificationIconStyle.entries.forEach { iconStyle ->
                        val isSelected = settings.notificationIconStyle == iconStyle
                        val iconDrawableRes = when (iconStyle) {
                            NotificationIconStyle.DYNAMIC_SPEED -> R.drawable.ic_speed_indicator
                            NotificationIconStyle.SPEEDOMETER -> R.drawable.ic_speed_indicator
                            NotificationIconStyle.ARROWS -> R.drawable.ic_notif_arrows
                            NotificationIconStyle.SIGNAL -> R.drawable.ic_notif_signal
                            NotificationIconStyle.MINIMAL_DOT -> R.drawable.ic_notif_dot
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) CyanPrimary.copy(alpha = 0.2f) else colors.background)
                                .border(
                                    1.dp,
                                    if (isSelected) CyanPrimary else colors.cardBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    onUpdateSettings { it.copy(notificationIconStyle = iconStyle) }
                                }
                                .padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (iconStyle == NotificationIconStyle.DYNAMIC_SPEED) {
                                Text(
                                    text = "2.4M",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) CyanGlow else colors.textSecondary
                                )
                            } else {
                                Icon(
                                    painter = painterResource(id = iconDrawableRes),
                                    contentDescription = iconStyle.label,
                                    tint = if (isSelected) CyanGlow else colors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = when(iconStyle) {
                                    NotificationIconStyle.DYNAMIC_SPEED -> "Live Num"
                                    NotificationIconStyle.SPEEDOMETER -> "Gauge"
                                    NotificationIconStyle.ARROWS -> "Arrows"
                                    NotificationIconStyle.SIGNAL -> "Signal"
                                    NotificationIconStyle.MINIMAL_DOT -> "Dot"
                                },
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) CyanGlow else colors.textSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = colors.cardBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Detailed Layout Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onUpdateSettings { it.copy(notificationDetailedLayout = !it.notificationDetailedLayout) }
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Detailed Notification Layout",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Expands notification to show session & today's data metrics",
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                    Switch(
                        checked = settings.notificationDetailedLayout,
                        onCheckedChange = { checked ->
                            onUpdateSettings { it.copy(notificationDetailedLayout = checked) }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyanPrimary,
                            checkedTrackColor = CyanPrimary.copy(alpha = 0.3f),
                            uncheckedThumbColor = colors.textTertiary,
                            uncheckedTrackColor = colors.background
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = colors.cardBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Display Mode items
                Text(
                    text = "Speed Display Format",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))

                DisplayMode.entries.forEachIndexed { index, mode ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onUpdateSettings { it.copy(displayMode = mode) }
                            }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = settings.displayMode == mode,
                            onClick = { onUpdateSettings { it.copy(displayMode = mode) } },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = CyanPrimary,
                                unselectedColor = colors.textTertiary
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = mode.label,
                            fontSize = 13.sp,
                            color = if (settings.displayMode == mode) colors.textPrimary else colors.textSecondary,
                            fontWeight = if (settings.displayMode == mode) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Update Interval (Polling Refresh Rate + Custom)
            SettingSectionHeader(
                icon = Icons.Default.Timer,
                title = "Update Interval (Refresh Rate)"
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surfaceHighlight)
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Polling Interval",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyanPrimary.copy(alpha = 0.15f))
                            .border(1.dp, CyanPrimary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable { showCustomIntervalDialog = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        val intervalSec = settings.updateIntervalMs / 1000f
                        Text(
                            text = "${settings.updateIntervalMs} ms (${String.format(java.util.Locale.US, "%.1f", intervalSec)}s)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanGlow
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Slider(
                    value = settings.updateIntervalMs.toFloat().coerceIn(500f, 5000f),
                    onValueChange = { newValue ->
                        val rounded = (Math.round(newValue / 250f) * 250).toLong().coerceIn(500L, 5000L)
                        onUpdateSettings { it.copy(updateIntervalMs = rounded) }
                    },
                    valueRange = 500f..5000f,
                    steps = 17,
                    colors = SliderDefaults.colors(
                        thumbColor = CyanGlow,
                        activeTrackColor = CyanPrimary,
                        inactiveTrackColor = colors.background
                    )
                )

                // Interval Preset & Custom Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val standardPresets = listOf(500L to "500ms", 1000L to "1.0s", 2000L to "2.0s", 5000L to "5.0s")
                    val isCustom = standardPresets.none { it.first == settings.updateIntervalMs }

                    standardPresets.forEach { (ms, label) ->
                        val isSelected = settings.updateIntervalMs == ms
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) CyanPrimary.copy(alpha = 0.2f) else colors.background)
                                .border(1.dp, if (isSelected) CyanPrimary else colors.cardBorder, RoundedCornerShape(8.dp))
                                .clickable { onUpdateSettings { it.copy(updateIntervalMs = ms) } }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) CyanGlow else colors.textSecondary
                            )
                        }
                    }

                    // Custom Interval Button
                    Box(
                        modifier = Modifier
                            .weight(1.1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isCustom) PurpleAccent.copy(alpha = 0.2f) else colors.background)
                            .border(1.dp, if (isCustom) PurpleAccent else colors.cardBorder, RoundedCornerShape(8.dp))
                            .clickable { showCustomIntervalDialog = true }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Custom Interval",
                                tint = if (isCustom) PurpleGlow else colors.textSecondary,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isCustom) "${settings.updateIntervalMs}ms" else "Custom",
                                fontSize = 11.sp,
                                fontWeight = if (isCustom) FontWeight.Bold else FontWeight.Medium,
                                color = if (isCustom) PurpleGlow else colors.textSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                if (settings.isBatterySaverMode) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(EmeraldSuccess.copy(alpha = 0.12f))
                            .border(1.dp, EmeraldSuccess.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "⚡ Battery Saver active: polling is relaxed to 3.0s to conserve CPU & battery.",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = EmeraldGlow
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
                Text(
                    text = "Lower interval provides instant response; higher interval conserves battery.",
                    fontSize = 11.sp,
                    color = colors.textTertiary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 6. Speed Threshold (Idle Filter + Custom)
            SettingSectionHeader(
                icon = Icons.Default.Tune,
                title = "Speed Threshold (Idle Filter)"
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surfaceHighlight)
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Idle Cutoff Threshold",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (settings.idleThresholdKbps > 0) CyanPrimary.copy(alpha = 0.15f) else colors.background)
                            .border(1.dp, if (settings.idleThresholdKbps > 0) CyanPrimary.copy(alpha = 0.4f) else colors.cardBorder, RoundedCornerShape(8.dp))
                            .clickable { showCustomThresholdDialog = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (settings.idleThresholdKbps == 0L) "0 KB/s (Always Show)" else "${settings.idleThresholdKbps} KB/s",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (settings.idleThresholdKbps > 0) CyanGlow else colors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Slider(
                    value = settings.idleThresholdKbps.toFloat().coerceIn(0f, 200f),
                    onValueChange = { newValue ->
                        val rounded = (Math.round(newValue / 5f) * 5).toLong().coerceIn(0L, 200L)
                        onUpdateSettings { it.copy(idleThresholdKbps = rounded) }
                    },
                    valueRange = 0f..200f,
                    steps = 39,
                    colors = SliderDefaults.colors(
                        thumbColor = CyanGlow,
                        activeTrackColor = CyanPrimary,
                        inactiveTrackColor = colors.background
                    )
                )

                // Threshold Preset & Custom Chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val standardThresholds = listOf(0L to "Off (0)", 10L to "10 KB/s", 50L to "50 KB/s", 100L to "100 KB/s")
                    val isCustomThreshold = standardThresholds.none { it.first == settings.idleThresholdKbps }

                    standardThresholds.forEach { (kb, label) ->
                        val isSelected = settings.idleThresholdKbps == kb
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) CyanPrimary.copy(alpha = 0.2f) else colors.background)
                                .border(1.dp, if (isSelected) CyanPrimary else colors.cardBorder, RoundedCornerShape(8.dp))
                                .clickable { onUpdateSettings { it.copy(idleThresholdKbps = kb) } }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) CyanGlow else colors.textSecondary
                            )
                        }
                    }

                    // Custom Threshold Button
                    Box(
                        modifier = Modifier
                            .weight(1.1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isCustomThreshold) PurpleAccent.copy(alpha = 0.2f) else colors.background)
                            .border(1.dp, if (isCustomThreshold) PurpleAccent else colors.cardBorder, RoundedCornerShape(8.dp))
                            .clickable { showCustomThresholdDialog = true }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Custom Threshold",
                                tint = if (isCustomThreshold) PurpleGlow else colors.textSecondary,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isCustomThreshold) "${settings.idleThresholdKbps}k" else "Custom",
                                fontSize = 11.sp,
                                fontWeight = if (isCustomThreshold) FontWeight.Bold else FontWeight.Medium,
                                color = if (isCustomThreshold) PurpleGlow else colors.textSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = colors.cardBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onUpdateSettings { it.copy(hideWhenIdle = !it.hideWhenIdle) }
                        },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hide Status Bar Chip When Idle",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Hides speed pill in status bar when speed is below threshold",
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                    Switch(
                        checked = settings.hideWhenIdle,
                        onCheckedChange = { checked ->
                            onUpdateSettings { it.copy(hideWhenIdle = checked) }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CyanPrimary,
                            checkedTrackColor = CyanPrimary.copy(alpha = 0.3f),
                            uncheckedThumbColor = colors.textTertiary,
                            uncheckedTrackColor = colors.background
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 7. Battery & Power Optimization
            SettingSectionHeader(
                icon = Icons.Default.BatterySaver,
                title = "Battery & Resource Optimization"
            )

            Spacer(modifier = Modifier.height(8.dp))

            SettingToggleItem(
                title = "Battery Saver",
                subtitle = "Reduces network speed polling frequency to 3.0s and pauses socket latency probes to conserve battery",
                checked = settings.isBatterySaverMode,
                badge = if (settings.isBatterySaverMode) "SAVER ON" else null,
                testTag = "battery_saver_toggle",
                onCheckedChange = { checked ->
                    onUpdateSettings { it.copy(isBatterySaverMode = checked) }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))

            SettingToggleItem(
                title = "Screen-Off Deep Sleep (Auto-Pause)",
                subtitle = "Throttles traffic polling to 10s and suspends notification updates when the screen turns off",
                checked = settings.autoPauseOnScreenOff,
                testTag = "screen_off_pause_toggle",
                onCheckedChange = { checked ->
                    onUpdateSettings { it.copy(autoPauseOnScreenOff = checked) }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 8. System & Boot Options
            SettingSectionHeader(
                icon = Icons.Default.PowerSettingsNew,
                title = "System & Automation"
            )

            Spacer(modifier = Modifier.height(8.dp))

            SettingToggleItem(
                title = "Start on Device Boot",
                subtitle = "Automatically run speed indicator when phone starts",
                checked = settings.autoStartOnBoot,
                testTag = "boot_start_toggle",
                onCheckedChange = { checked ->
                    onUpdateSettings { it.copy(autoStartOnBoot = checked) }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 9. Home Screen Widget Shortcut
            SettingSectionHeader(
                icon = Icons.Default.Widgets,
                title = "Home Screen Widget"
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surfaceHighlight)
                    .border(1.dp, CyanPrimary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .clickable {
                        val appWidgetManager = context.getSystemService(AppWidgetManager::class.java)
                        if (appWidgetManager != null && appWidgetManager.isRequestPinAppWidgetSupported) {
                            val provider = ComponentName(context, NetSpeedWidgetProvider::class.java)
                            appWidgetManager.requestPinAppWidget(provider, null, null)
                        } else {
                            Toast.makeText(
                                context,
                                "Touch & hold home screen -> Widgets -> Net Speed Widget",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Pin Widget to Home Screen",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Display real-time speeds directly on your launcher",
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyanPrimary.copy(alpha = 0.2f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "ADD",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanGlow
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 9. Reset Data Usage
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(RoseError.copy(alpha = 0.1f))
                    .border(1.dp, RoseError.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .clickable { onResetTodayUsage() }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Reset",
                    tint = RoseError,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Reset Today's Data Usage",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = RoseError
                    )
                    Text(
                        text = "Clear daily network traffic counter",
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }
            }
        }
    }

    // Custom Interval Dialog
    if (showCustomIntervalDialog) {
        CustomInputDialog(
            title = "Custom Polling Interval",
            description = "Enter polling refresh interval in milliseconds (250 - 30000 ms):",
            initialValue = settings.updateIntervalMs.toString(),
            unitSuffix = "ms",
            minValue = 250L,
            maxValue = 30000L,
            onConfirm = { customMs ->
                onUpdateSettings { it.copy(updateIntervalMs = customMs) }
                showCustomIntervalDialog = false
            },
            onDismiss = { showCustomIntervalDialog = false }
        )
    }

    // Custom Threshold Dialog
    if (showCustomThresholdDialog) {
        CustomInputDialog(
            title = "Custom Speed Threshold",
            description = "Enter idle speed threshold in KB/s (0 = Always Show, max 50000 KB/s):",
            initialValue = settings.idleThresholdKbps.toString(),
            unitSuffix = "KB/s",
            minValue = 0L,
            maxValue = 50000L,
            onConfirm = { customKbps ->
                onUpdateSettings { it.copy(idleThresholdKbps = customKbps) }
                showCustomThresholdDialog = false
            },
            onDismiss = { showCustomThresholdDialog = false }
        )
    }
}

@Composable
private fun SettingSectionHeader(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String
) {
    val colors = AppTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = CyanGlow,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textSecondary
        )
    }
}

@Composable
private fun SettingToggleItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    badge: String? = null,
    testTag: String = "",
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceHighlight)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
                if (badge != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(EmeraldSuccess.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badge,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGlow
                        )
                    }
                }
            }
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = colors.textSecondary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier,
            colors = SwitchDefaults.colors(
                checkedThumbColor = CyanPrimary,
                checkedTrackColor = CyanPrimary.copy(alpha = 0.3f),
                uncheckedThumbColor = colors.textTertiary,
                uncheckedTrackColor = colors.background
            )
        )
    }
}

@Composable
private fun CustomInputDialog(
    title: String,
    description: String,
    initialValue: String,
    unitSuffix: String,
    minValue: Long,
    maxValue: Long,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var textValue by remember { mutableStateOf(initialValue) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val colors = AppTheme.colors

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surfaceElevated,
        title = {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
        },
        text = {
            Column {
                Text(
                    text = description,
                    fontSize = 13.sp,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { input ->
                        textValue = input.filter { it.isDigit() }
                        errorMessage = null
                    },
                    suffix = {
                        Text(
                            text = unitSuffix,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanGlow
                        )
                    },
                    isError = errorMessage != null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanPrimary,
                        unfocusedBorderColor = colors.cardBorder,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        focusedContainerColor = colors.surfaceHighlight,
                        unfocusedContainerColor = colors.surfaceHighlight
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = errorMessage ?: "",
                        fontSize = 11.sp,
                        color = RoseError
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = textValue.toLongOrNull()
                    if (parsed == null) {
                        errorMessage = "Please enter a valid number"
                    } else if (parsed < minValue || parsed > maxValue) {
                        errorMessage = "Value must be between $minValue and $maxValue $unitSuffix"
                    } else {
                        onConfirm(parsed)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyanPrimary,
                    contentColor = colors.background
                )
            ) {
                Text("Set", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = colors.textSecondary
                )
            ) {
                Text("Cancel")
            }
        }
    )
}
