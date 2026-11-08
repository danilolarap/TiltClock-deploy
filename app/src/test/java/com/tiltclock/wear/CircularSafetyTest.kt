package com.tiltclock.wear

import com.tiltclock.wear.ui.CircularGeometry
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.*

class CircularSafetyTest {
    @Test fun rowsFitBothExtremesAcrossSizesAndFontScales() {
        for(d in listOf(192f,227f,298f,400f)) for(h in listOf(48f,64f,96f)) {
            val r=d/2
            for(y in -200..200) {
                val center=y*d/400
                val width=CircularGeometry.rowWidth(r,center,h,8f)
                if(width>0) for(edge in listOf(center-h/2,center+h/2)) {
                    assertTrue(width*width/4+edge*edge <= (r-8)*(r-8)+.01f)
                }
            }
        }
    }
    @Test fun anyRowCanBeCenteredWithSymmetricPadding() {
        assertTrue(CircularGeometry.rowWidth(96f,0f,64f,8f)>=48)
        assertEquals(0f,CircularGeometry.rowWidth(96f,96f,64f,8f),0f)
    }
}
