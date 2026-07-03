package com.huiyi.v4.runtime

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.huiyi.v4.BuildConfig
import com.huiyi.v4.domain.cloud.CloudAnalysisConfig
import com.huiyi.v4.domain.cloud.CloudAnalysisInput
import com.huiyi.v4.domain.cloud.CloudAnalysisOutput
import com.huiyi.v4.domain.cloud.CloudAnalysisRepository
import com.huiyi.v4.domain.cloud.CloudAnalysisService
import com.huiyi.v4.domain.cloud.CloudProviderType
import com.huiyi.v4.domain.cloud.CloudRuntimeSettings

class RuntimeCloudSettingsRepository(
    private val prefs: SharedPreferences,
    private val secureStorageAvailable: Boolean
) {
    fun load(): CloudRuntimeSettings {
        val baseUrl = prefs.getString(KEY_BASE_URL, "").orEmpty()
        val apiKeyConfigured = secureStorageAvailable && prefs.getString(KEY_API_KEY, "").orEmpty().isNotBlank()
        val buildConfigured = BuildConfig.HUIYI_RELAY_CONFIGURED_FOR_BUILD
        val effectiveBaseUrl = baseUrl.ifBlank { BuildConfig.HUIYI_RELAY_BASE_URL }
        val effectiveModel = prefs.getString(KEY_MODEL, "gpt-5.5").orEmpty()
            .ifBlank { BuildConfig.HUIYI_RELAY_MODEL.ifBlank { "gpt-5.5" } }
        val effectiveTimeoutMs = prefs.getLong(KEY_TIMEOUT_MS, BuildConfig.HUIYI_RELAY_TIMEOUT_MS)
            .coerceIn(1000L, 30000L)
        return CloudRuntimeSettings(
            cloudEnabled = (secureStorageAvailable && prefs.getBoolean(KEY_ENABLED, false)) || buildConfigured,
            providerType = prefs.getString(KEY_PROVIDER_TYPE, CloudProviderType.OPENAI_COMPATIBLE_RELAY).orEmpty()
                .ifBlank { CloudProviderType.OPENAI_COMPATIBLE_RELAY },
            baseUrl = effectiveBaseUrl,
            model = effectiveModel,
            timeoutMs = effectiveTimeoutMs,
            relayApiKeyConfigured = apiKeyConfigured || BuildConfig.HUIYI_RELAY_API_KEY.isNotBlank(),
            relayApiKeyStoredSecurely = (secureStorageAvailable && apiKeyConfigured) || buildConfigured,
            relaySecureStorageAvailable = secureStorageAvailable,
            relayApiKeyStorageMode = when {
                secureStorageAvailable && apiKeyConfigured -> "ANDROID_KEYSTORE_ENCRYPTED_SHARED_PREFERENCES"
                buildConfigured -> "PRECONFIGURED_BUILD_REDACTED"
                secureStorageAvailable -> "ANDROID_KEYSTORE_ENCRYPTED_SHARED_PREFERENCES"
                else -> "DEBUG_ONLY_INSECURE_STORAGE"
            }
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
        val safeCloudEnabled = secureStorageAvailable && cloudEnabled
        prefs.edit()
            .putBoolean(KEY_ENABLED, safeCloudEnabled)
            .putString(KEY_PROVIDER_TYPE, providerType.ifBlank { CloudProviderType.OPENAI_COMPATIBLE_RELAY })
            .putString(KEY_BASE_URL, baseUrl.trim())
            .putString(KEY_MODEL, model.trim().ifBlank { "gpt-5.5" })
            .putLong(KEY_TIMEOUT_MS, timeoutMs.coerceIn(1000L, 30000L))
            .apply {
                if (!secureStorageAvailable) {
                    remove(KEY_API_KEY)
                } else apiKeyInput?.let { key ->
                    if (key.isBlank()) remove(KEY_API_KEY) else putString(KEY_API_KEY, key.trim())
                }
            }
            .apply()
        return load()
    }

    fun currentConfig(): CloudAnalysisConfig {
        val settings = load()
        val apiKey = when {
            secureStorageAvailable && prefs.getString(KEY_API_KEY, "").orEmpty().isNotBlank() -> prefs.getString(KEY_API_KEY, "").orEmpty()
            BuildConfig.HUIYI_RELAY_CONFIGURED_FOR_BUILD -> BuildConfig.HUIYI_RELAY_API_KEY
            else -> ""
        }
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

    companion object {
        private const val KEY_ENABLED = "relay_cloud_enabled"
        private const val KEY_PROVIDER_TYPE = "relay_provider_type"
        private const val KEY_BASE_URL = "relay_base_url"
        private const val KEY_MODEL = "relay_model"
        private const val KEY_TIMEOUT_MS = "relay_timeout_ms"
        private const val KEY_API_KEY = "relay_api_key"

        fun create(context: Context): RuntimeCloudSettingsRepository {
            return runCatching {
                val masterKey = MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build()
                val encryptedPrefs = EncryptedSharedPreferences.create(
                    context,
                    "huiyi-cloud-runtime-secure",
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                )
                RuntimeCloudSettingsRepository(encryptedPrefs, secureStorageAvailable = true)
            }.getOrElse {
                RuntimeCloudSettingsRepository(
                    context.getSharedPreferences("huiyi-cloud-runtime-insecure-disabled", Context.MODE_PRIVATE),
                    secureStorageAvailable = false
                )
            }
        }
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
