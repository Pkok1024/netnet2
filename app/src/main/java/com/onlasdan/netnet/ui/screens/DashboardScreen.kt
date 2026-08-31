package com.onlasdan.netnet.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onlasdan.netnet.data.SpeedSettings
import com.onlasdan.netnet.model.SpeedFormatter
import com.onlasdan.netnet.model.SpeedPoint
import com.onlasdan.netnet.model.SpeedSnapshot
import com.onlasdan.netnet.ui.components.FloatingBubbleCard
import com.onlasdan.netnet.ui.components.LiveSpeedGraph
import com.onlasdan.netnet.ui.components.SpeedMetricCards
import com.onlasdan.netnet.ui.components.StaggeredAnimatedItem
import com.onlasdan.netnet.ui.theme.AmberWarning
import com.onlasdan.netnet.ui.theme.AppTheme
import com.onlasdan.netnet.ui.theme.CyanGlow
import com.onlasdan.netnet.ui.theme.CyanPrimary
import com.onlasdan.netnet.ui.theme.EmeraldGlow
import com.onlasdan.netnet.ui.theme.EmeraldSuccess
import com.onlasdan.netnet.ui.theme.PurpleAccent
import com.onlasdan.netnet.ui.theme.RoseError

private const val PING_GOOD_THRESHOLD_MS = 80L

/**
 * Primary Dashboard screen displaying live speed gauge, waveform graph,
 * network status, service controls, and quick metrics.
 */
@Suppress("LongParameterList")
@Composable
fun DashboardScreen(
    snapshot: SpeedSnapshot,
    history: List<SpeedPoint>,
    settings: SpeedSettings,
    isRunning: Boolean,
    isPaused: Boolean,
    isTestingSpeed: Boolean,
    isBubbleActive: Boolean,
    hasNotificationPermission: Boolean,
    onRequestNotificationPermission: () -> Unit,
    onToggleService: () -> Unit,
    onTogglePause: () -> Unit,
    onRunSpeedTest: () -> Unit,
    onToggleBubble: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Android 16 Promoted Status Bar Notification Badge Card
        StaggeredAnimatedItem(index = 0, modifier = Modifier.fillMaxWidth()) {
            PromotedNotificationBadgeCard(
                hasPermission = hasNotificationPermission,
                onRequestPermission = onRequestNotificationPermission,
                isServiceRunning = isRunning,
                isPaused = isPaused
            )
        }

        // 2. Active Network Mini Banner
        StaggeredAnimatedItem(index = 1, modifier = Modifier.fillMaxWidth()) {
            ActiveNetworkStatusBanner(
                snapshot = snapshot
            )
        }

        // 3. Dual Download & Upload Metric Cards
        StaggeredAnimatedItem(index = 2, modifier = Modifier.fillMaxWidth()) {
            SpeedMetricCards(
                snapshot = snapshot,
                unit = settings.speedUnit
            )
        }

        // 4. Service Controls Bar (Start/Stop, Pause, Burst Test)
        StaggeredAnimatedItem(index = 3, modifier = Modifier.fillMaxWidth()) {
            ServiceControlsBar(
                isRunning = isRunning,
                isPaused = isPaused,
                isTestingSpeed = isTestingSpeed,
                onToggleService = onToggleService,
                onTogglePause = onTogglePause,
                onRunSpeedTest = onRunSpeedTest
            )
        }

        // 5. Real-Time Waveform Chart
        StaggeredAnimatedItem(index = 4, modifier = Modifier.fillMaxWidth()) {
            LiveSpeedGraph(
                history = history,
                unit = settings.speedUnit
            )
        }

        // 6. Today's Data Usage Quick Summary
        StaggeredAnimatedItem(index = 5, modifier = Modifier.fillMaxWidth()) {
            TodayUsageGlanceCard(
                snapshot = snapshot
            )
        }

        // 7. Floating Bubble HUD Quick Card
        StaggeredAnimatedItem(index = 6, modifier = Modifier.fillMaxWidth()) {
            FloatingBubbleCard(
                snapshot = snapshot,
                unit = settings.speedUnit,
                isBubbleActive = isBubbleActive,
                onToggleBubble = onToggleBubble
            )
        }

        Spacer(modifier = Modifier.height(84.dp))
    }
}

@Suppress("LongMethod")
@Composable
private fun ActiveNetworkStatusBanner(
    snapshot: SpeedSnapshot
) {
    val colors = AppTheme.colors
    val networkTitle = if (snapshot.networkName.isNotEmpty() && snapshot.networkName != "Unknown") {
        snapshot.networkName
    } else {
        snapshot.networkType.title
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surfaceElevated)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(colors.accentPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = null,
                    tint = colors.accentGlow,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = networkTitle,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = "Type: ${snapshot.networkType.name} • IP: ${snapshot.ipAddress.ifEmpty { "Dynamic" }}",
                    fontSize = 10.5.sp,
                    color = colors.textSecondary,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        if (snapshot.pingMs >= 0) {
            val isPingGood = snapshot.pingMs <= PING_GOOD_THRESHOLD_MS
            val pingBgColor = if (isPingGood) {
                EmeraldSuccess.copy(alpha = 0.15f)
            } else {
                AmberWarning.copy(alpha = 0.15f)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(pingBgColor)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${snapshot.pingMs} ms",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = if (isPingGood) EmeraldGlow else AmberWarning
                )
            }
        }
    }
}

