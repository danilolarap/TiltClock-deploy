package com.tiltclock.wear.data

import java.time.Instant

/** Unknown is a state, never the number zero. No sample values in production. */
sealed interface Measurement<out T> {
    data object Loading : Measurement<Nothing>
    data object PermissionRequired : Measurement<Nothing>
    data class Unavailable(val reason: String) : Measurement<Nothing>
    data class Available<T>(val value: T, val source: String, val observedAt: Instant) : Measurement<T>
}

object StepGoal {
    // TiltClock's own goal; not read from Samsung Health.
    const val DAILY = 10_000L
    fun progress(steps: Measurement<Long>): Float? = when (steps) {
        is Measurement.Available -> (steps.value.toDouble() / DAILY).coerceIn(0.0, 1.0).toFloat()
        else -> null
    }
}
