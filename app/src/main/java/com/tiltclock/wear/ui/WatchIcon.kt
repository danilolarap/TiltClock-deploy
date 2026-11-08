package com.tiltclock.wear.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun WatchIcon(kind: String, modifier: Modifier = Modifier, cyan: Boolean = false) {
    val p=LocalWatchPalette.current
    Canvas(modifier.size(24.dp)) {
        val c=if(cyan) p.cyan else p.text
        val u=size.minDimension/24
        fun line(x: Float,y: Float,x2: Float,y2: Float) = drawLine(c,Offset(x*u,y*u),Offset(x2*u,y2*u),2*u,StrokeCap.Round)
        when(kind) {
            "pencil" -> {
                drawPath(Path().apply { moveTo(5*u,15*u); lineTo(16*u,4*u); lineTo(20*u,8*u); lineTo(9*u,19*u); lineTo(4*u,20*u); close() },c,style=Stroke(1.7f*u))
                line(14f,6f,18f,10f)
            }
            "calendar" -> { drawRoundRect(c,Offset(4*u,5*u),Size(16*u,16*u),CornerRadius(2*u),style=Stroke(1.7f*u)); line(4f,10f,20f,10f); line(8f,3f,8f,7f); line(16f,3f,16f,7f) }
            "steps" -> { drawCircle(c,2*u,Offset(14*u,4*u)); line(12f,8f,9f,14f); line(12f,8f,16f,12f); line(9f,14f,14f,17f); line(14f,17f,14f,22f); line(9f,14f,5f,21f); line(12f,8f,7f,10f) }
            "pause" -> { line(9f,6f,9f,18f); line(15f,6f,15f,18f) }
            "play" -> drawPath(Path().apply { moveTo(8*u,5*u); lineTo(19*u,12*u); lineTo(8*u,19*u); close() },c)
        }
    }
}
