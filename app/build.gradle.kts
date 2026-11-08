plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.tiltclock.wear"
    compileSdk = 37
    buildToolsVersion = "36.0.0"
    defaultConfig {
        applicationId = "com.tiltclock.wear"
        minSdk = 30
        targetSdk = 37
        versionCode = 4
        versionName = "0.4.0-phase4"
    }
    buildFeatures { compose = true }
    bundle { language { enableSplit = false } }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    androidResources { noCompress += "glb" }
    // Keep signing material inside this independent workspace.
    val workspaceDebug = signingConfigs.create("workspaceDebug") {
        storeFile = rootProject.file(".android/debug.keystore")
        storePassword = "android"
        keyAlias = "androiddebugkey"
        keyPassword = "android"
    }
    buildTypes.getByName("debug") { signingConfig = workspaceDebug }
}

dependencies {
    implementation("androidx.health:health-services-client:1.1.0")
    implementation("androidx.wear.compose:compose-foundation:1.5.6")
    implementation("com.google.android.filament:filament-android:1.77.3")
    implementation("com.google.android.filament:gltfio-android:1.77.3")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.compose.foundation:foundation:1.11.2")
    implementation("androidx.compose.ui:ui:1.11.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0")
    implementation("androidx.datastore:datastore-preferences:1.2.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.11.0")
    testImplementation("junit:junit:4.13.2")
}
