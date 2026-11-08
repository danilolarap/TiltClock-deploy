package com.tiltclock.wear.verification

import android.content.res.Configuration
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tiltclock.wear.ClockViewModel
import com.tiltclock.wear.ui.RendererScreen
import com.tiltclock.wear.data.Measurement
import java.time.ZonedDateTime
import java.time.Instant

/** Debug-only geometry host for emulator input tests. No launcher, fake measurements or release UI. */
class RendererTestActivity : ComponentActivity() {
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if(hasFocus) window.insetsController?.hide(android.view.WindowInsets.Type.systemBars())
    }
    override fun attachBaseContext(base: Context) {
        val configuration=Configuration(base.resources.configuration).apply {
            uiMode = (uiMode and Configuration.UI_MODE_TYPE_MASK.inv()) or Configuration.UI_MODE_TYPE_WATCH
            screenLayout = (screenLayout and Configuration.SCREENLAYOUT_ROUND_MASK.inv()) or Configuration.SCREENLAYOUT_ROUND_YES
        }
        super.attachBaseContext(base.createConfigurationContext(configuration))
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val model: ClockViewModel = viewModel()
            val live=model.state.collectAsStateWithLifecycle().value
            val fixture=intent.getBooleanExtra("fixture",false)
            RendererScreen(model,if(fixture) live.copy(
                now=ZonedDateTime.parse("2022-10-24T08:27:00-05:00"),
                steps=Measurement.Available(1259L,"debug visual fixture",Instant.now()),
                battery=Measurement.Available(85,"debug visual fixture",Instant.now()),
                preferences=live.preferences.copy(avatar="holo",scale=1f,
                    theme=intent.getStringExtra("theme") ?: "light",language=intent.getStringExtra("language") ?: "es")) else live,visualFixture=fixture)
        }
        window.decorView.post { window.decorView.windowInsetsController?.hide(android.view.WindowInsets.Type.systemBars()) }
    }
}
