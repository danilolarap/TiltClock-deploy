package com.tiltclock.wear.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tiltclock.wear.R

@Composable
fun ImportNotice(close: ()->Unit) {
    BackHandler(onBack=close)
    Box(Modifier.fillMaxSize().background(LocalWatchPalette.current.background),contentAlignment=Alignment.Center) {
        Column(Modifier.fillMaxWidth(.7f).verticalScroll(rememberScrollState()).padding(vertical=56.dp),horizontalAlignment=Alignment.CenterHorizontally) {
            WatchText(stringResource(R.string.import_provider_missing),12f)
            Box(Modifier.heightIn(min=48.dp).clickable(onClick=close),contentAlignment=Alignment.Center) {
                WatchText(stringResource(R.string.close_settings),14f)
            }
        }
    }
}
