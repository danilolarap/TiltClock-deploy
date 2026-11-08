package com.tiltclock.wear

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tiltclock.wear.data.*
import java.time.ZonedDateTime
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ClockState(
    val now: ZonedDateTime = ZonedDateTime.now(),
    val battery: Measurement<Int> = Measurement.Loading,
    val steps: Measurement<Long> = Measurement.Unavailable("Health Services integration pending phase 4"),
    val preferences: TiltPreferences = TiltPreferences(),
)

class ClockViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = PreferencesRepository(application)
    val state = combine(ClockRepository().readings, BatteryRepository(application).readings,
        preferences.preferences) { now, battery, prefs ->
        ClockState(now = now, battery = battery, preferences = prefs)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(0), ClockState())
}
