package com.onlasdan.netnet.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.onlasdan.netnet.model.SpeedFormatter
import com.onlasdan.netnet.model.SpeedSnapshot
import com.onlasdan.netnet.model.SpeedUnit
import com.onlasdan.netnet.ui.theme.AppTheme
import com.onlasdan.netnet.ui.theme.CyanGlow
import com.onlasdan.netnet.ui.theme.CyanPrimary
import com.onlasdan.netnet.ui.theme.EmeraldGlow
import com.onlasdan.netnet.ui.theme.EmeraldSuccess
import com.onlasdan.netnet.ui.theme.PurpleAccent
import com.onlasdan.netnet.ui.theme.PurpleGlow
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.sin

@Composable
fun SpeedGauge(
    snapshot: SpeedSnapshot,
    unit: SpeedUnit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    val maxReferenceBytes = max(10 * 1024 * 1024L, max(snapshot.peakDownloadBytesPerSec, snapshot.peakUploadBytesPerSec))
    
    // Logarithmic gauge fraction for visual dynamic range
    val dlFraction = calculateGaugeFraction(snapshot.downloadBytesPerSec, maxReferenceBytes)
    val ulFraction = calculateGaugeFraction(snapshot.uploadBytesPerSec, maxReferenceBytes)

    val animatedDlAngle by animateFloatAsState(
        targetValue = dlFraction,
        animationSpec = tween(durationMillis = 350),
        label = "dlAngle"
    )

    val animatedUlAngle by animateFloatAsState(
        targetValue = ulFraction,
        animationSpec = tween(durationMillis = 350),
        label = "ulAngle"
    )

    val (dlVal, dlUnit) = SpeedFormatter.formatSpeedValue(snapshot.downloadBytesPerSec, unit)
    val (ulVal, ulUnit) = SpeedFormatter.formatSpeedValue(snapshot.uploadBytesPerSec, unit)

    Box(
        modifier = modifier.size(240.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val innerStrokeWidth = 8.dp.toPx()
            val trackColor = colors.gaugeTrack
            val innerTrackColor = colors.gaugeInnerTrack
            val padding = strokeWidth / 2 + 10.dp.toPx()
            val arcSize = Size(size.width - padding * 2, size.height - padding * 2)
            val topLeft = Offset(padding, padding)

            val startAngle = 135f
            val sweepTotal = 270f

            // Background track
            drawArc(
                color = trackColor,
                startAngle = startAngle,
                sweepAngle = sweepTotal,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Download Arc (Outer)
            if (animatedDlAngle > 0.005f) {
                val dlSweep = (animatedDlAngle * sweepTotal).coerceIn(1f, sweepTotal)
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to colors.accentPrimary,
                        0.5f to colors.accentGlow,
                        1.0f to colors.accentSuccess,
                        center = Offset(size.width / 2, size.height / 2)
                    ),
                    startAngle = startAngle,
                    sweepAngle = dlSweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }

            // Upload Arc (Inner)
            val innerPadding = padding + strokeWidth + 4.dp.toPx()
            val innerArcSize = Size(size.width - innerPadding * 2, size.height - innerPadding * 2)
            val innerTopLeft = Offset(innerPadding, innerPadding)

            drawArc(
                color = innerTrackColor,
                startAngle = startAngle,
                sweepAngle = sweepTotal,
                useCenter = false,
                topLeft = innerTopLeft,
                size = innerArcSize,
                style = Stroke(width = innerStrokeWidth, cap = StrokeCap.Round)
            )

            if (animatedUlAngle > 0.005f) {
                val ulSweep = (animatedUlAngle * sweepTotal).coerceIn(1f, sweepTotal)
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to colors.accentSecondary,
                        1.0f to colors.accentSecondaryGlow,
                        center = Offset(size.width / 2, size.height / 2)
                    ),
                    startAngle = startAngle,
                    sweepAngle = ulSweep,
                    useCenter = false,
                    topLeft = innerTopLeft,
                    size = innerArcSize,
                    style = Stroke(width = innerStrokeWidth, cap = StrokeCap.Round)
                )
            }

            // Ticks around gauge
            val numTicks = 9
            val radius = (size.width - padding * 2) / 2
            val center = Offset(size.width / 2, size.height / 2)
            for (i in 0 until numTicks) {
                val angleDeg = startAngle + (i.toFloat() / (numTicks - 1)) * sweepTotal
                val angleRad = Math.toRadians(angleDeg.toDouble())
                val tickStart = Offset(
                    (center.x + (radius + 8.dp.toPx()) * cos(angleRad)).toFloat(),
                    (center.y + (radius + 8.dp.toPx()) * sin(angleRad)).toFloat()
                )
                val tickEnd = Offset(
                    (center.x + (radius + 14.dp.toPx()) * cos(angleRad)).toFloat(),
                    (center.y + (radius + 14.dp.toPx()) * sin(angleRad)).toFloat()
                )
                drawCircle(
                    color = if (i % 2 == 0) colors.accentGlow.copy(alpha = 0.6f) else colors.textTertiary.copy(alpha = 0.3f),
                    radius = 2.dp.toPx(),
                    center = tickEnd
                )
            }
        }

        // Center Speed Display
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "DOWNLOAD",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = colors.accentGlow,
                letterSpacing = 1.5.sp
            )
            Text(
                text = dlVal,
                fontSize = 38.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.SansSerif,
                color = colors.textPrimary
            )
            Text(
                text = dlUnit,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textSecondary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "↑ $ulVal $ulUnit",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = colors.accentSecondaryGlow
            )
        }
    }
}

private fun calculateGaugeFraction(bytesPerSec: Long, maxReferenceBytes: Long): Float {
    if (bytesPerSec <= 0) return 0f
    val logSpeed = log10(max(1.0, bytesPerSec.toDouble()))
    val logMax = log10(max(10.0, maxReferenceBytes.toDouble()))
    return (logSpeed / logMax).toFloat().coerceIn(0f, 1f)
}
