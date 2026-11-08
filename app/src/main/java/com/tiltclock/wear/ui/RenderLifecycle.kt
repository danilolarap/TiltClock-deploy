package com.tiltclock.wear.ui

import android.content.*
import android.os.PowerManager
import android.os.Build
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.*
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.tiltclock.wear.render.FilamentRenderer
import com.tiltclock.wear.sensors.OrientationController

@Composable
fun RenderLifecycle(renderer: FilamentRenderer,sensors: OrientationController,blocked: Boolean,onForeground: (Boolean)->Unit = {},sensorsEnabled: Boolean=true) {
    val owner = LocalLifecycleOwner.current; val context = LocalContext.current
    DisposableEffect(owner,renderer,sensors,blocked,sensorsEnabled) {
        val power = context.getSystemService(PowerManager::class.java)
        fun update() {
            val resumed=owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
            val foreground=ActivityPolicy.measurements(resumed,power.isInteractive)
            onForeground(foreground)
            val active = ActivityPolicy.render(resumed,power.isInteractive,blocked)
            renderer.setActive(active)
            if (active && sensorsEnabled) sensors.start() else sensors.stop()
        }
        val observer = LifecycleEventObserver { _,_ -> update() }
        val receiver = object : BroadcastReceiver() { override fun onReceive(c: Context,i: Intent) = update() }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF); addAction(Intent.ACTION_SCREEN_ON)
        }
        if (Build.VERSION.SDK_INT >= 33) context.registerReceiver(receiver,filter,Context.RECEIVER_NOT_EXPORTED)
        else context.registerReceiver(receiver,filter)
        owner.lifecycle.addObserver(observer); update()
        onDispose { context.unregisterReceiver(receiver); owner.lifecycle.removeObserver(observer)
            sensors.stop(); renderer.setActive(false); onForeground(false) }
    }
}
