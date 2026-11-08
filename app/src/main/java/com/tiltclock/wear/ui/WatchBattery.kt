package com.tiltclock.wear.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tiltclock.wear.R
import com.tiltclock.wear.data.Measurement

@Composable
fun WatchBattery(battery: Measurement<Int>, scale: Float) {
    GlassCapsule {
        Canvas(Modifier.size((12*scale).dp,(8*scale).dp)) {
            val c=Color(0xFF10B981)
            drawRoundRect(c, size=Size(size.width*.85f,size.height),cornerRadius=CornerRadius(2f),style=Stroke(1.5f))
            drawRect(c,Offset(size.width*.9f,size.height*.3f),Size(size.width*.1f,size.height*.4f))
            if (battery is Measurement.Available) drawRect(c,Offset(2f,2f),Size((size.width*.85f-4f)*battery.value/100f,(size.height-4f).coerceAtLeast(0f)))
        }
        Spacer(Modifier.width(4.dp))
        WatchText(if(battery is Measurement.Available) "${battery.value}%" else stringResource(R.string.battery_unknown),10*scale)
    }
}
