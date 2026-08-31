package com.onlasdan.netnet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onlasdan.netnet.data.SpeedSettings
import com.onlasdan.netnet.model.SpeedSnapshot
import com.onlasdan.netnet.ui.components.FloatingBubbleCard
import com.onlasdan.netnet.ui.components.HomeScreenWidgetCard
import com.onlasdan.netnet.ui.components.QuickSettingsTileCard
import com.onlasdan.netnet.ui.theme.AmberWarning
import com.onlasdan.netnet.ui.theme.AppTheme
import com.onlasdan.netnet.ui.theme.CyanGlow
import com.onlasdan.netnet.ui.theme.CyanPrimary
import com.onlasdan.netnet.ui.theme.PurpleAccent
import com.onlasdan.netnet.ui.theme.PurpleGlow

@Composable
fun ToolsAndOverlaysScreen(
    snapshot: SpeedSnapshot,
    settings: SpeedSettings,
    isServiceRunning: Boolean,
    isPaused: Boolean,
    isBubbleActive: Boolean,
    isTestingSpeed: Boolean,
    onToggleBubble: () -> Unit,
    onRunSpeedTest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Draggable Floating Speed Bubble HUD Card
        FloatingBubbleCard(
            snapshot = snapshot,
            unit = settings.speedUnit,
            isBubbleActive = isBubbleActive,
            onToggleBubble = onToggleBubble
        )

        // 2. Home Screen App Widget Showcase & Pin Card
        HomeScreenWidgetCard(
            snapshot = snapshot,
            settings = settings,
            isServiceRunning = isServiceRunning,
            isPaused = isPaused
        )

        // 3. Android Quick Settings Tile Card
        QuickSettingsTileCard(
            snapshot = snapshot,
            settings = settings,
            isServiceRunning = isServiceRunning,
            isPaused = isPaused
        )

        // 4. Speed Burst Test Playground Card
        SpeedBurstPlaygroundCard(
            isTestingSpeed = isTestingSpeed,
            onRunSpeedTest = onRunSpeedTest
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SpeedBurstPlaygroundCard(
    isTestingSpeed: Boolean,
    onRunSpeedTest: () -> Unit
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.accentSecondary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ElectricBolt,
                        contentDescription = "Burst Test",
                        tint = colors.accentSecondaryGlow,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Network Throughput Benchmark",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Generate controlled burst to test live indicator reaction",
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Initiates a fast parallel HTTP stream (~10MB chunk) from global edge servers to verify gauge acceleration and status bar chip response.",
            fontSize = 12.sp,
            color = colors.textSecondary,
            lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = onRunSpeedTest,
            enabled = !isTestingSpeed,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("tools_burst_test_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.accentSecondary,
                contentColor = colors.background
            )
        ) {
            if (isTestingSpeed) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Benchmarking Throughput...",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Run Burst Throughput Test",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
