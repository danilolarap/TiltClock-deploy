package com.tiltclock.wear.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiltclock.wear.ClockState
import com.tiltclock.wear.R
import com.tiltclock.wear.data.ClockText
import com.tiltclock.wear.data.Measurement

/** Diagnostic foundation only. The measured TiltClock UI and bundled fonts arrive in phase 3. */
@Composable
fun FoundationScreen(state: ClockState) {
    val configuration = LocalConfiguration.current
    val round = configuration.screenLayout and Configuration.SCREENLAYOUT_ROUND_MASK ==
        Configuration.SCREENLAYOUT_ROUND_YES
    val watch = configuration.uiMode and Configuration.UI_MODE_TYPE_MASK == Configuration.UI_MODE_TYPE_WATCH
    Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
        Box(Modifier.fillMaxSize().aspectRatio(1f).clip(CircleShape).background(Color(0xFFF8FAFC)),
            contentAlignment = Alignment.Center) {
            Column(Modifier.fillMaxWidth(.7f).padding(vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (!round || !watch) {
                    Label(stringResource(R.string.round_required))
                } else {
                    Label("TiltClock 3D")
                    Label(stringResource(R.string.base_status))
                    Label(ClockText.date(state.now, stringArrayResource(R.array.weekdays), stringArrayResource(R.array.months)))
                    Label("${ClockText.time(state.now)} ${ClockText.period(state.now)}", 24)
                    Label(stringResource(R.string.renderer_pending))
                    Label(stringResource(R.string.steps_unavailable))
                    val battery = state.battery
                    Label(if (battery is Measurement.Available) stringResource(R.string.battery_value, battery.value)
                        else stringResource(R.string.battery_unknown))
                }
            }
        }
    }
}

@Composable
private fun Label(text: String, size: Int = 12) {
    BasicText(text, style = TextStyle(color = Color(0xFF0F172A), fontSize = size.sp, textAlign = TextAlign.Center))
}
