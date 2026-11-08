package com.tiltclock.wear.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp

@Composable
fun GlassCapsule(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    val p = LocalWatchPalette.current
    Row(modifier.shadow(3.dp, RoundedCornerShape(16.dp)).background(p.glass, RoundedCornerShape(16.dp))
        .border(.6.dp,p.border,RoundedCornerShape(16.dp)).padding(horizontal=8.dp,vertical=2.dp),
        verticalAlignment=androidx.compose.ui.Alignment.CenterVertically, content=content)
}
