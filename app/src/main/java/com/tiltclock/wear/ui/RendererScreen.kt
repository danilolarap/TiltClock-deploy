package com.tiltclock.wear.ui

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.*
import androidx.compose.ui.res.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tiltclock.wear.*
import com.tiltclock.wear.R
import com.tiltclock.wear.data.*
import com.tiltclock.wear.render.*
import com.tiltclock.wear.sensors.OrientationController
import kotlinx.coroutines.CancellationException
import androidx.compose.animation.*
import androidx.compose.animation.core.tween

/** Executable 3D integration; precision capsules, fonts and final settings belong to phase 3. */
@Composable
fun RendererScreen(model: ClockViewModel,state: ClockState) {
    WatchLocale(state.preferences.language) {
        CompositionLocalProvider(LocalWatchPalette provides WatchPalette(state.preferences.theme == "dark")) {
            WatchContent(model,state)
        }
    }
}

@Composable
private fun WatchContent(model: ClockViewModel,state: ClockState) {
    val config = LocalConfiguration.current
    val watch = config.uiMode and Configuration.UI_MODE_TYPE_MASK == Configuration.UI_MODE_TYPE_WATCH
    if (!config.isScreenRound || !watch) { FoundationScreen(state); return }
    val context = LocalContext.current.applicationContext
    val transforms = remember { TransformController() }
    val renderer = remember { FilamentRenderer(context.applicationContext,transforms,{ model.render.value = it },model::avatarAccepted) }
    var sensorsAvailable by remember { mutableStateOf(true) }
    val sensors = remember { OrientationController(context,transforms,
        { (context.getSystemService(android.content.Context.DISPLAY_SERVICE) as android.hardware.display.DisplayManager)
            .getDisplay(android.view.Display.DEFAULT_DISPLAY)?.rotation ?: 0 },{ sensorsAvailable = it }) }
    var picker by remember { mutableStateOf(false) }
    val render by model.render.collectAsStateWithLifecycle()
    val selection by model.selection.collectAsStateWithLifecycle()
    val id = selection?.first ?: state.preferences.avatar
    val sequence = selection?.second ?: 0
    LaunchedEffect(state.preferencesReady,id,sequence) {
        if (state.preferencesReady) {
            val start = System.nanoTime()
            model.render.value = render.copy(loading=true,error=null)
            try { renderer.load(id,model.prepare(id),start) }
            catch (cancel: CancellationException) { throw cancel }
            catch (failure: Exception) { renderer.error(failure) }
        }
    }
    LaunchedEffect(state.preferences.scale) { renderer.setScale(state.preferences.scale) }
    RenderLifecycle(renderer,sensors,picker)
    DisposableEffect(renderer) { onDispose { sensors.stop(); renderer.close() } }
    BackHandler(picker) { picker = false }
    Box(Modifier.fillMaxSize().background(Color.Black),contentAlignment=Alignment.Center) {
        BoxWithConstraints(Modifier.fillMaxSize().aspectRatio(1f).clip(CircleShape).background(LocalWatchPalette.current.background)) {
            val scale = maxWidth.value / WatchDimensions.REFERENCE
            AvatarSurface(renderer,Modifier.fillMaxSize())
            WatchRing(state.steps)
            // This layer handles only the avatar; later sibling controls receive their own touches.
            Box(Modifier.fillMaxSize().pointerInput(picker) {
                if (!picker) detectDragGestures(onDragStart={ transforms.beginDrag() },
                    onDragEnd={ transforms.endDrag() },onDragCancel={ transforms.endDrag() }) { event,delta ->
                    event.consume(); transforms.drag(delta.x,delta.y,size.width.toFloat())
                }
            })
            ClockCapsules(state,scale,Modifier.align(Alignment.TopCenter).padding(top=(8*scale).dp))
            Box(Modifier.align(Alignment.CenterStart).padding(start=(0*scale).dp).size(48.dp).clickable { picker = true },contentAlignment=Alignment.Center) {
                WatchIcon("pencil")
            }
            Column(Modifier.align(Alignment.CenterEnd),horizontalAlignment=Alignment.CenterHorizontally) {
                ZoomButton(1,model::zoom); ZoomButton(-1,model::zoom)
                if (render.animated) Box(Modifier.size(48.dp).clickable { renderer.toggleAnimation() },contentAlignment=Alignment.Center) {
                    WatchIcon(if (render.playing) "pause" else "play")
                }
            }
            Column(Modifier.align(Alignment.BottomCenter).padding(bottom=(4*scale).dp).fillMaxWidth(.8f),
                horizontalAlignment=Alignment.CenterHorizontally) {
                if (render.loading) WatchText(stringResource(R.string.avatar_loading),10f)
                if (render.error != null) WatchText(stringResource(R.string.avatar_error),10f)
                if (!sensorsAvailable) WatchText(stringResource(R.string.sensor_unavailable),9f)
                WatchBattery(state.battery,scale)
                UploadButton(scale) { picker = true }
            }
            ScreenShine()
            AnimatedVisibility(picker,enter=fadeIn(tween(280))+slideInVertically(tween(280)) { it/12 },
                exit=fadeOut(tween(280))+slideOutVertically(tween(280)) { it/12 }) {
                CircularSettings(state,render,model) { picker=false }
            }
        }
    }
}

@Composable private fun Text(value: String,size: Int) = BasicText(value,style=TextStyle(
    color=Color(0xFF0F172A),fontSize=size.sp,textAlign=TextAlign.Center))
