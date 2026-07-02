package com.huiyi.v4.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class UpdateManifest(
    val versionName: String,
    val versionCode: Int,
    val apkUrl: String,
    val releaseNotes: String,
    val forceUpdate: Boolean,
    val sha256: String,
    val createdAt: Long
)
