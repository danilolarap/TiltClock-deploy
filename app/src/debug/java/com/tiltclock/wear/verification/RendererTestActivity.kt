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

/** Debug-only geometry host for emulator input tests. No launcher, fake measurements or release UI. */
class RendererTestActivity : ComponentActivity() {
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
            RendererScreen(model,model.state.collectAsStateWithLifecycle().value)
        }
        window.decorView.post { window.decorView.windowInsetsController?.hide(android.view.WindowInsets.Type.systemBars()) }
    }
}
