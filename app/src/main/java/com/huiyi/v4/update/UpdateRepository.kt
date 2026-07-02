package com.huiyi.v4.update

import com.huiyi.v4.domain.model.UpdateManifest
import kotlinx.serialization.json.Json

class UpdateRepository(
    private val updateBaseUrl: String
) {
    fun parseLatest(rawJson: String): UpdateManifest = Json.decodeFromString(UpdateManifest.serializer(), rawJson)

    fun describeCurrentSource(): String = updateBaseUrl.ifBlank { "未配置更新源" }
}
