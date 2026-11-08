package com.tiltclock.wear.render

object ZoomController {
    const val MIN = .3f
    const val MAX = 3f
    const val HOLD_DELAY_MS = 250L
    const val REPEAT_MS = 50L
    fun change(scale: Float, direction: Int, repeating: Boolean = false): Float =
        (scale + direction.coerceIn(-1,1)*(if (repeating) .03f else .1f)).coerceIn(MIN,MAX)
}
