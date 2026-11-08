package com.tiltclock.wear.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.tiltclock.wear.data.*

@Composable
fun WatchRing(steps: Measurement<Long>) {
    val palette = LocalWatchPalette.current
    val target = StepGoal.progress(steps)
    val progress by animateFloatAsState(target ?: 0f, tween(600, easing = FastOutSlowInEasing))
    Canvas(Modifier.fillMaxSize()) {
        val d = size.minDimension
        val inset = d * .1f
        drawCircle(palette.border, d * .4f, style = Stroke(d * .0125f))
        if (target != null) drawArc(palette.cyan, -90f, progress * 360f, false,
            Offset(inset, inset), Size(d * .8f, d * .8f), style = Stroke(d * .015f, cap = StrokeCap.Round))
    }
}
