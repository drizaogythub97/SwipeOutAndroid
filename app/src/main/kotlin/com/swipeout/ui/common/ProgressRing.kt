package com.swipeout.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.swipeout.ui.theme.Accent
import com.swipeout.ui.theme.Border
import com.swipeout.ui.theme.Keep

@Composable
fun ProgressRing(
    progress: Float,
    isComplete: Boolean,
    modifier: Modifier = Modifier,
) {
    val trackColor  = Border
    val activeColor = if (isComplete) Keep else Accent

    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.09f
        val radius      = (size.minDimension - strokeWidth) / 2f
        val topLeft     = Offset((size.width - radius * 2) / 2, (size.height - radius * 2) / 2)
        val arcSize     = Size(radius * 2, radius * 2)

        drawArc(color = trackColor,  startAngle = 0f,   sweepAngle = 360f,               useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(strokeWidth))
        drawArc(color = activeColor, startAngle = -90f, sweepAngle = 360f * progress.coerceIn(0f, 1f), useCenter = false, topLeft = topLeft, size = arcSize, style = Stroke(strokeWidth, cap = StrokeCap.Round))
    }
}
