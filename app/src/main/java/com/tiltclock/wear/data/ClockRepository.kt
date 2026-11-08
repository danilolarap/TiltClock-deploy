package com.tiltclock.wear.data

import java.time.ZonedDateTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow

class ClockRepository {
    val readings = flow {
        while (true) {
            // Re-read the system zone every tick; never freeze the zone at startup.
            emit(ZonedDateTime.now())
            delay(1_000)
        }
    }
}

object ClockText {
    fun time(now: ZonedDateTime): String = "%02d:%02d".format(java.util.Locale.ROOT,
        (now.hour % 12).let { if (it == 0) 12 else it }, now.minute)
    fun period(now: ZonedDateTime): String = if (now.hour < 12) "AM" else "PM"
    fun date(now: ZonedDateTime, days: Array<String>, months: Array<String>): String =
        "${days[now.dayOfWeek.value % 7]}, ${now.dayOfMonth} ${months[now.monthValue - 1]}"
}
