package com.tiltclock.wear.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.*
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.*
import androidx.compose.ui.unit.*
import com.tiltclock.wear.*
import com.tiltclock.wear.R
import com.tiltclock.wear.data.AvatarCatalog
import com.tiltclock.wear.render.RenderState
import kotlinx.coroutines.launch
import androidx.compose.foundation.gestures.scrollBy
import androidx.wear.compose.foundation.CurvedLayout
import androidx.wear.compose.foundation.basicCurvedText
import androidx.wear.compose.foundation.CurvedTextStyle

@Composable
fun CircularSettings(state: ClockState, render: RenderState, model: ClockViewModel, close: () -> Unit) {
    var page by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("settings") }
    BackHandler { if(page=="settings") close() else page="settings" }
    val p=LocalWatchPalette.current
    val scroll=rememberScrollState()
    val scope=rememberCoroutineScope()
    val focus=remember { FocusRequester() }
    LaunchedEffect(page) { scroll.scrollTo(0); focus.requestFocus() }
    val names=stringArrayResource(R.array.avatar_names)
    val heading=stringResource(R.string.settings_title)
    BoxWithConstraints(Modifier.fillMaxSize().background(p.glass)) {
        CurvedLayout { basicCurvedText(heading,style=CurvedTextStyle(color=p.text,fontSize=14.sp)) }
        val radius=maxWidth.value/2
        Column(Modifier.fillMaxSize().onRotaryScrollEvent { scope.launch { scroll.scrollBy(it.verticalScrollPixels) }; true }
            .focusRequester(focus).focusable().verticalScroll(scroll).padding(vertical=(radius-24).dp),
            horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.spacedBy(8.dp)) {
            CircularSettingRow(radius,stringResource(R.string.settings_title)) {}
            when(page) {
                "settings" -> {
                    CircularSettingRow(radius,"◇ Avatar",render.error?.let { stringResource(R.string.avatar_error) }
                        ?: if(render.loading) stringResource(R.string.avatar_loading) else names[AvatarCatalog.ids.indexOf(render.avatar).coerceAtLeast(0)]) { page="avatar" }
                    CircularSettingRow(radius,stringResource(R.string.theme_title),stringResource(if(state.preferences.theme=="dark") R.string.dark_theme else R.string.light_theme)) { page="theme" }
                    CircularSettingRow(radius,stringResource(R.string.language_title),if(state.preferences.language=="es") "Español" else "English") { page="language" }
                }
                "avatar" -> AvatarCatalog.ids.forEachIndexed { index,id ->
                    CircularSettingRow(radius,names[index],if(render.avatar==id) "✓" else "◇") { model.selectAvatar(id); page="settings" }
                }
                "theme" -> listOf("light" to R.string.light_theme,"dark" to R.string.dark_theme).forEach { (id,label) ->
                    CircularSettingRow(radius,stringResource(label),if(state.preferences.theme==id) "✓" else "") { model.setTheme(id) }
                }
                "language" -> listOf("es" to "Español","en" to "English").forEach { (id,label) ->
                    CircularSettingRow(radius,label,if(state.preferences.language==id) "✓" else "") { model.setLanguage(id) }
                }
            }
            CircularSettingRow(radius,stringResource(if(page=="settings") R.string.close_settings else R.string.back_settings)) {
                if(page=="settings") close() else page="settings"
            }
        }
    }
}

@Composable
private fun CircularSettingRow(radius: Float,title: String,subtitle: String?=null,click: () -> Unit) {
    val density=LocalDensity.current
    var y by remember { mutableFloatStateOf(0f) }
    val height = 64f * density.fontScale.coerceAtLeast(1f)
    val width=CircularGeometry.rowWidth(radius,y,height,8f).coerceAtMost(radius*1.65f)
    val p=LocalWatchPalette.current
    Box(Modifier.fillMaxWidth().height(height.dp).onGloballyPositioned {
        y=with(density) { (it.positionInRoot().y+it.size.height/2).toDp().value }-radius
    },contentAlignment=Alignment.Center) {
        Column(Modifier.width(width.dp).fillMaxHeight().clip(RoundedCornerShape(24.dp)).background(p.border)
            .clickable(enabled=width>=48,onClick=click).padding(horizontal=8.dp,vertical=10.dp),horizontalAlignment=Alignment.CenterHorizontally,
            verticalArrangement=Arrangement.Center) {
            WatchText(title,14f)
            if(!subtitle.isNullOrEmpty()) WatchText(subtitle,12f)
        }
    }
}
