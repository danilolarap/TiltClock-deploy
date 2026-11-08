package com.tiltclock.wear.ui

import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

@Composable
fun WatchText(text: String, size: Float = 12f, modifier: Modifier = Modifier) {
    BasicText(text, modifier, style = TextStyle(color = LocalWatchPalette.current.text,
        fontFamily = Inter, fontWeight = FontWeight.SemiBold, fontSize = size.sp, textAlign = TextAlign.Center),
        maxLines=2, overflow=androidx.compose.ui.text.style.TextOverflow.Ellipsis)
}
