package com.tiltclock.wear.render

import kotlin.math.min

/** Sensor and touch share one rotation; the internal clip never writes this parent transform. */
class TransformController {
    private var sensor = Quaternion()
    private var filteredSensor = Quaternion()
    private var reference: Quaternion? = null
    private var base = Quaternion()
    private var visible = Quaternion()
    private var target = Quaternion()
    private var dragging = false
    private var lastSensorNanos = 0L

    @Synchronized fun sensor(value: Quaternion, nanos: Long) {
        sensor = value.normalized()
        val dt = if (lastSensorNanos == 0L) 1f/60 else ((nanos-lastSensorNanos)/1e9f).coerceIn(0f,.1f)
        lastSensorNanos = nanos
        filteredSensor = if (reference == null) sensor else filteredSensor.slerp(sensor, Quaternion.response(.15f,dt))
        if (reference == null) reference = filteredSensor
        if (!dragging) target = base * (reference!!.inverse() * filteredSensor)
    }
    @Synchronized fun beginDrag() {
        dragging = true; base = visible; target = visible
    }
    @Synchronized fun drag(dx: Float, dy: Float, diameterPixels: Float) {
        if (!dragging || diameterPixels <= 0) return
        // Reference mobile useful diameter: 298 CSS px; refine after DOM measurement in phase 3.
        val sensitivity = .015f*298f/diameterPixels
        base = Quaternion.axis(0f,1f,0f,dx*sensitivity) *
            Quaternion.axis(1f,0f,0f,dy*sensitivity) * base
        target = base
    }
    @Synchronized fun endDrag() {
        // Keep the last displayed pose, discarding any smoothing backlog on release/cancel.
        base = visible; target = visible; reference = sensor; filteredSensor = sensor; dragging = false
    }
    @Synchronized fun recalibrate() {
        base = visible; target = visible; reference = null; lastSensorNanos = 0L
    }
    @Synchronized fun frame(seconds: Float): Quaternion {
        val dt = seconds.coerceIn(0f,.1f)
        val angle = visible.angleTo(target)
        val fraction = if (angle < 1e-6f) 1f else min(Quaternion.response(.08f,dt),3f*dt/angle)
        visible = visible.slerp(target,fraction)
        return visible
    }
}
