package com.tiltclock.wear

import com.tiltclock.wear.render.ZoomController
import org.junit.Assert.*
import org.junit.Test

class ZoomControllerTest {
    @Test fun tapsAndRepeatsUseDifferentSteps() {
        assertEquals(1.1f,ZoomController.change(1f,1),.00001f)
        assertEquals(.97f,ZoomController.change(1f,-1,true),.00001f)
    }
    @Test fun repeatedZoomNeverEscapesLimits() {
        var scale=1f; repeat(500) { scale=ZoomController.change(scale,1,true) }; assertEquals(3f,scale,0f)
        repeat(500) { scale=ZoomController.change(scale,-1,true) }; assertEquals(.3f,scale,0f)
    }
}
