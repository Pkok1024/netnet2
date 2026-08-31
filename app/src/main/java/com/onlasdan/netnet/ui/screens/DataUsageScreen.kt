package com.onlasdan.netnet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onlasdan.netnet.data.SpeedSettingsRepository
import com.onlasdan.netnet.model.SpeedFormatter
import com.onlasdan.netnet.model.SpeedSnapshot
import com.onlasdan.netnet.model.SpeedUnit
import com.onlasdan.netnet.ui.components.DataUsageAnalyticsCard
import com.onlasdan.netnet.ui.components.StaggeredAnimatedItem
import com.onlasdan.netnet.ui.theme.AppTheme
import com.onlasdan.netnet.ui.theme.CyanGlow
import com.onlasdan.netnet.ui.theme.CyanPrimary
import com.onlasdan.netnet.ui.theme.PurpleAccent
import com.onlasdan.netnet.ui.theme.PurpleGlow
import com.onlasdan.netnet.ui.theme.RoseError

@Composable
fun DataUsageScreen(
    snapshot: SpeedSnapshot,
    speedUnit: SpeedUnit,
    onResetTodayUsage: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = AppTheme.colors
    var showResetConfirmDialog by remember { mutableStateOf(false) }

    val repo = remember { SpeedSettingsRepository(context) }
    val todayRecord = remember(snapshot.todayTotalBytes) {
        repo.getHistoricalUsage(1).records.firstOrNull()
    }
    val todayWifiBytes = todayRecord?.wifiBytes ?: (snapshot.todayTotalBytes * 75 / 100)
    val todayCellBytes = todayRecord?.cellBytes ?: (snapshot.todayTotalBytes * 25 / 100)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Data Consumption Overview Banner
        StaggeredAnimatedItem(index = 0, modifier = Modifier.fillMaxWidth()) {
            DataSummaryCardsRow(snapshot = snapshot)
        }

        // 2. Wi-Fi vs Cellular Breakdown Card
        StaggeredAnimatedItem(index = 1, modifier = Modifier.fillMaxWidth()) {
            WifiVsCellularCard(
                totalBytes = snapshot.todayTotalBytes,
                wifiBytes = todayWifiBytes,
                cellBytes = todayCellBytes
            )
        }

        // 3. 7-Day & 30-Day Interactive Analytics Bar Chart
        StaggeredAnimatedItem(index = 2, modifier = Modifier.fillMaxWidth()) {
            DataUsageAnalyticsCard()
        }

        // 4. Active Session Breakdown Card
        StaggeredAnimatedItem(index = 3, modifier = Modifier.fillMaxWidth()) {
            ActiveSessionCard(
                snapshot = snapshot,
                speedUnit = speedUnit
            )
        }

        // 5. Data Actions Card (Reset Today's Usage)
        StaggeredAnimatedItem(index = 4, modifier = Modifier.fillMaxWidth()) {
            DataActionsCard(
                onResetClick = { showResetConfirmDialog = true }
            )
        }

        Spacer(modifier = Modifier.height(84.dp))
    }

    if (showResetConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showResetConfirmDialog = false },
            containerColor = colors.surfaceElevated,
            title = {
                Text(
                    text = "Reset Today's Data Usage?",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = "This will zero out today's tracked Wi-Fi and Cellular counters for the current day. Historical logs from previous days will be preserved.",
                    fontSize = 14.sp,
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onResetTodayUsage()
                        showResetConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoseError)
                ) {
                    Text("Reset Today", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirmDialog = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            }
        )
    }
}

@Composable
private fun DataSummaryCardsRow(snapshot: SpeedSnapshot) {
    val colors = AppTheme.colors

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Today Card
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surfaceElevated)
                .border(1.dp, colors.accentPrimary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "TODAY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.accentGlow
                )
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = colors.accentGlow,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = SpeedFormatter.formatDataSize(snapshot.todayTotalBytes),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Total Consumed",
                fontSize = 11.sp,
                color = colors.textSecondary
            )
        }

        // Session Card
        Column(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(16.dp))
                .background(colors.surfaceElevated)
                .border(1.dp, colors.accentSecondary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "SESSION",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.accentSecondaryGlow
                )
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = colors.accentSecondaryGlow,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = SpeedFormatter.formatDataSize(snapshot.sessionTotalBytes),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Since App Launch",
                fontSize = 11.sp,
                color = colors.textSecondary
            )
        }
    }
}

@Composable
private fun WifiVsCellularCard(
    totalBytes: Long,
    wifiBytes: Long,
    cellBytes: Long
) {
    val colors = AppTheme.colors
    val total = (wifiBytes + cellBytes).coerceAtLeast(1L)
    val wifiFraction = (wifiBytes.toFloat() / total).coerceIn(0f, 1f)
    val mobileFraction = (cellBytes.toFloat() / total).coerceIn(0f, 1f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surfaceElevated)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Interface Distribution",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Text(
                text = "Today",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textSecondary
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Multi-color distribution bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(colors.surfaceHighlight)
        ) {
            if (wifiFraction > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(wifiFraction.coerceAtLeast(0.01f))
                        .background(colors.accentPrimary)
                )
            }
            if (mobileFraction > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(mobileFraction.coerceAtLeast(0.01f))
                        .background(colors.accentSecondary)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Breakdown items
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Wi-Fi
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surfaceHighlight)
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(colors.accentPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = "Wi-Fi",
                        tint = colors.accentGlow,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Wi-Fi (${(wifiFraction * 100).toInt()}%)",
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                    Text(
                        text = SpeedFormatter.formatDataSize(wifiBytes),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }
            }

            // Mobile
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surfaceHighlight)
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(colors.accentSecondary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CellTower,
                        contentDescription = "Cellular",
                        tint = colors.accentSecondaryGlow,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Cellular (${(mobileFraction * 100).toInt()}%)",
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                    Text(
                        text = SpeedFormatter.formatDataSize(cellBytes),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun ActiveSessionCard(
    snapshot: SpeedSnapshot,
    speedUnit: SpeedUnit
) {
    val colors = AppTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surfaceElevated)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Session Speeds & Peaks",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            Text(
                text = "Live Telemetry",
                fontSize = 11.sp,
                color = colors.accentGlow,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Download Peak
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surfaceHighlight)
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = colors.accentGlow,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Peak Download",
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = SpeedFormatter.formatSpeed(snapshot.peakDownloadBytesPerSec, speedUnit),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.accentGlow,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Upload Peak
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surfaceHighlight)
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = colors.accentSecondaryGlow,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Peak Upload",
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = SpeedFormatter.formatSpeed(snapshot.peakUploadBytesPerSec, speedUnit),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.accentSecondaryGlow,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}

@Composable
private fun DataActionsCard(
    onResetClick: () -> Unit
) {
    val colors = AppTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colors.surfaceElevated)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(16.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Reset Today's Counters",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary
            )
            Text(
                text = "Zeros out today's logged data consumption",
                fontSize = 11.sp,
                color = colors.textSecondary
            )
        }

        OutlinedButton(
            onClick = onResetClick,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseError),
            border = androidx.compose.foundation.BorderStroke(1.dp, RoseError.copy(alpha = 0.5f)),
            modifier = Modifier.testTag("reset_today_usage_button")
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Reset",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
