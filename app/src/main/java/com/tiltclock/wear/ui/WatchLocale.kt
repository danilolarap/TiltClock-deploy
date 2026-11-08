package com.tiltclock.wear.ui

import android.content.res.Configuration
import android.os.LocaleList
import androidx.compose.runtime.*
import androidx.compose.ui.platform.*
import java.util.Locale

@Composable
fun WatchLocale(language: String, content: @Composable () -> Unit) {
    val registry=requireNotNull(androidx.activity.compose.LocalActivityResultRegistryOwner.current)
    val base = LocalContext.current
    val current = LocalConfiguration.current
    val configuration = remember(current, language) { Configuration(current).apply { setLocales(LocaleList(Locale.forLanguageTag(language))) } }
    val context = remember(base, configuration) { base.createConfigurationContext(configuration) }
    CompositionLocalProvider(LocalContext provides context, LocalConfiguration provides configuration,
        androidx.activity.compose.LocalActivityResultRegistryOwner provides registry, content = content)
}
