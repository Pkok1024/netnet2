package com.onlasdan.netnet.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lan
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onlasdan.netnet.model.ConnectionQualityRating
import com.onlasdan.netnet.model.DetailedNetworkDiagnostics
import com.onlasdan.netnet.model.NetworkInterfaceItem
import com.onlasdan.netnet.model.NetworkType
import com.onlasdan.netnet.model.PingDiagnosticState
import com.onlasdan.netnet.model.PingDiagnosticStatus
import com.onlasdan.netnet.model.SpeedFormatter
import com.onlasdan.netnet.model.SpeedSnapshot
import com.onlasdan.netnet.ui.theme.AmberWarning
import com.onlasdan.netnet.ui.theme.AppTheme
import com.onlasdan.netnet.ui.theme.CyanGlow
import com.onlasdan.netnet.ui.theme.CyanPrimary
import com.onlasdan.netnet.ui.theme.EmeraldGlow
import com.onlasdan.netnet.ui.theme.EmeraldSuccess
import com.onlasdan.netnet.ui.theme.RoseError

@Composable
fun NetworkInfoCard(
    snapshot: SpeedSnapshot,
    diagnostics: DetailedNetworkDiagnostics,
    pingState: PingDiagnosticState = PingDiagnosticState(),
    onRunPingDiagnostic: () -> Unit = {},
    onCancelPingDiagnostic: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    var showAllInterfaces by remember { mutableStateOf(false) }

    val activeType = diagnostics.networkType
    val isWifi = activeType == NetworkType.WIFI
    val isCellular = activeType == NetworkType.CELLULAR
    val isVpn = diagnostics.isVpn || activeType == NetworkType.VPN

    val netIcon: ImageVector = when (activeType) {
        NetworkType.WIFI -> Icons.Default.Wifi
        NetworkType.CELLULAR -> Icons.Default.CellTower
        NetworkType.ETHERNET -> Icons.Default.Lan
        NetworkType.VPN -> Icons.Default.VpnKey
        NetworkType.OFFLINE -> Icons.Default.WifiOff
    }

    val statusColor = when {
        diagnostics.isValidated -> EmeraldSuccess
        activeType != NetworkType.OFFLINE -> AmberWarning
        else -> RoseError
    }

    val pingColor = when {
        snapshot.pingMs in 0..60 -> EmeraldSuccess
        snapshot.pingMs in 61..130 -> AmberWarning
        snapshot.pingMs > 130 -> RoseError
        else -> colors.textTertiary
    }

    val pingText = if (snapshot.pingMs >= 0) "${snapshot.pingMs} ms" else "Probing..."

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(colors.surfaceElevated)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
            .animateContentSize()
            .testTag("network_diagnostics_card")
    ) {
        // --- 1. Top Header: Network Identity & Ping ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (isWifi) CyanPrimary.copy(alpha = 0.15f)
                            else if (isCellular) Color(0xFFA855F7).copy(alpha = 0.15f)
                            else colors.surfaceHighlight,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = netIcon,
                        contentDescription = "Active Network",
                        tint = if (isWifi) CyanGlow else if (isCellular) Color(0xFFA855F7) else colors.textPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = diagnostics.networkName.ifEmpty { snapshot.networkName },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        if (isVpn) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF3B82F6).copy(alpha = 0.2f))
                                    .padding(horizontal = 5.dp, vertical = 2.dp)
                            ) {
                                Text("VPN", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF60A5FA))
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        // Interface Tag
                        Text(
                            text = "IF: ${diagnostics.activeInterfaceName}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold,
                            color = CyanGlow
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "•",
                            fontSize = 11.sp,
                            color = colors.textTertiary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (diagnostics.isMetered) "Metered (Data-Cap)" else "Unmetered (Flat)",
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            // Latency / Ping Pill
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(colors.surfaceHighlight)
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(pingColor, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = pingText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = pingColor
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- 2. Connection Validation & Mode Banner ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(statusColor.copy(alpha = 0.12f))
                .border(1.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = if (diagnostics.isValidated) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = diagnostics.connectionStatus,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.surfaceHighlight)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = if (isWifi) "Wi-Fi Interface" else if (isCellular) "Mobile Data" else "Active IF",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isWifi) CyanGlow else if (isCellular) Color(0xFFA855F7) else colors.textSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // --- 3. Wi-Fi vs Mobile Specific Diagnostics Grid ---
        if (isWifi) {
            // Wi-Fi Specific Telemetry
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DiagMetricTile(
                    label = "STANDARD",
                    value = diagnostics.wifiStandard ?: "802.11 Auto",
                    sub = diagnostics.wifiBand ?: "Dynamic Band",
                    icon = Icons.Default.Wifi,
                    modifier = Modifier.weight(1f)
                )
                DiagMetricTile(
                    label = "SIGNAL LEVEL",
                    value = if (diagnostics.signalStrengthDbm != null) "${diagnostics.signalStrengthDbm} dBm" else "${diagnostics.signalLevelPercent ?: 85}%",
                    sub = "Link: ${if (snapshot.linkSpeedMbps > 0) "${snapshot.linkSpeedMbps} Mbps" else "Dynamic"}",
                    icon = Icons.Default.SignalCellularAlt,
                    modifier = Modifier.weight(1f)
                )
            }
        } else if (isCellular) {
            // Cellular Specific Telemetry
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DiagMetricTile(
                    label = "GENERATION",
                    value = diagnostics.cellularGeneration ?: "4G / LTE",
                    sub = diagnostics.cellularOperatorName ?: "Carrier Network",
                    icon = Icons.Default.CellTower,
                    modifier = Modifier.weight(1f)
                )
                DiagMetricTile(
                    label = "MAX BANDWIDTH",
                    value = "${diagnostics.downstreamBandwidthMbps.coerceAtLeast(snapshot.linkSpeedMbps)} Mbps",
                    sub = "Up: ${diagnostics.upstreamBandwidthMbps} Mbps",
                    icon = Icons.Default.Speed,
                    modifier = Modifier.weight(1f)
                )
            }
        } else {
            // General / Ethernet / Offline
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DiagMetricTile(
                    label = "LINK SPEED",
                    value = if (snapshot.linkSpeedMbps > 0) "${snapshot.linkSpeedMbps} Mbps" else "Dynamic",
                    sub = "Downstream Bandwidth",
                    icon = Icons.Default.Speed,
                    modifier = Modifier.weight(1f)
                )
                DiagMetricTile(
                    label = "TODAY USAGE",
                    value = SpeedFormatter.formatDataSize(snapshot.todayTotalBytes),
                    sub = "Combined Total",
                    icon = Icons.Default.Public,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- 4. Addressing & Gateway Specs (IPv4, IPv6, Gateway, DNS) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colors.surfaceHighlight)
                .border(1.dp, colors.cardBorder, RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Local IPv4", fontSize = 10.sp, color = colors.textTertiary)
                    Text(
                        text = diagnostics.ipv4Address.ifEmpty { snapshot.ipAddress },
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        color = colors.textPrimary
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("Gateway Router", fontSize = 10.sp, color = colors.textTertiary)
                    Text(
                        text = diagnostics.gatewayAddress ?: "Auto/Assigned",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace,
                        color = colors.textPrimary
                    )
                }
            }

            if (diagnostics.dnsServers.isNotEmpty() || diagnostics.ipv6Address != null) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = colors.cardBorder, thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("DNS Server(s)", fontSize = 10.sp, color = colors.textTertiary)
                        Text(
                            text = if (diagnostics.dnsServers.isNotEmpty()) diagnostics.dnsServers.joinToString(", ") else "System Default",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Monospace,
                            color = colors.textSecondary
                        )
                    }

                    if (diagnostics.ipv6Address != null) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("IPv6 Address", fontSize = 10.sp, color = colors.textTertiary)
                            Text(
                                text = diagnostics.ipv6Address ?: "—",
                                fontSize = 11.sp,
                                maxLines = 1,
                                fontFamily = FontFamily.Monospace,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // --- 5. Interactive Network Diagnostic & Ping Latency Test Suite ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(colors.surfaceHighlight)
                .border(1.dp, colors.cardBorder, RoundedCornerShape(14.dp))
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(CyanPrimary.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NetworkCheck,
                            contentDescription = null,
                            tint = CyanGlow,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(9.dp))
                    Column {
                        Text(
                            text = "Ping & Latency Diagnostic",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Test gateway, DNS & multi-target quality",
                            fontSize = 10.5.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                if (pingState.status == PingDiagnosticStatus.RUNNING) {
                    OutlinedButton(
                        onClick = onCancelPingDiagnostic,
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("cancel_ping_diagnostic_button"),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseError),
                        border = androidx.compose.foundation.BorderStroke(1.dp, RoseError.copy(alpha = 0.5f))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cancel", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onRunPingDiagnostic,
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("run_ping_diagnostic_button"),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (pingState.status == PingDiagnosticStatus.COMPLETED) colors.surfaceElevated else CyanPrimary,
                            contentColor = if (pingState.status == PingDiagnosticStatus.COMPLETED) CyanGlow else colors.background
                        ),
                        border = if (pingState.status == PingDiagnosticStatus.COMPLETED) androidx.compose.foundation.BorderStroke(1.dp, CyanGlow.copy(alpha = 0.5f)) else null
                    ) {
                        Icon(
                            imageVector = if (pingState.status == PingDiagnosticStatus.COMPLETED) Icons.Default.Refresh else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (pingState.status == PingDiagnosticStatus.COMPLETED) "Retest" else "Run Ping Test",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Running state progress bar
            if (pingState.status == PingDiagnosticStatus.RUNNING) {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { pingState.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = CyanGlow,
                    trackColor = colors.surfaceElevated
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = pingState.currentStep,
                        fontSize = 11.sp,
                        color = CyanGlow,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${(pingState.progress * 100).toInt()}%",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = colors.textSecondary
                    )
                }
            }

            // Completed State Display
            if (pingState.status == PingDiagnosticStatus.COMPLETED && pingState.result != null) {
                val result = pingState.result
                Spacer(modifier = Modifier.height(12.dp))

                val qualityColor = when (result.qualityRating) {
                    ConnectionQualityRating.EXCELLENT -> EmeraldSuccess
                    ConnectionQualityRating.GOOD -> CyanGlow
                    ConnectionQualityRating.FAIR -> AmberWarning
                    ConnectionQualityRating.POOR,
                    ConnectionQualityRating.UNSTABLE,
                    ConnectionQualityRating.OFFLINE -> RoseError
                }

                // Quality Badge & Status
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(qualityColor.copy(alpha = 0.12f))
                        .border(1.dp, qualityColor.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (result.qualityRating == ConnectionQualityRating.EXCELLENT || result.qualityRating == ConnectionQualityRating.GOOD) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = qualityColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = result.qualityRating.label.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = qualityColor
                        )
                        Text(
                            text = result.summaryAdvice,
                            fontSize = 11.sp,
                            color = colors.textPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 4-Quadrant Metric Tiles: Avg Ping, Min/Max, Jitter, Packet Loss
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PingMetricCard(
                        title = "AVG LATENCY",
                        value = "${result.avgLatencyMs} ms",
                        sub = if (result.avgLatencyMs <= 40) "Ultra Low" else if (result.avgLatencyMs <= 90) "Normal" else "High",
                        highlightColor = qualityColor,
                        modifier = Modifier.weight(1f)
                    )
                    PingMetricCard(
                        title = "MIN / MAX",
                        value = "${result.minLatencyMs}/${result.maxLatencyMs}",
                        sub = "Range (ms)",
                        highlightColor = colors.textPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val jitterColor = if (result.jitterMs <= 15) EmeraldSuccess else if (result.jitterMs <= 35) AmberWarning else RoseError
                    PingMetricCard(
                        title = "JITTER",
                        value = "${result.jitterMs} ms",
                        sub = if (result.jitterMs <= 15) "Stable (<15ms)" else "Fluctuating",
                        highlightColor = jitterColor,
                        modifier = Modifier.weight(1f)
                    )
                    val lossColor = if (result.packetLossPercent == 0) EmeraldSuccess else if (result.packetLossPercent <= 10) AmberWarning else RoseError
                    PingMetricCard(
                        title = "PACKET LOSS",
                        value = "${result.packetLossPercent}%",
                        sub = if (result.packetLossPercent == 0) "0 Drops (Perfect)" else "${result.packetLossPercent}% Failed",
                        highlightColor = lossColor,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Target Breakdown List
                Text(
                    text = "Endpoint Breakdown & Response Times",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(colors.surfaceElevated)
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    result.targets.forEach { target ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(if (target.isSuccess) EmeraldSuccess else RoseError, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = target.name,
                                    fontSize = 11.5.sp,
                                    color = colors.textPrimary,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "(${target.host.removePrefix("https://").substringBefore("/")})",
                                    fontSize = 10.sp,
                                    color = colors.textTertiary,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            if (target.isSuccess) {
                                Text(
                                    text = "${target.latencyMs} ms",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (target.latencyMs <= 50) EmeraldGlow else if (target.latencyMs <= 100) AmberWarning else RoseError
                                )
                            } else {
                                Text(
                                    text = "TIMEOUT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RoseError
                                )
                            }
                        }
                    }
                }
            } else if (pingState.status == PingDiagnosticStatus.FAILED) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Diagnostic Error: ${pingState.errorMessage ?: "Network probe unreachable"}",
                    fontSize = 11.sp,
                    color = RoseError
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // --- 6. Toggleable All Network Interfaces Explorer ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .clickable { showAllInterfaces = !showAllInterfaces }
                .padding(vertical = 4.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SettingsEthernet,
                    contentDescription = null,
                    tint = CyanGlow,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (showAllInterfaces) "Hide Network Interfaces List" else "View All Network Interfaces (${diagnostics.interfaceList.size} found)",
                    fontSize = 11.5.sp,
                    color = CyanGlow,
                    fontWeight = FontWeight.Medium
                )
            }
            Icon(
                imageVector = if (showAllInterfaces) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = "Expand Interfaces",
                tint = CyanGlow,
                modifier = Modifier.size(16.dp)
            )
        }

        AnimatedVisibility(
            visible = showAllInterfaces,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0A0E18))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Linux Kernel & System Network Interfaces",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )

                diagnostics.interfaceList.forEach { iface ->
                    InterfaceRowItem(iface)
                }
            }
        }
    }
}

