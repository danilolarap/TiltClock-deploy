package com.tiltclock.wear.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*

@Composable
fun ScreenShine() {
    Canvas(Modifier.fillMaxSize()) {
        drawRect(Brush.linearGradient(listOf(Color.White.copy(alpha=.18f),Color.Transparent,Color.Transparent),
            Offset.Zero,Offset(size.width,size.height)))
    }
}
