package com.tiltclock.wear.verification

import android.app.Instrumentation
import android.os.Bundle
import android.net.Uri
import com.tiltclock.wear.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import java.io.File

class Phase4Instrumentation: Instrumentation() {
    override fun onCreate(arguments: Bundle?) { super.onCreate(arguments); start() }
    override fun onStart() {
        val report=JSONObject()
        try { runBlocking {
            val store=ImportedAvatarStore(targetContext)
            val id=store.import(Uri.parse("content://com.tiltclock.wear.phase4/valid"))
            check(store.read(id).contentEquals(targetContext.assets.open("models/fox.glb").use { it.readBytes() }))
            val prefs=PreferencesRepository(targetContext)
            prefs.commitAvatar("custom",store.file(id).absolutePath)
            val saved=prefs.preferences.first()
            check(saved.avatar=="custom" && store.idForPath(saved.importedModel!!)==id)
            report.put("validImportExactBytes",true).put("durablePrivatePath",true)
            try { store.import(Uri.parse("content://com.tiltclock.wear.phase4/invalid")); error("Invalid accepted") }
            catch(expected: IllegalArgumentException) { report.put("invalidRejected",true) }
            try { store.import(Uri.parse("content://com.tiltclock.wear.phase4/external")); error("External accepted") }
            catch(expected: IllegalArgumentException) { report.put("externalRejected",true) }
            check(prefs.preferences.first().importedModel==saved.importedModel)
            report.put("invalidPreservesPreference",true)
            val health=withTimeout(30_000) { StepsRepository(targetContext).readings.first { it!=Measurement.Loading } }
            report.put("actualHealthState",health.javaClass.simpleName)
            report.put("documentPickerAvailable",DocumentAvailability.supported(targetContext))
            prefs.commitAvatar("holo")
            report.put("success",true)
        } } catch(failure: Throwable) { report.put("success",false).put("error",failure.toString()) }
        File(targetContext.filesDir,"phase4-report.json").writeText(report.toString(2))
        finish(if(report.optBoolean("success")) 0 else 1,Bundle().apply { putString("report",report.toString()) })
    }
}
