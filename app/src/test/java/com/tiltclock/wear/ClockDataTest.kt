package com.tiltclock.wear

import com.tiltclock.wear.data.*
import java.time.Instant
import java.time.ZonedDateTime
import org.junit.Assert.*
import org.junit.Test

class ClockDataTest {
    @Test fun twelveHourBoundariesAndZone() {
        val midnight = ZonedDateTime.parse("2026-10-24T00:07:00-05:00[America/Bogota]")
        assertEquals("12:07", ClockText.time(midnight))
        assertEquals("AM", ClockText.period(midnight))
        assertEquals("12:07", ClockText.time(midnight.plusHours(12)))
        assertEquals("PM", ClockText.period(midnight.plusHours(12)))
        assertEquals("11:07", ClockText.time(midnight.plusHours(23)))
        assertEquals("05:07", ClockText.time(midnight.withZoneSameInstant(java.time.ZoneId.of("UTC"))))
    }
    @Test fun sundayIndexesAndLocalizedDate() {
        val days = arrayOf("DOM", "LUN", "MAR", "MIÉ", "JUE", "VIE", "SÁB")
        val months = arrayOf("ENE", "FEB", "MAR", "ABR", "MAY", "JUN", "JUL", "AGO", "SEP", "OCT", "NOV", "DIC")
        val sunday = ZonedDateTime.parse("2026-11-08T23:59:00-05:00[America/Bogota]")
        assertEquals("DOM, 8 NOV", ClockText.date(sunday, days, months))
        assertEquals("LUN, 9 NOV", ClockText.date(sunday.plusMinutes(1), days, months))
    }
    @Test fun unknownIsNotZeroAndGoalIsClamped() {
        assertNull(StepGoal.progress(Measurement.Loading))
        assertNull(StepGoal.progress(Measurement.PermissionRequired))
        assertNull(StepGoal.progress(Measurement.Unavailable("provider missing")))
        fun reading(value: Long) = Measurement.Available(value, "test only", Instant.EPOCH)
        assertEquals(0f, StepGoal.progress(reading(0)))
        assertEquals(.1259f, StepGoal.progress(reading(1259)))
        assertEquals(1f, StepGoal.progress(reading(20000)))
        assertEquals(0f, StepGoal.progress(reading(-1)))
    }
}
