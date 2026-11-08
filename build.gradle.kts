buildscript {
    // Upgrade AGP's built-in Kotlin toolchain to match Filament's published stdlib metadata.
    dependencies { classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.4.0") }
}

plugins {
    id("com.android.application") version "9.4.1" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.4.0" apply false
}
