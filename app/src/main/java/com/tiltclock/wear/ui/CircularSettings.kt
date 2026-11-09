package com.tiltclock.wear.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tiltclock.wear.ClockState
import com.tiltclock.wear.ClockViewModel
import com.tiltclock.wear.R
import com.tiltclock.wear.data.AvatarCatalog
import com.tiltclock.wear.render.RenderState
import kotlinx.coroutines.launch

/** A complete first screen: no scrolling is needed to discover the three settings. */
@Composable
fun CircularSettings(state: ClockState, render: RenderState, model: ClockViewModel, close: () -> Unit) {
    var page by rememberSaveable { mutableStateOf("settings") }
    val palette = LocalWatchPalette.current
    val names = stringArrayResource(R.array.avatar_names)
    val currentId = render.avatar ?: model.rendererId(state.preferences)
    val currentName = avatarName(currentId, names)
    val back = { if (page == "settings") close() else page = "settings" }
    BackHandler(onBack = back)

    // An opaque surface gives both themes clear contrast over the underlying 3D scene.
    BoxWithConstraints(Modifier.fillMaxSize().background(palette.background)) {
        val diameter = maxWidth
        val screenHeight = maxHeight
        val cardHeight = (screenHeight * .26f).coerceAtLeast(48.dp)
        val heading = stringResource(when (page) {
            "avatar" -> R.string.models_title
            "theme" -> R.string.theme_title
            "language" -> R.string.language_title
            else -> R.string.settings_title
        })

        if (page == "avatar") {
            val ids = remember(currentId) {
                AvatarCatalog.ids + listOfNotNull(currentId.takeIf { it.startsWith("import_") })
            }
            val pager = rememberPagerState(initialPage = ids.indexOf(currentId).coerceAtLeast(0)) { ids.size }
            val scope = rememberCoroutineScope()
            val focus = remember { FocusRequester() }
            // Accumulate a crown detent, then change one page; prevent overlapping animations.
            var rotaryPixels by remember { mutableFloatStateOf(0f) }
            fun step(direction: Int) {
                if (!pager.isScrollInProgress) {
                    val target = (pager.currentPage + direction).coerceIn(0, ids.lastIndex)
                    scope.launch { pager.animateScrollToPage(target) }
                }
            }
            LaunchedEffect(Unit) { focus.requestFocus() }
            SettingsHeader(heading, stringResource(R.string.model_counter, pager.currentPage + 1, ids.size),
                Modifier.align(Alignment.TopCenter).padding(top = screenHeight * .045f), back)
            Box(Modifier.fillMaxWidth().height(screenHeight * .41f)
                .align(Alignment.TopCenter).offset(y = screenHeight * .28f)
                .onRotaryScrollEvent {
                    rotaryPixels += it.verticalScrollPixels
                    if (kotlin.math.abs(rotaryPixels) >= 24f) {
                        step(if (rotaryPixels > 0) 1 else -1)
                        rotaryPixels = 0f
                    }
                    true
                }.focusRequester(focus).focusable()) {
                HorizontalPager(pager, Modifier.fillMaxSize(),
                    flingBehavior = PagerDefaults.flingBehavior(pager, snapPositionalThreshold = .25f)) { index ->
                    val id = ids[index]
                    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
                        AvatarThumbnail(id, Modifier.size(screenHeight * .28f))
                        SettingsText(avatarName(id, names), 13f,
                            Modifier.fillMaxWidth(.55f), maxLines = 1)
                    }
                }
                SettingsIconButton("previous", stringResource(R.string.previous_model),
                    Modifier.align(Alignment.CenterStart).padding(start = diameter * .025f),
                    enabled = pager.currentPage > 0) { step(-1) }
                SettingsIconButton("next", stringResource(R.string.next_model),
                    Modifier.align(Alignment.CenterEnd).padding(end = diameter * .025f),
                    enabled = pager.currentPage < ids.lastIndex) { step(1) }
            }
            val selectedId = ids[pager.currentPage]
            val isCurrent = selectedId == currentId
            val action = stringResource(if (isCurrent) R.string.current_model else R.string.use_model)
            Row(Modifier.align(Alignment.BottomCenter).padding(bottom = screenHeight * .06f)
                .width(diameter * .60f).heightIn(min = 48.dp).clip(CircleShape)
                .background(palette.cyan.copy(alpha = if (palette.dark) .24f else .16f))
                .border(1.dp, palette.cyan, CircleShape)
                .semantics { role = Role.Button }
                .clickable {
                    if (!isCurrent) model.selectAvatar(selectedId)
                    close()
                }.padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically) {
                WatchIcon(if (isCurrent) "check" else "apply", Modifier.size(18.dp))
                SettingsText(action, 13f, Modifier.weight(1f), maxLines = 2)
            }
        } else {
            SettingsHeader(heading, null,
                Modifier.align(Alignment.TopCenter).padding(top = screenHeight * .045f), back)
            Column(Modifier.align(Alignment.TopCenter).offset(y = screenHeight * .30f)
                .width(diameter * .70f)
                .then(if (page != "settings") Modifier.selectableGroup() else Modifier),
                verticalArrangement = Arrangement.spacedBy(screenHeight * .03f)) {
                when (page) {
                    "settings" -> {
                        SettingsCard(Modifier.fillMaxWidth().height(cardHeight), onClick = { page = "avatar" }) {
                            Row(Modifier.fillMaxSize().padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                AvatarThumbnail(currentId, Modifier.size(32.dp))
                                Column(Modifier.weight(1f)) {
                                    SettingsText(stringResource(R.string.models_title), 14f, align = TextAlign.Start, maxLines = 1, fit = true)
                                    SettingsText(currentName, 10f, color = palette.secondary, align = TextAlign.Start, maxLines = 1)
                                }
                                WatchIcon("next", Modifier.size(12.dp))
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            HubTile("theme", stringResource(R.string.theme_title),
                                stringResource(if (state.preferences.theme == "dark") R.string.dark_theme else R.string.light_theme),
                                Modifier.weight(1f).height(cardHeight)) { page = "theme" }
                            HubTile("language", stringResource(R.string.language_title),
                                if (state.preferences.language == "es") "Español" else "English",
                                Modifier.weight(1f).height(cardHeight)) { page = "language" }
                        }
                    }
                    "theme" -> {
                        ChoiceCard("sun", stringResource(R.string.light_theme), state.preferences.theme == "light",
                            Modifier.fillMaxWidth().height(cardHeight), swatch = Color(0xFFF8FAFC)) { model.setTheme("light") }
                        ChoiceCard("moon", stringResource(R.string.dark_theme), state.preferences.theme == "dark",
                            Modifier.fillMaxWidth().height(cardHeight), swatch = Color(0xFF0F172A)) { model.setTheme("dark") }
                    }
                    "language" -> {
                        ChoiceCard("ES", "Español", state.preferences.language == "es",
                            Modifier.fillMaxWidth().height(cardHeight)) { model.setLanguage("es") }
                        ChoiceCard("EN", "English", state.preferences.language == "en",
                            Modifier.fillMaxWidth().height(cardHeight)) { model.setLanguage("en") }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsHeader(title: String, subtitle: String?, modifier: Modifier, back: () -> Unit) {
    val home = title == stringResource(R.string.settings_title)
    Row(modifier.fillMaxWidth(.78f).heightIn(min = 48.dp), verticalAlignment = Alignment.CenterVertically) {
        SettingsIconButton(if (home) "close" else "previous",
            stringResource(if (home) R.string.close_settings else R.string.back_settings), onClick = back)
        Column(Modifier.weight(1f).padding(end = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            SettingsText(title, 15f, Modifier.semantics { heading() }, maxLines = 1)
            if (subtitle != null) SettingsText(subtitle, 10f, color = LocalWatchPalette.current.secondary)
        }
    }
}

@Composable
private fun SettingsCard(modifier: Modifier, selected: Boolean = false, radio: Boolean = false,
                         onClick: () -> Unit, content: @Composable () -> Unit) {
    val p = LocalWatchPalette.current
    val shape = RoundedCornerShape(18.dp)
    val interaction = if (radio) Modifier.selectable(selected, role = Role.RadioButton, onClick = onClick)
        else Modifier.clickable(role = Role.Button, onClick = onClick)
    Box(modifier.clip(shape)
        .background(if (selected) p.cyan.copy(alpha = if (p.dark) .22f else .12f) else if (p.dark) Color(0xFF172033) else Color.White)
        .border(if (selected) 2.dp else 1.dp, if (selected) p.cyan else p.border, shape)
        .then(interaction).semantics(mergeDescendants = true) {},
        contentAlignment = Alignment.Center) { content() }
}

@Composable
private fun HubTile(icon: String, title: String, value: String, modifier: Modifier, onClick: () -> Unit) {
    SettingsCard(modifier, onClick = onClick) {
        Column(Modifier.padding(vertical = 3.dp, horizontal = 2.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            if (LocalDensity.current.fontScale <= 1.15f) WatchIcon(icon, Modifier.size(16.dp), cyan = true)
            SettingsText(title, 12f, maxLines = 1, fit = true)
            SettingsText(value, 10f, color = LocalWatchPalette.current.secondary, maxLines = 1)
        }
    }
}

@Composable
private fun ChoiceCard(icon: String, title: String, selected: Boolean, modifier: Modifier,
                       swatch: Color? = null, onClick: () -> Unit) {
    SettingsCard(modifier, selected, radio = true, onClick) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 6.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (swatch != null) {
                Box(Modifier.size(28.dp).background(swatch, CircleShape).border(1.dp, Color.Gray, CircleShape),
                    contentAlignment = Alignment.Center) {
                    CompositionLocalProvider(LocalWatchPalette provides WatchPalette(icon == "moon")) {
                        WatchIcon(icon, Modifier.size(18.dp))
                    }
                }
            } else SettingsText(icon, 10f, Modifier.width(20.dp), color = LocalWatchPalette.current.cyan)
            SettingsText(title, 14f, Modifier.weight(1f), align = TextAlign.Start, maxLines = 1)
            if (selected) WatchIcon("check", Modifier.size(18.dp), cyan = true)
            else Box(Modifier.size(18.dp).border(1.dp, LocalWatchPalette.current.secondary, CircleShape))
        }
    }
}

@Composable
private fun SettingsIconButton(icon: String, label: String, modifier: Modifier = Modifier,
                               enabled: Boolean = true, onClick: () -> Unit) {
    Box(modifier.size(48.dp).clip(CircleShape)
        .background(LocalWatchPalette.current.border.copy(alpha = if (enabled) .55f else .12f))
        .semantics { contentDescription = label; role = Role.Button }
        .clickable(enabled = enabled, onClick = onClick), contentAlignment = Alignment.Center) {
        WatchIcon(icon, Modifier.size(20.dp).alpha(if (enabled) 1f else .25f))
    }
}

@Composable
private fun avatarName(id: String, names: Array<String>): String =
    AvatarCatalog.ids.indexOf(id).takeIf { it >= 0 }?.let { names[it] } ?: stringResource(R.string.imported_avatar)

/** Real offline renders of the bundled models; browsing never loads another 3D engine. */
@Composable
private fun AvatarThumbnail(id: String, modifier: Modifier) {
    val drawable = when (id) {
        "holo" -> R.drawable.avatar_holo
        "fox" -> R.drawable.avatar_fox
        "pajaro" -> R.drawable.avatar_pajaro
        "mapache" -> R.drawable.avatar_mapache
        "moneda" -> R.drawable.avatar_moneda
        "riot" -> R.drawable.avatar_riot
        else -> null
    }
    if (drawable != null) Image(painterResource(drawable), contentDescription = null,
        modifier = modifier.clip(RoundedCornerShape(12.dp)).background(Color(0xFFE2E8F0)).padding(4.dp),
        contentScale = ContentScale.Fit)
    else Box(modifier, contentAlignment = Alignment.Center) { WatchIcon("model", Modifier.size(28.dp), cyan = true) }
}

@Composable
private fun SettingsText(text: String, size: Float, modifier: Modifier = Modifier,
                         color: Color = LocalWatchPalette.current.text, align: TextAlign = TextAlign.Center,
                         maxLines: Int = 2, fit: Boolean = false) {
    BasicText(text, modifier, style = TextStyle(color = color, fontFamily = Inter,
        fontWeight = FontWeight.SemiBold, fontSize = size.sp, textAlign = align),
        maxLines = maxLines, overflow = TextOverflow.Ellipsis,
        autoSize = if (fit) TextAutoSize.StepBased(minFontSize = (size * .75f).sp, maxFontSize = size.sp) else null)
}
