package com.tiltclock.wear

import com.tiltclock.wear.render.ModelTransform
import org.junit.Assert.*
import org.junit.Test

class ModelTransformTest {
    @Test fun offsetIsAppliedBeforeScale() {
        val center=ModelTransform.center(floatArrayOf(2f,3f,4f))
        val scale=ModelTransform.baseScale(floatArrayOf(.5f,1f,.25f))
        assertEquals(1.4f,scale,.00001f)
        assertEquals(-2f,center[12],0f); assertEquals(-3.02f,center[13],.00001f)
        assertEquals(-.028f,(center[13]+3f)*scale,.00001f)
    }
    @Test(expected=IllegalArgumentException::class) fun emptyBoundsRejected() { ModelTransform.baseScale(floatArrayOf(0f,0f,0f)) }
}
