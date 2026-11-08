package com.tiltclock.wear.data

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.os.SystemClock
import androidx.health.services.client.*
import androidx.health.services.client.data.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import java.time.*

class StepsRepository(private val context: Context) {
    val readings = callbackFlow<Measurement<Long>> {
        trySend(Measurement.Loading)
        val policy=DailyStepsPolicy()
        var status: Measurement<Long> = Measurement.Loading
        var registered=false
        var changedAt=Instant.MIN
        val client=try { HealthServices.getClient(context).passiveMonitoringClient }
        catch(failure: Exception) { trySend(Measurement.Unavailable("Health Services unavailable")); close(); return@callbackFlow }
        fun publish(value: Measurement<Long>) { status=value; trySend(value) }
        val callback=object: PassiveListenerCallback {
            override fun onNewDataPointsReceived(dataPoints: DataPointContainer) {
                val now=Instant.now(); val zone=ZoneId.systemDefault()
                val boot=now.minusMillis(SystemClock.elapsedRealtime())
                dataPoints.getData(DataType.STEPS_DAILY).sortedBy { it.endDurationFromBoot }.forEach {
                    val end=it.getEndInstant(boot)
                    if(end>=changedAt) publish(policy.accept(it.value,end,now,zone,it.getStartInstant(boot)))
                }
            }
            override fun onRegistrationFailed(throwable: Throwable) { publish(Measurement.Unavailable("Health Services registration failed")) }
            override fun onPermissionLost() { policy.reset(); publish(Measurement.PermissionRequired) }
        }
        val receiver=object: BroadcastReceiver() { override fun onReceive(c: Context,i: Intent) {
            policy.reset(); changedAt=Instant.now(); publish(Measurement.Loading)
        } }
        context.registerReceiver(receiver,IntentFilter().apply { addAction(Intent.ACTION_TIME_CHANGED); addAction(Intent.ACTION_TIMEZONE_CHANGED); addAction(Intent.ACTION_DATE_CHANGED) })
        val registration=launch {
            try {
                android.util.Log.i("TiltClockHealth","Checking daily steps capabilities")
                val capabilities=withTimeout(10_000) { client.getCapabilitiesAsync().awaitHealth() }
                android.util.Log.i("TiltClockHealth","Daily steps capabilities received")
                if(DataType.STEPS_DAILY !in capabilities.supportedDataTypesPassiveMonitoring) {
                    publish(Measurement.Unavailable("STEPS_DAILY unsupported")); return@launch
                }
                if(context.checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION)!=PackageManager.PERMISSION_GRANTED) {
                    publish(Measurement.PermissionRequired); return@launch
                }
                registered=true
                client.setPassiveListenerCallback(PassiveListenerConfig.builder().setDataTypes(setOf(DataType.STEPS_DAILY)).build(),callback)
            } catch(cancel: CancellationException) {
                currentCoroutineContext().ensureActive()
                android.util.Log.i("TiltClockHealth","Capability request unavailable or cancelled")
                publish(Measurement.Unavailable("Health Services request unavailable"))
            }
            catch(failure: SecurityException) { publish(Measurement.PermissionRequired) }
            catch(failure: Exception) { publish(Measurement.Unavailable("Health Services unavailable")) }
        }
        val expiry=launch {
            val started=Instant.now()
            while(isActive) {
                delay(10_000)
                if(registered && context.checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION)!=PackageManager.PERMISSION_GRANTED) {
                    policy.reset(); publish(Measurement.PermissionRequired)
                } else if(status is Measurement.Available) publish(policy.current(Instant.now(),ZoneId.systemDefault()))
                else if(status==Measurement.Loading && Duration.between(started,Instant.now()).seconds>60) publish(Measurement.Unavailable("Waiting for provider daily total"))
            }
        }
        awaitClose {
            registration.cancel(); expiry.cancel(); context.unregisterReceiver(receiver)
            if(registered) client.clearPassiveListenerCallbackAsync()
        }
    }
}
