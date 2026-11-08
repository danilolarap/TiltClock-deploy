package com.tiltclock.wear

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tiltclock.wear.data.*
import java.time.ZonedDateTime
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import com.tiltclock.wear.render.RenderState
import com.tiltclock.wear.render.ZoomController

data class ClockState(
    val now: ZonedDateTime = ZonedDateTime.now(),
    val battery: Measurement<Int> = Measurement.Loading,
    val steps: Measurement<Long> = Measurement.Unavailable("Health Services integration pending phase 4"),
    val preferences: TiltPreferences = TiltPreferences(),
    val preferencesReady: Boolean = false,
)

class ClockViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = PreferencesRepository(application)
    val state = combine(ClockRepository().readings, BatteryRepository(application).readings,
        preferences.preferences) { now, battery, prefs ->
        ClockState(now = now, battery = battery, preferences = prefs, preferencesReady = true)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(0), ClockState())
    val render = MutableStateFlow(RenderState())
    val selection = MutableStateFlow<Pair<String,Int>?>(null)
    private var sequence = 0
    private val avatars = AvatarRepository(application)
    suspend fun prepare(id: String) = avatars.prepare(id)
    fun selectAvatar(id: String) { selection.value = id to ++sequence }
    fun avatarAccepted(id: String) { viewModelScope.launch { preferences.commitAvatar(id) } }
    fun setTheme(value: String) { viewModelScope.launch { preferences.setTheme(value) } }
    fun setLanguage(value: String) { viewModelScope.launch { preferences.setLanguage(value) } }
    // Serialize writes and derive repeat steps from the latest requested value, not a lagging collector.
    private var requestedScale: Float? = null
    private var scaleWrite: kotlinx.coroutines.Job? = null
    fun zoom(direction: Int, repeating: Boolean) {
        val value = ZoomController.change(requestedScale ?: state.value.preferences.scale,direction,repeating)
        requestedScale = value
        scaleWrite?.cancel()
        scaleWrite = viewModelScope.launch { preferences.setScale(value) }
    }
}
