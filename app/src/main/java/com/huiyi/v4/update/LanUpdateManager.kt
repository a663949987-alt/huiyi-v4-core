package com.huiyi.v4.update

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.huiyi.v4.domain.model.UpdateManifest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

data class LanUpdateState(
    val updateUrl: String = "",
    val latestManifest: UpdateManifest? = null,
    val latestJsonRaw: String? = null,
    val downloadedApkPath: String? = null,
    val status: String = "未检查",
    val error: String? = null
)

class LanUpdateManager(
    private val context: Context
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun check(updateUrl: String): Result<Pair<UpdateManifest, String>> = withContext(Dispatchers.IO) {
        runCatching {
            val latestUrl = normalizeLatestUrl(updateUrl)
            val request = Request.Builder().url(latestUrl).build()
            val raw = client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) error("检查更新失败：HTTP ${response.code}")
                response.body?.string() ?: error("latest.json 为空")
            }
            Json.decodeFromString(UpdateManifest.serializer(), raw) to raw
        }
    }

    suspend fun download(updateUrl: String, manifest: UpdateManifest): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val apkUrl = resolveApkUrl(updateUrl, manifest.apkUrl)
            val request = Request.Builder().url(apkUrl).build()
            val dir = File(context.cacheDir, "lan_update").apply { mkdirs() }
            val file = File(dir, "huiyi-update-${manifest.versionCode}.apk")
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) error("下载失败：HTTP ${response.code}")
                val body = response.body ?: error("APK 响应为空")
                file.outputStream().use { out -> body.byteStream().copyTo(out) }
            }
            if (manifest.sha256.isNotBlank()) {
                val actual = sha256(file)
                check(actual.equals(manifest.sha256, ignoreCase = true)) {
                    "SHA-256 不匹配：$actual"
                }
            }
            file
        }
    }

    fun openInstaller(file: File): Result<Unit> = runCatching {
        val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun normalizeLatestUrl(updateUrl: String): String {
        val trimmed = updateUrl.trim().trimEnd('/')
        require(trimmed.isNotBlank()) { "请先填写局域网更新地址" }
        return if (trimmed.endsWith(".json")) trimmed else "$trimmed/latest.json"
    }

    private fun resolveApkUrl(updateUrl: String, apkUrl: String): String {
        if (apkUrl.startsWith("http://") || apkUrl.startsWith("https://")) return apkUrl
        val latestUrl = normalizeLatestUrl(updateUrl)
        val base = latestUrl.substringBeforeLast("/")
        return "$base/${apkUrl.trimStart('/')}"
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
