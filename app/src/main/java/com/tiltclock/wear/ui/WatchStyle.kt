package com.tiltclock.wear.ui

import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.*
import com.tiltclock.wear.R

val Inter = FontFamily(Font(R.font.inter, FontWeight.Normal), Font(R.font.inter, FontWeight.SemiBold), Font(R.font.inter, FontWeight.Bold))
val SpaceGrotesk = FontFamily(Font(R.font.space_grotesk, FontWeight.Bold))
data class WatchPalette(val dark: Boolean) {
    val background = Color(if (dark) 0xFF030712 else 0xFFF8FAFC)
    val text = Color(if (dark) 0xFFF8FAFC else 0xFF0F172A)
    val secondary = Color(if (dark) 0xFFCBD5E1 else 0xFF475569)
    val glass = Color(if (dark) 0xD90F172A else 0xCCFFFFFF)
    val border = Color(if (dark) 0xB3334155 else 0x99E2E8F0)
    val cyan = Color(0xFF06B6D4)
}
val LocalWatchPalette = staticCompositionLocalOf { WatchPalette(false) }

/** All geometry uses the measured 298 CSS px internal web canvas. */
object WatchDimensions {
    const val REFERENCE = 298f
    const val RING_RADIUS = .4f
    const val OPEN_MS = 280
    const val TOUCH = 48
}
