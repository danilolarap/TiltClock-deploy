package com.tiltclock.wear

import com.tiltclock.wear.data.*
import org.junit.Assert.*
import org.junit.Test
import java.time.*

class DailyStepsPolicyTest {
    private val now=Instant.parse("2026-11-08T18:00:00Z")
    private val zone=ZoneId.of("America/Bogota")
    @Test fun totalsReplaceAndDuplicatesDoNotAccumulate() {
        val p=DailyStepsPolicy()
        p.accept(1259,now,now,zone); p.accept(1259,now,now,zone)
        assertEquals(1400L,(p.accept(1400,now.plusSeconds(1),now.plusSeconds(1),zone) as Measurement.Available).value)
    }
    @Test fun outOfOrderBatchesKeepLatestTimestamp() {
        val p=DailyStepsPolicy(); p.accept(100,now,now,zone)
        assertEquals(100L,(p.accept(50,now.minusSeconds(10),now,zone) as Measurement.Available).value)
    }
    @Test fun genuineZeroIsValidButUnknownIsNotZero() {
        val p=DailyStepsPolicy(); assertEquals(Measurement.Loading,p.current(now,zone))
        assertEquals(0L,(p.accept(0,now,now,zone) as Measurement.Available).value)
    }
    @Test fun staleFutureNegativeAndPreviousDayNeverBecomeAvailable() {
        for(pair in listOf(-1L to now, 100L to now.minusSeconds(1000),100L to now.plusSeconds(30),100L to now.minusSeconds(86400))) {
            assertFalse(DailyStepsPolicy().accept(pair.first,pair.second,now,zone) is Measurement.Available)
        }
    }
    @Test fun midnightExpiryAndZoneChangesInvalidatePreviousReading() {
        val p=DailyStepsPolicy(); val end=Instant.parse("2026-11-09T04:59:50Z")
        p.accept(200,end,end,zone)
        assertFalse(p.current(end.plusSeconds(20),zone) is Measurement.Available)
        p.accept(200,now,now,zone)
        assertFalse(p.current(now,ZoneId.of("Europe/Madrid")) is Measurement.Available)
    }
    @Test fun ttlExpiresWithoutNewProviderData() {
        val p=DailyStepsPolicy(); p.accept(100,now,now,zone)
        assertFalse(p.current(now.plusSeconds(901),zone) is Measurement.Available)
    }
    @Test fun previousDayTotalEndingExactlyAtMidnightIsNotTodaysSteps() {
        val p=DailyStepsPolicy(); val midnight=Instant.parse("2026-11-09T05:00:00Z")
        assertFalse(p.accept(9000,midnight,midnight.plusSeconds(1),zone,midnight.minusSeconds(86400)) is Measurement.Available)
        assertEquals(0L,(p.accept(0,midnight,midnight.plusSeconds(1),zone,midnight) as Measurement.Available).value)
    }
}
