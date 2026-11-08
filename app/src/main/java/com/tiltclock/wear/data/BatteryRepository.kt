package com.tiltclock.wear.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import java.time.Instant
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class BatteryRepository(private val context: Context) {
    val readings: Flow<Measurement<Int>> = callbackFlow {
        trySend(Measurement.Loading)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
                val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
                val reading = if (level >= 0 && scale > 0 && level <= scale) {
                    Measurement.Available((level * 100L / scale).toInt(), "ACTION_BATTERY_CHANGED", Instant.now())
                } else Measurement.Unavailable("BatteryManager returned no valid level")
                trySend(reading)
            }
        }
        // Protected system broadcast; registration delivers the current sticky value.
        context.registerReceiver(receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        awaitClose { context.unregisterReceiver(receiver) }
    }
}
