package com.huiyi.v4.runtime

import android.content.SharedPreferences
import com.huiyi.v4.domain.cloud.CloudAnalysisConfig
import com.huiyi.v4.domain.cloud.CloudAnalysisInput
import com.huiyi.v4.domain.cloud.CloudAnalysisOutput
import com.huiyi.v4.domain.cloud.CloudAnalysisRepository
import com.huiyi.v4.domain.cloud.CloudAnalysisService
import com.huiyi.v4.domain.cloud.CloudProviderType
import com.huiyi.v4.domain.cloud.CloudRuntimeSettings

class RuntimeCloudSettingsRepository(
    private val prefs: SharedPreferences
) {
    fun load(): CloudRuntimeSettings {
        val baseUrl = prefs.getString(KEY_BASE_URL, "").orEmpty()
        val apiKeyConfigured = prefs.getString(KEY_API_KEY, "").orEmpty().isNotBlank()
        return CloudRuntimeSettings(
            cloudEnabled = prefs.getBoolean(KEY_ENABLED, false),
            providerType = prefs.getString(KEY_PROVIDER_TYPE, CloudProviderType.OPENAI_COMPATIBLE_RELAY).orEmpty()
                .ifBlank { CloudProviderType.OPENAI_COMPATIBLE_RELAY },
            baseUrl = baseUrl,
            model = prefs.getString(KEY_MODEL, "gpt-5.5").orEmpty().ifBlank { "gpt-5.5" },
            timeoutMs = prefs.getLong(KEY_TIMEOUT_MS, 6000L),
            relayApiKeyConfigured = apiKeyConfigured,
            relayApiKeyStoredSecurely = false,
            relayApiKeyStorageMode = "DEBUG_ONLY_INSECURE_STORAGE"
        )
    }

    fun save(
        cloudEnabled: Boolean,
        providerType: String,
        baseUrl: String,
        model: String,
        timeoutMs: Long,
        apiKeyInput: String?
    ): CloudRuntimeSettings {
        prefs.edit()
            .putBoolean(KEY_ENABLED, cloudEnabled)
            .putString(KEY_PROVIDER_TYPE, providerType.ifBlank { CloudProviderType.OPENAI_COMPATIBLE_RELAY })
            .putString(KEY_BASE_URL, baseUrl.trim())
            .putString(KEY_MODEL, model.trim().ifBlank { "gpt-5.5" })
            .putLong(KEY_TIMEOUT_MS, timeoutMs.coerceIn(1000L, 30000L))
            .apply {
                apiKeyInput?.let { key ->
                    if (key.isBlank()) remove(KEY_API_KEY) else putString(KEY_API_KEY, key.trim())
                }
            }
            .apply()
        return load()
    }

    fun currentConfig(): CloudAnalysisConfig {
        val settings = load()
        val apiKey = prefs.getString(KEY_API_KEY, "").orEmpty()
        return CloudAnalysisConfig(
            cloudEnabled = settings.cloudEnabled,
            providerType = settings.providerType,
            endpoint = settings.baseUrl,
            model = settings.model,
            timeoutMs = settings.timeoutMs,
            clientId = "huiyi-v4-runtime",
            apiKey = apiKey,
            relayApiKeyStoredSecurely = settings.relayApiKeyStoredSecurely
        )
    }

    private companion object {
        const val KEY_ENABLED = "relay_cloud_enabled"
        const val KEY_PROVIDER_TYPE = "relay_provider_type"
        const val KEY_BASE_URL = "relay_base_url"
        const val KEY_MODEL = "relay_model"
        const val KEY_TIMEOUT_MS = "relay_timeout_ms"
        const val KEY_API_KEY = "relay_api_key"
    }
}

class RuntimeCloudAnalysisService(
    private val settingsRepository: RuntimeCloudSettingsRepository
) : CloudAnalysisService {
    override val config: CloudAnalysisConfig
        get() = settingsRepository.currentConfig()

    override suspend fun analyze(input: CloudAnalysisInput): Result<CloudAnalysisOutput> {
        val currentConfig = settingsRepository.currentConfig()
        return CloudAnalysisRepository(currentConfig).analyze(input)
    }
}