@Suppress("LongMethod", "MagicNumber")
@Composable
private fun PromotedNotificationBadgeCard(
    hasPermission: Boolean,
    onRequestPermission: () -> Unit,
    isServiceRunning: Boolean,
    isPaused: Boolean
) {
    val colors = AppTheme.colors
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    if (!hasPermission) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(AmberWarning.copy(alpha = 0.12f))
                .border(1.dp, AmberWarning.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .clickable { onRequestPermission() }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = AmberWarning,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Notification Permission Needed",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Enable to show live speed chip in Android 16 status bar",
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }
            }
            Text(
                text = "ALLOW",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = AmberWarning,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surfaceElevated)
                .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val dotColor = if (isServiceRunning && !isPaused) {
                    EmeraldSuccess.copy(alpha = pulseAlpha)
                } else {
                    colors.textTertiary
                }
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(dotColor, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Android 16 Promoted Live Notification",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Active",
                    tint = if (isServiceRunning && !isPaused) EmeraldSuccess else colors.textTertiary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (!isServiceRunning) "Stopped" else if (isPaused) "Paused" else "Running",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isServiceRunning && !isPaused) EmeraldGlow else colors.textSecondary
                )
            }
        }
    }
}

@Suppress("LongParameterList", "LongMethod", "MagicNumber")
@Composable
private fun ServiceControlsBar(
    isRunning: Boolean,
    isPaused: Boolean,
    isTestingSpeed: Boolean,
    onToggleService: () -> Unit,
    onTogglePause: () -> Unit,
    onRunSpeedTest: () -> Unit
) {
    val colors = AppTheme.colors

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Toggle Service (Start / Stop)
        Button(
            onClick = onToggleService,
            modifier = Modifier
                .weight(1.3f)
                .height(48.dp)
                .testTag("toggle_service_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRunning) RoseError.copy(alpha = 0.2f) else colors.accentPrimary,
                contentColor = if (isRunning) RoseError else colors.background
            ),
            border = if (isRunning) {
                androidx.compose.foundation.BorderStroke(1.dp, RoseError.copy(alpha = 0.5f))
            } else {
                null
            }
        ) {
            Icon(
                imageVector = Icons.Default.PowerSettingsNew,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isRunning) "Stop Service" else "Start Monitor",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Pause / Resume Button
        if (isRunning) {
            OutlinedButton(
                onClick = onTogglePause,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("toggle_pause_button"),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = if (isPaused) EmeraldSuccess else colors.cardBorder
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (isPaused) EmeraldGlow else colors.textPrimary
                )
            ) {
                Icon(
                    imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isPaused) "Resume" else "Pause",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Burst Test Button
        Button(
            onClick = onRunSpeedTest,
            enabled = !isTestingSpeed,
            modifier = Modifier
                .weight(1.1f)
                .height(48.dp)
                .testTag("burst_test_button"),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.accentSecondary.copy(alpha = 0.25f),
                contentColor = colors.accentGlow
            ),
            border = androidx.compose.foundation.BorderStroke(1.dp, colors.accentSecondary.copy(alpha = 0.5f))
        ) {
            if (isTestingSpeed) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = colors.accentGlow,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = colors.accentGlow,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Burst Test",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.accentGlow
                )
            }
        }
    }
}

@Composable
private fun TodayUsageGlanceCard(
    snapshot: SpeedSnapshot
) {
    val colors = AppTheme.colors
    val totalToday = snapshot.todayTotalBytes

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(colors.surfaceElevated)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Today's Data Consumption",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Text(
                text = SpeedFormatter.formatDataSize(totalToday),
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                color = colors.accentGlow,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Download Today
            MiniUsageStatChip(
                label = "Download",
                amount = SpeedFormatter.formatDataSize(snapshot.todayRxBytes),
                color = colors.accentPrimary,
                modifier = Modifier.weight(1f)
            )

            // Upload Today
            MiniUsageStatChip(
                label = "Upload",
                amount = SpeedFormatter.formatDataSize(snapshot.todayTxBytes),
                color = EmeraldSuccess,
                modifier = Modifier.weight(1f)
            )

            // Session Total
            MiniUsageStatChip(
                label = "Session",
                amount = SpeedFormatter.formatDataSize(snapshot.sessionTotalBytes),
                color = colors.accentSecondary,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MiniUsageStatChip(
    label: String,
    amount: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceHighlight)
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp, horizontal = 10.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary
                )
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = amount,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
