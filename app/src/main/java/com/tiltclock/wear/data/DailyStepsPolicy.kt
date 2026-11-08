package com.tiltclock.wear.data

import java.time.*

/** Daily provider totals replace, never increment, the displayed value. No persisted readings. */
class DailyStepsPolicy {
    companion object { val MAX_AGE: Duration = Duration.ofMinutes(15) }
    private var latest: Measurement.Available<Long>? = null
    private var zone: ZoneId? = null
    fun reset() { latest=null; zone=null }
    fun accept(total: Long, end: Instant, now: Instant, currentZone: ZoneId,start: Instant? = null): Measurement<Long> {
        val midnight=now.atZone(currentZone).toLocalDate().atStartOfDay(currentZone).toInstant()
        if(start!=null && start<midnight && end<=midnight) return current(now,currentZone)
        if(total<0 || end.isAfter(now.plusSeconds(5)) || Duration.between(end,now)>MAX_AGE ||
            end.atZone(currentZone).toLocalDate()!=now.atZone(currentZone).toLocalDate()) return current(now,currentZone)
        if(zone!=currentZone) reset()
        if(latest==null || !end.isBefore(latest!!.observedAt)) {
            zone=currentZone; latest=Measurement.Available(total,"Health Services STEPS_DAILY",end)
        }
        return current(now,currentZone)
    }
    fun current(now: Instant, currentZone: ZoneId): Measurement<Long> {
        val value=latest ?: return Measurement.Loading
        if(zone!=currentZone || value.observedAt.atZone(currentZone).toLocalDate()!=now.atZone(currentZone).toLocalDate() ||
            Duration.between(value.observedAt,now)>MAX_AGE || value.observedAt.isAfter(now.plusSeconds(5))) {
            reset(); return Measurement.Unavailable("Daily steps expired or clock changed")
        }
        return value
    }
}
