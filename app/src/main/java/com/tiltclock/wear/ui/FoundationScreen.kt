package com.tiltclock.wear.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tiltclock.wear.ClockState
import com.tiltclock.wear.R

/** Unsupported-device notice replacing the phase-one diagnostic screen. */
@Composable
fun FoundationScreen(@Suppress("UNUSED_PARAMETER") state: ClockState) {
    Box(Modifier.fillMaxSize().background(LocalWatchPalette.current.background).padding(24.dp),contentAlignment=Alignment.Center) {
        WatchText(stringResource(R.string.round_required))
    }
}
