package com.tiltclock.wear.ui

import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sqrt

/** Coordinates are relative to the center of the useful screen, in the same units. */
object CircularGeometry {
    fun rowWidth(radius: Float, centerY: Float, rowHeight: Float, safetyMargin: Float): Float {
        require(radius > 0 && rowHeight >= 0 && safetyMargin >= 0)
        val safeRadius = (radius - safetyMargin).coerceAtLeast(0f)
        val farthestY = max(abs(centerY - rowHeight / 2), abs(centerY + rowHeight / 2))
        if (farthestY >= safeRadius) return 0f
        return 2 * sqrt(safeRadius * safeRadius - farthestY * farthestY)
    }
}
