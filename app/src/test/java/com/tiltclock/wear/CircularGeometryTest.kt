package com.tiltclock.wear

import com.tiltclock.wear.ui.CircularGeometry
import org.junit.Assert.*
import org.junit.Test

class CircularGeometryTest {
    @Test fun everyRowCornerFitsAtBothWatchSizes() {
        for (diameter in listOf(192f, 228f)) {
            val radius = diameter / 2
            for (y in -120..120) {
                val width = CircularGeometry.rowWidth(radius, y.toFloat(), 48f, 6f)
                if (width == 0f) continue
                for (edgeY in listOf(y - 24f, y + 24f)) {
                    val squaredDistance = width * width / 4 + edgeY * edgeY
                    assertTrue(squaredDistance <= (radius - 6f) * (radius - 6f) + .01f)
                }
            }
        }
    }
    @Test fun inaccessibleRowsMustScrollToCenterBeforeInteraction() {
        assertEquals(0f, CircularGeometry.rowWidth(96f, 90f, 48f, 6f), 0f)
        assertTrue(CircularGeometry.rowWidth(96f, 0f, 48f, 6f) >= 48f)
        assertEquals(CircularGeometry.rowWidth(114f, 40f, 48f, 6f),
            CircularGeometry.rowWidth(114f, -40f, 48f, 6f), .001f)
    }
}
