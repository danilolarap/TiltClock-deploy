package com.tiltclock.wear

import com.tiltclock.wear.render.Quaternion
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.PI

class QuaternionTest {
    @Test fun inverseCancelsRotation() {
        val q = Quaternion.axis(1f,2f,3f,1.7f)
        assertEquals(0f,(q*q.inverse()).angleTo(Quaternion()),.001f)
    }
    @Test fun oppositeSignsRepresentSamePose() {
        val q = Quaternion.axis(0f,1f,0f,2f)
        assertEquals(0f,q.slerp(Quaternion(-q.x,-q.y,-q.z,-q.w),.5f).angleTo(q),.001f)
    }
    @Test fun quarterTurnMapsXToNegativeZ() {
        val m = Quaternion.axis(0f,1f,0f,PI.toFloat()/2).matrix()
        assertEquals(0f,m[0],.0001f); assertEquals(-1f,m[2],.0001f)
    }
    @Test fun zeroAndInvalidInputsStayFinite() {
        assertEquals(Quaternion(),Quaternion(0f,0f,0f,0f).normalized())
        assertEquals(Quaternion(),Quaternion(Float.NaN).normalized())
    }
    @Test fun temporalResponseIsIndependentOfFrameRate() {
        val a = Quaternion.response(.15f,1f/30)
        val b = Quaternion.response(.15f,1f/60)
        assertEquals(a,1-(1-b)*(1-b),.00001f)
    }
}
