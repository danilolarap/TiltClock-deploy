package com.tiltclock.wear.render

object ModelTransform {
    /** T(-center.x,-center.y-.02,-center.z) is a CHILD of rotation and base/user scale. */
    fun center(center: FloatArray): FloatArray = Quaternion().matrix().also {
        it[12] = -center[0]; it[13] = -center[1]-.02f; it[14] = -center[2]
    }
    fun baseScale(halfExtent: FloatArray): Float {
        val maximum = 2f*(halfExtent.maxOrNull() ?: 0f)
        require(maximum.isFinite() && maximum > 0f) { "Invalid model bounds" }
        return 2.8f/maximum
    }
}
