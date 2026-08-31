package com.onlasdan.netnet.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CellWifi
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onlasdan.netnet.data.SpeedSettingsRepository
import com.onlasdan.netnet.model.DailyUsageRecord
import com.onlasdan.netnet.model.SpeedFormatter
import com.onlasdan.netnet.model.UsageAnalyticsSummary
import com.onlasdan.netnet.ui.theme.AppTheme
import com.onlasdan.netnet.ui.theme.CyanGlow
import com.onlasdan.netnet.ui.theme.CyanPrimary
import com.onlasdan.netnet.ui.theme.EmeraldGlow
import com.onlasdan.netnet.ui.theme.EmeraldSuccess
import kotlin.math.max

@Composable
fun DataUsageAnalyticsCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = AppTheme.colors
    val repo = remember { SpeedSettingsRepository.getInstance(context) }

    var selectedDays by remember { mutableIntStateOf(7) }
    var selectedRecordIndex by remember { mutableStateOf<Int?>(null) }

    val analytics: UsageAnalyticsSummary = remember(selectedDays) {
        repo.getHistoricalUsage(selectedDays)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surfaceElevated)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
            .animateContentSize()
    ) {
        // Header with 7D / 30D Segmented Switch
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(EmeraldSuccess.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = "Usage Analytics",
                        tint = EmeraldGlow,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Data Usage Analytics",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Wi-Fi vs Mobile Data Breakdown",
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }
            }

            // Segmented Period Selector (7D vs 30D)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.surfaceHighlight)
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(10.dp))
                    .padding(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedDays == 7) colors.accentPrimary else Color.Transparent)
                        .clickable {
                            selectedDays = 7
                            selectedRecordIndex = null
                        }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .testTag("filter_7_days"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "7 Days",
                        fontSize = 11.sp,
                        fontWeight = if (selectedDays == 7) FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedDays == 7) colors.background else colors.textSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedDays == 30) colors.accentPrimary else Color.Transparent)
                        .clickable {
                            selectedDays = 30
                            selectedRecordIndex = null
                        }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .testTag("filter_30_days"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "30 Days",
                        fontSize = 11.sp,
                        fontWeight = if (selectedDays == 30) FontWeight.Bold else FontWeight.Medium,
                        color = if (selectedDays == 30) colors.background else colors.textSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Total & Stats Breakdown
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Total Usage Box
            Column(
                modifier = Modifier
                    .weight(1.2f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surfaceHighlight)
                    .padding(10.dp)
            ) {
                Text(
                    text = "TOTAL ($selectedDays DAYS)",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = SpeedFormatter.formatDataSize(analytics.totalBytes),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = "Avg: ${SpeedFormatter.formatDataSize(analytics.dailyAverageBytes)}/day",
                    fontSize = 9.sp,
                    color = colors.textSecondary
                )
            }

            // Wi-Fi Box
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.accentPrimary.copy(alpha = 0.1f))
                    .border(1.dp, colors.accentPrimary.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = null,
                        tint = colors.accentGlow,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "WI-FI",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.accentGlow
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = SpeedFormatter.formatDataSize(analytics.totalWifiBytes),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = String.format("%.0f%% share", analytics.wifiPercentage),
                    fontSize = 9.sp,
                    color = colors.accentGlow
                )
            }

            // Cellular Box
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(EmeraldSuccess.copy(alpha = 0.1f))
                    .border(1.dp, EmeraldSuccess.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SignalCellularAlt,
                        contentDescription = null,
                        tint = EmeraldGlow,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "CELLULAR",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldGlow
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = SpeedFormatter.formatDataSize(analytics.totalCellBytes),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = String.format("%.0f%% share", analytics.cellPercentage),
                    fontSize = 9.sp,
                    color = EmeraldGlow
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Ratio Bar (Wi-Fi vs Cellular)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(colors.surfaceHighlight)
        ) {
            val wifiWeight = max(analytics.wifiPercentage / 100f, 0.01f)
            val cellWeight = max(analytics.cellPercentage / 100f, 0.01f)

            Box(
                modifier = Modifier
                    .weight(wifiWeight)
                    .height(6.dp)
                    .background(colors.accentPrimary)
            )
            Box(
                modifier = Modifier
                    .weight(cellWeight)
                    .height(6.dp)
                    .background(EmeraldSuccess)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Selected Day Details Callout (if tapped)
        val highlightedRecord = selectedRecordIndex?.let { idx ->
            analytics.records.getOrNull(idx)
        }

        AnimatedVisibility(
            visible = highlightedRecord != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            if (highlightedRecord != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.surfaceHighlight)
                        .border(1.dp, colors.accentPrimary.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = highlightedRecord.dateFormatted,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Total: " + SpeedFormatter.formatDataSize(highlightedRecord.totalBytes),
                            fontSize = 10.sp,
                            color = colors.textSecondary
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(colors.accentGlow)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Wi-Fi: " + SpeedFormatter.formatDataSize(highlightedRecord.wifiBytes),
                                fontSize = 10.sp,
                                color = colors.accentGlow,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldGlow)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Cell: " + SpeedFormatter.formatDataSize(highlightedRecord.cellBytes),
                                fontSize = 10.sp,
                                color = EmeraldGlow,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        // Interactive Stacked Bar Chart
        val records = analytics.records
        val maxDayBytes = max(analytics.peakDayBytes, 1024L * 1024L) // at least 1MB scale

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.chartBg)
                .padding(8.dp)
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(95.dp)
                    .pointerInput(records) {
                        detectTapGestures { offset ->
                            val count = records.size
                            if (count > 0) {
                                val slotWidth = size.width / count
                                val clickedIdx = (offset.x / slotWidth).toInt().coerceIn(0, count - 1)
                                selectedRecordIndex = if (selectedRecordIndex == clickedIdx) null else clickedIdx
                            }
                        }
                    }
            ) {
                val count = records.size
                if (count == 0) return@Canvas

                val slotWidth = size.width / count
                val barWidth = if (count <= 7) (slotWidth * 0.55f).coerceAtLeast(14f) else (slotWidth * 0.7f).coerceAtLeast(4f)
                val chartHeight = size.height

                // Draw Average Line
                if (analytics.dailyAverageBytes > 0) {
                    val avgY = chartHeight - (analytics.dailyAverageBytes.toFloat() / maxDayBytes) * (chartHeight * 0.85f)
                    drawLine(
                        color = Color(0x6694A3B8),
                        start = Offset(0f, avgY),
                        end = Offset(size.width, avgY),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                    )
                }

                records.forEachIndexed { index, record ->
                    val centerX = (index * slotWidth) + (slotWidth / 2f)
                    val left = centerX - (barWidth / 2f)

                    val isSelected = selectedRecordIndex == index

                    val wifiRatio = (record.wifiBytes.toFloat() / maxDayBytes).coerceIn(0f, 1f)
                    val cellRatio = (record.cellBytes.toFloat() / maxDayBytes).coerceIn(0f, 1f)

                    val wifiHeight = (wifiRatio * (chartHeight * 0.85f)).coerceAtLeast(if (record.wifiBytes > 0) 4f else 0f)
                    val cellHeight = (cellRatio * (chartHeight * 0.85f)).coerceAtLeast(if (record.cellBytes > 0) 4f else 0f)

                    // Draw Background Pillar
                    drawRoundRect(
                        color = if (isSelected) colors.accentPrimary.copy(alpha = 0.25f) else Color(0x15FFFFFF),
                        topLeft = Offset(left, 0f),
                        size = Size(barWidth, chartHeight),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )

                    // Wi-Fi (Bottom Segment)
                    if (wifiHeight > 0) {
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(colors.accentGlow, colors.accentPrimary)
                            ),
                            topLeft = Offset(left, chartHeight - wifiHeight),
                            size = Size(barWidth, wifiHeight),
                            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                        )
                    }

                    // Cellular (Stacked Top Segment)
                    if (cellHeight > 0) {
                        val cellTop = chartHeight - wifiHeight - cellHeight
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                colors = listOf(EmeraldGlow, EmeraldSuccess)
                            ),
                            topLeft = Offset(left, cellTop.coerceAtLeast(0f)),
                            size = Size(barWidth, cellHeight),
                            cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                        )
                    }

                    // Selection Glow outline
                    if (isSelected) {
                        drawRoundRect(
                            color = Color.White,
                            topLeft = Offset(left - 1.5f, 0f),
                            size = Size(barWidth + 3f, chartHeight),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.5.dp.toPx())
                        )
                    }
                }
            }

            // X-Axis Labels for 7 Days
            if (records.size <= 7) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    records.forEachIndexed { idx, rec ->
                        Text(
                            text = rec.dayShortLabel,
                            fontSize = 8.sp,
                            fontWeight = if (selectedRecordIndex == idx) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedRecordIndex == idx) colors.accentGlow else Color(0xFF64748B),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Chart Footer & Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Legend
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(colors.accentPrimary)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Wi-Fi",
                        fontSize = 10.sp,
                        color = colors.textSecondary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(EmeraldSuccess)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Mobile Data",
                        fontSize = 10.sp,
                        color = colors.textSecondary
                    )
                }
            }

            Text(
                text = "Peak: " + analytics.peakDayLabel,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = colors.textSecondary
            )
        }
    }
}
