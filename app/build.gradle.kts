plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("org.jetbrains.kotlin.plugin.serialization")
}

import java.util.Properties

fun loadPropertiesFile(name: String): Properties = Properties().apply {
    val file = rootProject.file(name)
    if (file.exists()) file.inputStream().use(::load)
}

val localProps = loadPropertiesFile("local.properties")
val cloudProps = loadPropertiesFile("huiyi-cloud.properties")
val envLocalProps = loadPropertiesFile(".env.local")

fun localConfig(vararg keys: String, default: String = ""): String {
    keys.forEach { key ->
        cloudProps.getProperty(key)?.takeIf { it.isNotBlank() }?.let { return it }
        envLocalProps.getProperty(key)?.takeIf { it.isNotBlank() }?.let { return it }
        localProps.getProperty(key)?.takeIf { it.isNotBlank() }?.let { return it }
        System.getenv(key)?.takeIf { it.isNotBlank() }?.let { return it }
    }
    return default
}

fun String.asBuildConfigString(): String {
    val escaped = replace("\\", "\\\\").replace("\"", "\\\"")
    return "\"$escaped\""
}

android {
    namespace = "com.huiyi.v4"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.huiyi.v4"
        minSdk = 29
        targetSdk = 35
        versionCode = 444
        versionName = "4.1.25"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "HUIYI_API_BASE_URL", "\"${localProps.getProperty("huiyi.api.baseUrl", "https://toapis.com/v1")}\"")
        buildConfigField("String", "HUIYI_API_KEY", "\"\"")
        buildConfigField("String", "HUIYI_API_MODEL", "\"${localProps.getProperty("huiyi.api.model", "gpt-5.5")}\"")
        buildConfigField("Long", "HUIYI_API_TIMEOUT_SECONDS", "${localProps.getProperty("huiyi.api.timeoutSeconds", "60")}L")
        buildConfigField("String", "HUIYI_UPDATE_BASE_URL", "\"${localProps.getProperty("huiyi.update.baseUrl", "http://192.168.31.243:8787/latest.json")}\"")
        buildConfigField("String", "HUIYI_REVIEW_UPLOAD_ENDPOINT", "\"${localProps.getProperty("huiyi.reviewUpload.endpoint", "")}\"")
        buildConfigField("String", "HUIYI_REVIEW_UPLOAD_CLIENT_KEY", "\"${localProps.getProperty("huiyi.reviewUpload.clientKey", "")}\"")
        buildConfigField("String", "HUIYI_CLOUD_ANALYSIS_ENDPOINT", "\"${localProps.getProperty("huiyi.cloud.endpoint", "")}\"")
        buildConfigField("String", "HUIYI_CLOUD_ANALYSIS_CLIENT_ID", "\"${localProps.getProperty("huiyi.cloud.clientId", "huiyi-v4-dev")}\"")
        buildConfigField("String", "HUIYI_CLOUD_ANALYSIS_CLIENT_TOKEN", "\"\"")
        buildConfigField("String", "HUIYI_RELAY_BASE_URL", localConfig("huiyi.relay.baseUrl", "HUIYI_RELAY_BASE_URL").asBuildConfigString())
        buildConfigField("String", "HUIYI_RELAY_MODEL", localConfig("huiyi.relay.model", "HUIYI_RELAY_MODEL", default = "gpt-5.5").asBuildConfigString())
        buildConfigField("String", "HUIYI_RELAY_API_KEY", localConfig("huiyi.relay.apiKey", "HUIYI_RELAY_API_KEY").asBuildConfigString())
        buildConfigField("Long", "HUIYI_RELAY_TIMEOUT_MS", "${localConfig("huiyi.relay.timeoutMs", "HUIYI_RELAY_TIMEOUT_MS", default = "6000")}L")
        buildConfigField("Boolean", "HUIYI_RELAY_CONFIGURED_FOR_BUILD", (localConfig("huiyi.relay.baseUrl", "HUIYI_RELAY_BASE_URL").isNotBlank() && localConfig("huiyi.relay.apiKey", "HUIYI_RELAY_API_KEY").isNotBlank()).toString())
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
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    kapt("androidx.room:room-compiler:2.6.1")

    debugImplementation("androidx.compose.ui:ui-tooling")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
}
