@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
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
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.*
import android.net.Uri
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class ClockState(
    val now: ZonedDateTime = ZonedDateTime.now(),
    val battery: Measurement<Int> = Measurement.Loading,
    val steps: Measurement<Long> = Measurement.Loading,
    val preferences: TiltPreferences = TiltPreferences(),
    val preferencesReady: Boolean = false,
)

class ClockViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = PreferencesRepository(application)
    private val foreground=MutableStateFlow(false)
    private val permissionRevision=MutableStateFlow(0)
    private val steps=combine(foreground,permissionRevision) { active,_ -> active }.flatMapLatest { active ->
        if(active) StepsRepository(application).readings else flowOf(Measurement.Loading)
    }
    fun setForeground(active: Boolean) { foreground.value=active }
    fun permissionChanged() { permissionRevision.value++ }
    val state = combine(ClockRepository().readings, BatteryRepository(application).readings,
        preferences.preferences,steps) { now, battery, prefs, steps ->
        ClockState(now = now, battery = battery, steps=steps, preferences = prefs, preferencesReady = true)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(0,replayExpirationMillis=0), ClockState())
    val render = MutableStateFlow(RenderState())
    val selection = MutableStateFlow<Pair<String,Int>?>(null)
    private var sequence = 0
    private val avatars = AvatarRepository(application)
    private val importMutex=Mutex()
    suspend fun prepare(id: String) = avatars.prepare(id)
    fun rendererId(prefs: TiltPreferences): String = if(prefs.avatar=="custom")
        runCatching { avatars.imports.idForPath(requireNotNull(prefs.importedModel)) }.getOrDefault("missing_import") else prefs.avatar
    fun selectAvatar(id: String) { render.value=render.value.copy(loading=true,error=null); selection.value = id to ++sequence }
    fun avatarAccepted(id: String) { viewModelScope.launch {
        if(ImportLimits.validId(id)) preferences.commitAvatar("custom",avatars.imports.file(id).absolutePath)
        else preferences.commitAvatar(id)
        importMutex.withLock {
            val requested=selection.value?.first
            avatars.imports.retain(setOfNotNull(id.takeIf(ImportLimits::validId),requested?.takeIf(ImportLimits::validId)))
        }
    } }
    val importBusy=MutableStateFlow(false)
    val importError=MutableStateFlow(false)
    fun hasInbox()=avatars.imports.inbox.isFile
    fun importDocument(uri: Uri?) {
        if(importBusy.value) return
        importBusy.value=true; importError.value=false
        viewModelScope.launch {
            try { importMutex.withLock {
                selectAvatar(withTimeout(60_000) { if(uri==null) avatars.imports.importInbox() else avatars.imports.import(uri) })
            } }
            catch(cancel: CancellationException) { if(cancel is TimeoutCancellationException) importError.value=true else throw cancel }
            catch(failure: Exception) { importError.value=true }
            finally { importBusy.value=false }
        }
    }
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
