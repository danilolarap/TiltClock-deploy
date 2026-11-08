package com.tiltclock.wear.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.tiltclock.wear.*
import com.tiltclock.wear.R
import com.tiltclock.wear.data.*

@Composable
fun ClockCapsules(state: ClockState, scale: Float, modifier: Modifier = Modifier) {
    val p = LocalWatchPalette.current
    Column(modifier, horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy((6*scale).dp)) {
        GlassCapsule {
            WatchIcon("calendar",Modifier.size((10*scale).dp),true)
            Spacer(Modifier.width((4*scale).dp))
            WatchText(ClockText.date(state.now,stringArrayResource(R.array.weekdays),stringArrayResource(R.array.months)),10*scale)
            WatchText(" • ",10*scale)
            val steps = state.steps
            WatchIcon("steps",Modifier.size((10*scale).dp),true)
            WatchText(when(steps) {
                is Measurement.Available -> "${steps.value} " + stringResource(R.string.steps_label)
                Measurement.Loading -> "…"
                Measurement.PermissionRequired -> stringResource(R.string.permission_short)
                else -> stringResource(R.string.steps_unknown_short)
            },9*scale)
        }
        GlassCapsule {
            BasicText(ClockText.time(state.now),style=TextStyle(color=p.text,fontFamily=SpaceGrotesk,
                fontWeight=FontWeight.Bold,fontSize=(30*scale).sp,fontFeatureSettings="tnum",
                shadow=Shadow(p.cyan.copy(alpha=.3f),Offset(0f,2f),10f)))
            Spacer(Modifier.width((4*scale).dp))
            BasicText(ClockText.period(state.now),style=TextStyle(color=p.cyan,fontFamily=SpaceGrotesk,
                fontWeight=FontWeight.Bold,fontSize=(12*scale).sp))
        }
    }
}