@Composable
private fun DiagMetricTile(
    label: String,
    value: String,
    sub: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(colors.surfaceHighlight)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(14.dp))
            .padding(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = label,
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textSecondary
            )
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CyanGlow,
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )
        Text(
            text = sub,
            fontSize = 10.sp,
            color = colors.textSecondary
        )
    }
}

@Composable
private fun InterfaceRowItem(iface: NetworkInterfaceItem) {
    val colors = AppTheme.colors

    val statusBadgeColor = when {
        iface.isActiveDefault -> EmeraldSuccess
        iface.isUp -> CyanGlow
        else -> colors.textTertiary
    }

    val statusText = when {
        iface.isActiveDefault -> "ACTIVE DEFAULT"
        iface.isUp -> "UP"
        else -> "STANDBY"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (iface.isActiveDefault) EmeraldSuccess.copy(alpha = 0.08f) else colors.surfaceElevated)
            .border(
                1.dp,
                if (iface.isActiveDefault) EmeraldSuccess.copy(alpha = 0.35f) else colors.cardBorder,
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 10.dp, vertical = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = iface.name,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = if (iface.isActiveDefault) EmeraldGlow else colors.textPrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = iface.displayName.substringBefore("(").trim(),
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )
            }
            if (iface.ipAddresses.isNotEmpty()) {
                Text(
                    text = iface.ipAddresses.joinToString(", "),
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = colors.textTertiary
                )
            } else {
                Text(
                    text = "MTU: ${iface.mtu} • No IPv4/v6 bound",
                    fontSize = 10.sp,
                    color = colors.textTertiary
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(statusBadgeColor.copy(alpha = 0.15f))
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Text(
                text = statusText,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = statusBadgeColor
            )
        }
    }
}

@Composable
private fun PingMetricCard(
    title: String,
    value: String,
    sub: String,
    highlightColor: Color,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(colors.surfaceElevated)
            .border(1.dp, colors.cardBorder, RoundedCornerShape(10.dp))
            .padding(8.dp)
    ) {
        Text(
            text = title,
            fontSize = 9.5.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textSecondary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = highlightColor
        )
        Text(
            text = sub,
            fontSize = 9.5.sp,
            color = colors.textTertiary
        )
    }
}

