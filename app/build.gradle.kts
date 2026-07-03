plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("org.jetbrains.kotlin.plugin.serialization")
}

import java.util.Properties

val localProps = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) file.inputStream().use(::load)
}

android {
    namespace = "com.huiyi.v4"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.huiyi.v4"
        minSdk = 29
        targetSdk = 35
        versionCode = 422
        versionName = "4.1.8b"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "HUIYI_API_BASE_URL", "\"${localProps.getProperty("huiyi.api.baseUrl", "https://toapis.com/v1")}\"")
        buildConfigField("String", "HUIYI_API_KEY", "\"${localProps.getProperty("huiyi.api.key", "")}\"")
        buildConfigField("String", "HUIYI_API_MODEL", "\"${localProps.getProperty("huiyi.api.model", "gpt-5.5")}\"")
        buildConfigField("Long", "HUIYI_API_TIMEOUT_SECONDS", "${localProps.getProperty("huiyi.api.timeoutSeconds", "60")}L")
        buildConfigField("String", "HUIYI_UPDATE_BASE_URL", "\"${localProps.getProperty("huiyi.update.baseUrl", "")}\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.navigation:navigation-compose:2.8.3")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    kapt("androidx.room:room-compiler:2.6.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
}
