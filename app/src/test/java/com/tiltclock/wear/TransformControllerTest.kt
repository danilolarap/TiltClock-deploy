package com.tiltclock.wear

import com.tiltclock.wear.render.*
import org.junit.Assert.*
import org.junit.Test

class TransformControllerTest {
    private fun settle(c: TransformController) { repeat(180) { c.frame(1f/60) } }
    @Test fun physicalSensorChangesVisiblePose() {
        val c=TransformController(); c.sensor(Quaternion(),1)
        repeat(180) { c.sensor(Quaternion.axis(1f,0f,0f,.5f),it*16_666_667L+2); c.frame(1f/60) }
        assertEquals(.5f,c.frame(0f).angleTo(Quaternion()),.002f)
    }
    @Test fun dragHasPriorityAndReleaseDoesNotSnap() {
        val c=TransformController(); c.sensor(Quaternion(),1); c.beginDrag(); c.drag(40f,10f,298f)
        settle(c); val before=c.frame(0f)
        c.sensor(Quaternion.axis(0f,1f,0f,1.2f),20_000_000)
        assertEquals(0f,before.angleTo(c.frame(1f/60)),.001f)
        c.endDrag(); c.sensor(Quaternion.axis(0f,1f,0f,1.2f),40_000_000)
        assertEquals(0f,before.angleTo(c.frame(1f/60)),.001f)
    }
    @Test fun sensorContinuesFromManualBase() {
        val c=TransformController(); c.sensor(Quaternion(),1); c.beginDrag(); c.drag(30f,0f,298f)
        settle(c); c.endDrag(); val base=c.frame(0f)
        repeat(180) { c.sensor(Quaternion.axis(1f,0f,0f,.2f),it*16_666_667L+2); c.frame(1f/60) }
        assertEquals(.2f,base.angleTo(c.frame(0f)),.002f)
    }
    @Test fun screenSizeNormalizationIsConsistent() {
        fun pose(d: Float): Quaternion { val c=TransformController(); c.beginDrag(); c.drag(d/10,0f,d); settle(c); return c.frame(0f) }
        assertEquals(0f,pose(192f).angleTo(pose(228f)),.001f)
    }
    @Test fun rotationSpeedIsBoundedByTime() {
        val c=TransformController(); c.beginDrag(); c.drag(200f,0f,298f)
        assertTrue(c.frame(1f/30).angleTo(Quaternion()) <= .1001f)
    }
    @Test fun resumeRecalibrationPreservesVisiblePose() {
        val c=TransformController(); c.beginDrag(); c.drag(30f,0f,298f); settle(c); c.endDrag()
        val before=c.frame(0f); c.recalibrate(); c.sensor(Quaternion.axis(1f,0f,0f,2f),1)
        assertEquals(0f,before.angleTo(c.frame(1f/60)),.001f)
    }
}
