package com.tiltclock.wear.render

import kotlin.math.*

/** Unit quaternions, Hamilton product; column-major matrices for Filament. */
data class Quaternion(val x: Float = 0f, val y: Float = 0f, val z: Float = 0f, val w: Float = 1f) {
    fun normalized(): Quaternion {
        val n = sqrt(x*x + y*y + z*z + w*w)
        return if (n > 1e-8f && n.isFinite()) Quaternion(x/n,y/n,z/n,w/n) else Quaternion()
    }
    fun inverse() = Quaternion(-x,-y,-z,w).normalized()
    operator fun times(b: Quaternion) = Quaternion(
        w*b.x+x*b.w+y*b.z-z*b.y, w*b.y-x*b.z+y*b.w+z*b.x,
        w*b.z+x*b.y-y*b.x+z*b.w, w*b.w-x*b.x-y*b.y-z*b.z).normalized()
    fun angleTo(b: Quaternion): Float = 2f*acos(abs(dot(b)).coerceIn(0f,1f))
    private fun dot(b: Quaternion) = x*b.x+y*b.y+z*b.z+w*b.w
    fun slerp(b: Quaternion, fraction: Float): Quaternion {
        var d = dot(b); val sign = if (d < 0) -1f else 1f; d = abs(d).coerceIn(0f,1f)
        val t = fraction.coerceIn(0f,1f)
        val theta = acos(d)
        val aWeight = if (d > .9995f) 1f-t else sin((1f-t)*theta)/sin(theta)
        val bWeight = (if (d > .9995f) t else sin(t*theta)/sin(theta))*sign
        return Quaternion(x*aWeight+b.x*bWeight,y*aWeight+b.y*bWeight,
            z*aWeight+b.z*bWeight,w*aWeight+b.w*bWeight).normalized()
    }
    fun matrix(scale: Float = 1f): FloatArray {
        val q = normalized(); val (a,b,c,d) = q
        return floatArrayOf(
            (1-2*b*b-2*c*c)*scale,(2*a*b+2*c*d)*scale,(2*a*c-2*b*d)*scale,0f,
            (2*a*b-2*c*d)*scale,(1-2*a*a-2*c*c)*scale,(2*b*c+2*a*d)*scale,0f,
            (2*a*c+2*b*d)*scale,(2*b*c-2*a*d)*scale,(1-2*a*a-2*b*b)*scale,0f,
            0f,0f,0f,1f)
    }
    companion object {
        fun axis(x: Float, y: Float, z: Float, angle: Float): Quaternion {
            val length = sqrt(x*x+y*y+z*z); if (length == 0f) return Quaternion()
            val s = sin(angle/2)/length
            return Quaternion(x*s,y*s,z*s,cos(angle/2)).normalized()
        }
        fun response(perFrame: Float, seconds: Float) = 1f-(1f-perFrame).pow(seconds*60f)
    }
}
