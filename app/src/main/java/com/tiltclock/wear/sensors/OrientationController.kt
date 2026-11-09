package com.tiltclock.wear.sensors

import android.content.Context
import android.hardware.*
import android.view.Surface
import com.tiltclock.wear.render.Quaternion
import com.tiltclock.wear.render.TransformController

/** Android rotation-vector fusion uses the gyro without unbounded angular-velocity integration. */
class OrientationController(context: Context, private val transforms: TransformController,
    private val rotation: () -> Int, private val report: (Boolean) -> Unit) : SensorEventListener {
    private val manager = context.getSystemService(SensorManager::class.java)
    private val gyro = manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val orientation = if (gyro != null) manager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR)
        ?: manager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) else null
    private var started = false
    private val quaternionBuffer = FloatArray(4)
    fun start() {
        if (started) return
        transforms.recalibrate()
        started = orientation != null && manager.registerListener(this,orientation,SensorManager.SENSOR_DELAY_GAME)
        report(started)
    }
    fun stop() { manager.unregisterListener(this); started = false; transforms.recalibrate() }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    override fun onSensorChanged(event: SensorEvent) {
        if (!started) return
        val q = quaternionBuffer; SensorManager.getQuaternionFromVector(q,event.values)
        val device = Quaternion(q[1],q[2],q[3],q[0]).normalized()
        val angle = when (rotation()) {
            Surface.ROTATION_90 -> -Math.PI.toFloat()/2
            Surface.ROTATION_180 -> Math.PI.toFloat()
            Surface.ROTATION_270 -> Math.PI.toFloat()/2
            else -> 0f
        }
        val screen = Quaternion.axis(0f,0f,1f,angle)
        transforms.sensor(screen.inverse()*device*screen,event.timestamp)
    }
}
