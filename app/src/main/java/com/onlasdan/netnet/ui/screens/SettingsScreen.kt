package com.onlasdan.netnet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatterySaver
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Contrast
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onlasdan.netnet.R
import com.onlasdan.netnet.data.SpeedSettings
import com.onlasdan.netnet.model.AppThemeMode
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
import java.util.Locale

@Composable
fun SettingsScreen(
    settings: SpeedSettings,
    onUpdateSettings: ((SpeedSettings) -> SpeedSettings) -> Unit,
    onResetTodayUsage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    var showCustomIntervalDialog by remember { mutableStateOf(false) }
    var showCustomThresholdDialog by remember { mutableStateOf(false) }
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Appearance & Theme Mode Selector
        SettingsSection(
            icon = Icons.Default.Brightness4,
            title = "Appearance & Theme Mode"
        ) {
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
        }

        // 2. Android 16 Promoted Status Bar Chip
        SettingsSection(
            icon = Icons.Default.NotificationsActive,
            title = "Android 16 Promoted Notification"
        ) {
            ToggleSettingRow(
                title = "Status Bar Live Speed Chip",
                subtitle = "Promoted ongoing notification pill directly in Android 16 status bar",
                checked = settings.showStatusBarChip,
                testTag = "status_bar_chip_toggle_page",
                onCheckedChange = { checked ->
                    onUpdateSettings { it.copy(showStatusBarChip = checked) }
                }
            )
        }

        // 3. Speed Measurement Units
        SettingsSection(
            icon = Icons.Default.Speed,
            title = "Speed Measurement Units"
        ) {
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
                                selectedColor = colors.accentPrimary,
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
        }

        // 4. Notification Style & Theme Palette
        SettingsSection(
            icon = Icons.Default.Palette,
            title = "Notification Style & Theme"
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.surfaceHighlight)
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                // Accent Color Selector
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
                                        .clip(CircleShape)
                                        .background(themeColor)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = colors.cardBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Icon Style Selector
                Text(
                    text = "Status Bar & Notification Icon",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
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
                                .background(if (isSelected) colors.accentPrimary.copy(alpha = 0.2f) else colors.background)
                                .border(
                                    1.dp,
                                    if (isSelected) colors.accentPrimary else colors.cardBorder,
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
                                    color = if (isSelected) colors.accentGlow else colors.textSecondary
                                )
                            } else {
                                Icon(
                                    painter = painterResource(id = iconDrawableRes),
                                    contentDescription = iconStyle.label,
                                    tint = if (isSelected) colors.accentGlow else colors.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = when (iconStyle) {
                                    NotificationIconStyle.DYNAMIC_SPEED -> "Live Num"
                                    NotificationIconStyle.SPEEDOMETER -> "Gauge"
                                    NotificationIconStyle.ARROWS -> "Arrows"
                                    NotificationIconStyle.SIGNAL -> "Signal"
                                    NotificationIconStyle.MINIMAL_DOT -> "Dot"
                                },
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) colors.accentGlow else colors.textSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = colors.cardBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Detailed layout switch
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
                            checkedThumbColor = colors.accentPrimary,
                            checkedTrackColor = colors.accentPrimary.copy(alpha = 0.3f),
                            uncheckedThumbColor = colors.textTertiary,
                            uncheckedTrackColor = colors.background
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = colors.cardBorder, thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Display mode radio group
                Text(
                    text = "Speed Display Format",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))

                DisplayMode.entries.forEach { mode ->
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
                                selectedColor = colors.accentPrimary,
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
        }

        // 5. Polling Refresh Rate Interval
        SettingsSection(
            icon = Icons.Default.Timer,
            title = "Update Interval (Refresh Rate)"
        ) {
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
                            .background(colors.accentPrimary.copy(alpha = 0.15f))
                            .border(1.dp, colors.accentPrimary.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .clickable { showCustomIntervalDialog = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        val intervalSec = settings.updateIntervalMs / 1000f
                        Text(
                            text = "${settings.updateIntervalMs} ms (${String.format(Locale.US, "%.1f", intervalSec)}s)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.accentGlow
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
                        thumbColor = colors.accentGlow,
                        activeTrackColor = colors.accentPrimary,
                        inactiveTrackColor = colors.background
                    )
                )

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
                                .background(if (isSelected) colors.accentPrimary.copy(alpha = 0.2f) else colors.background)
                                .border(1.dp, if (isSelected) colors.accentPrimary else colors.cardBorder, RoundedCornerShape(8.dp))
                                .clickable { onUpdateSettings { it.copy(updateIntervalMs = ms) } }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) colors.accentGlow else colors.textSecondary
                            )
                        }
                    }

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
            }
        }

        // 6. Speed Threshold (Idle Filter)
        SettingsSection(
            icon = Icons.Default.Tune,
            title = "Speed Threshold (Idle Filter)"
        ) {
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
                            .background(if (settings.idleThresholdKbps > 0) colors.accentPrimary.copy(alpha = 0.15f) else colors.background)
                            .border(1.dp, if (settings.idleThresholdKbps > 0) colors.accentPrimary.copy(alpha = 0.4f) else colors.cardBorder, RoundedCornerShape(8.dp))
                            .clickable { showCustomThresholdDialog = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (settings.idleThresholdKbps == 0L) "0 KB/s (Always Show)" else "${settings.idleThresholdKbps} KB/s",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (settings.idleThresholdKbps > 0) colors.accentGlow else colors.textSecondary
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
                        thumbColor = colors.accentGlow,
                        activeTrackColor = colors.accentPrimary,
                        inactiveTrackColor = colors.background
                    )
                )

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
                                .background(if (isSelected) colors.accentPrimary.copy(alpha = 0.2f) else colors.background)
                                .border(1.dp, if (isSelected) colors.accentPrimary else colors.cardBorder, RoundedCornerShape(8.dp))
                                .clickable { onUpdateSettings { it.copy(idleThresholdKbps = kb) } }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) colors.accentGlow else colors.textSecondary
                            )
                        }
                    }

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
                            text = "Hide Notification When Idle",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Hides notification bar when total speed drops below threshold",
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
                            checkedThumbColor = colors.accentPrimary,
                            checkedTrackColor = colors.accentPrimary.copy(alpha = 0.3f),
                            uncheckedThumbColor = colors.textTertiary,
                            uncheckedTrackColor = colors.background
                        )
                    )
                }
            }
        }

        // 7. Battery Saver & Efficiency
        SettingsSection(
            icon = Icons.Default.BatterySaver,
            title = "Battery Saver & Efficiency"
        ) {
            ToggleSettingRow(
                title = "Smart Battery Saver Mode",
                subtitle = "Extends polling interval to 3.0s to reduce CPU load & preserve battery",
                checked = settings.isBatterySaverMode,
                testTag = "battery_saver_toggle_page",
                onCheckedChange = { checked ->
                    onUpdateSettings { it.copy(isBatterySaverMode = checked) }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // Custom Interval Dialog
    if (showCustomIntervalDialog) {
        var intervalInput by remember { mutableStateOf(settings.updateIntervalMs.toString()) }
        var isError by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showCustomIntervalDialog = false },
            containerColor = colors.surfaceElevated,
            title = {
                Text(
                    text = "Custom Update Interval",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter a polling interval in milliseconds (250ms – 10000ms):",
                        fontSize = 13.sp,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = intervalInput,
                        onValueChange = {
                            intervalInput = it
                            val parsed = it.toLongOrNull()
                            isError = parsed == null || parsed !in 250L..10000L
                        },
                        isError = isError,
                        label = { Text("Interval (ms)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.accentPrimary,
                            unfocusedBorderColor = colors.cardBorder,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = intervalInput.toLongOrNull()
                        if (parsed != null && parsed in 250L..10000L) {
                            onUpdateSettings { it.copy(updateIntervalMs = parsed) }
                            showCustomIntervalDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accentPrimary)
                ) {
                    Text("Apply", color = colors.background)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomIntervalDialog = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            }
        )
    }

    // Custom Threshold Dialog
    if (showCustomThresholdDialog) {
        var thresholdInput by remember { mutableStateOf(settings.idleThresholdKbps.toString()) }
        var isError by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showCustomThresholdDialog = false },
            containerColor = colors.surfaceElevated,
            title = {
                Text(
                    text = "Custom Idle Threshold",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter idle cutoff threshold in KB/s (0 – 10000 KB/s):",
                        fontSize = 13.sp,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = thresholdInput,
                        onValueChange = {
                            thresholdInput = it
                            val parsed = it.toLongOrNull()
                            isError = parsed == null || parsed !in 0L..10000L
                        },
                        isError = isError,
                        label = { Text("Threshold (KB/s)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.accentPrimary,
                            unfocusedBorderColor = colors.cardBorder,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = thresholdInput.toLongOrNull()
                        if (parsed != null && parsed in 0L..10000L) {
                            onUpdateSettings { it.copy(idleThresholdKbps = parsed) }
                            showCustomThresholdDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accentPrimary)
                ) {
                    Text("Apply", color = colors.background)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomThresholdDialog = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    icon: ImageVector,
    title: String,
    content: @Composable () -> Unit
) {
    val colors = AppTheme.colors
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.accentPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = colors.accentGlow,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
        }
        content()
    }
}

@Composable
private fun ToggleSettingRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    testTag: String,
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
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = colors.textSecondary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.accentPrimary,
                checkedTrackColor = colors.accentPrimary.copy(alpha = 0.3f),
                uncheckedThumbColor = colors.textTertiary,
                uncheckedTrackColor = colors.background
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}
