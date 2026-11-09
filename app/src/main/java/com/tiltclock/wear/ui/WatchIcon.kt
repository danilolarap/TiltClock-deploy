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
            "previous" -> { line(14f,6f,8f,12f); line(8f,12f,14f,18f) }
            "next", "apply" -> { line(9f,6f,15f,12f); line(15f,12f,9f,18f) }
            "close" -> { line(7f,7f,17f,17f); line(17f,7f,7f,17f) }
            "check" -> { line(5f,12f,10f,17f); line(10f,17f,19f,7f) }
            "theme" -> {
                drawCircle(c,8*u,Offset(12*u,12*u),style=Stroke(1.7f*u))
                drawArc(c,-90f,180f,true,Offset(4*u,4*u),Size(16*u,16*u))
            }
            "sun" -> {
                drawCircle(c,4*u,Offset(12*u,12*u),style=Stroke(1.7f*u))
                line(12f,2f,12f,4f); line(12f,20f,12f,22f); line(2f,12f,4f,12f); line(20f,12f,22f,12f)
                line(5f,5f,6.5f,6.5f); line(17.5f,17.5f,19f,19f); line(5f,19f,6.5f,17.5f); line(17.5f,6.5f,19f,5f)
            }
            "moon" -> drawPath(Path().apply {
                moveTo(15*u,3*u); cubicTo(3*u,0f,0f,18*u,12*u,21*u)
                cubicTo(17*u,22*u,21*u,18*u,21*u,14*u)
                cubicTo(13*u,18*u,7*u,8*u,15*u,3*u); close()
            },c,style=Stroke(1.7f*u))
            "language" -> {
                drawCircle(c,9*u,Offset(12*u,12*u),style=Stroke(1.7f*u))
                drawOval(c,Offset(8*u,3*u),Size(8*u,18*u),style=Stroke(1.4f*u))
                line(3f,12f,21f,12f)
            }
            "model" -> {
                val path=Path().apply {
                    moveTo(12*u,2*u); lineTo(21*u,7*u); lineTo(21*u,17*u); lineTo(12*u,22*u)
                    lineTo(3*u,17*u); lineTo(3*u,7*u); close()
                }
                drawPath(path,c,style=Stroke(1.7f*u))
                line(3f,7f,12f,12f); line(21f,7f,12f,12f); line(12f,12f,12f,22f)
            }
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
