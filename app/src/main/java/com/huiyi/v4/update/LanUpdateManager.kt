package com.huiyi.v4.update

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import androidx.core.content.FileProvider
import com.huiyi.v4.domain.model.UpdateManifest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope
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
        .readTimeout(180, TimeUnit.SECONDS)
        .build()

    suspend fun check(updateUrl: String): Result<Pair<UpdateManifest, String>> = withContext(Dispatchers.IO) {
        runCatching {
            fetchManifest(normalizeLatestUrl(updateUrl))
        }
    }

    suspend fun discoverAndCheck(port: Int = 8787): Result<Triple<String, UpdateManifest, String>> = withContext(Dispatchers.IO) {
        runCatching {
            val probeClient = client.newBuilder()
                .connectTimeout(500, TimeUnit.MILLISECONDS)
                .readTimeout(900, TimeUnit.MILLISECONDS)
                .build()

            discoverySeedUrls(port).forEach { url ->
                fetchManifestOrNull(probeClient, url)?.let { (manifest, raw) ->
                    return@runCatching Triple(url, manifest, raw)
                }
            }

            supervisorScope {
                discoveryPrefixes().flatMap { prefix ->
                    (1..254).map { host ->
                        async(Dispatchers.IO) {
                            val url = "http://$prefix.$host:$port/latest.json"
                            fetchManifestOrNull(probeClient, url)?.let { (manifest, raw) ->
                                Triple(url, manifest, raw)
                            }
                        }
                    }
                }.awaitAll().firstOrNull()
            } ?: error("没有发现会意局域网更新服务。请确认手机和电脑在同一 Wi-Fi，电脑防火墙允许 8787 端口，或临时填写 http://192.168.31.243:8787")
        }
    }

    suspend fun download(updateUrl: String, manifest: UpdateManifest): Result<File> = withContext(Dispatchers.IO) {
        runCatching {
            val apkUrl = resolveApkUrl(updateUrl, manifest.apkUrl)
            val request = Request.Builder()
                .url(apkUrl)
                .header("Cache-Control", "no-cache")
                .build()
            val dir = File(context.cacheDir, "lan_update").apply { mkdirs() }
            val shaSuffix = manifest.sha256.take(8).ifBlank { "nosha" }
            val file = File(dir, "huiyi-update-${manifest.versionCode}-$shaSuffix.apk")
            val tempFile = File(dir, "${file.name}.download")
            if (tempFile.exists()) tempFile.delete()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) error("下载失败：HTTP ${response.code}")
                val body = response.body ?: error("APK 响应为空")
                tempFile.outputStream().use { out -> body.byteStream().copyTo(out) }
            }
            if (manifest.sha256.isNotBlank()) {
                val actual = sha256(tempFile)
                check(actual.equals(manifest.sha256, ignoreCase = true)) {
                    "SHA-256 不匹配：$actual"
                }
            }
            if (file.exists()) file.delete()
            check(tempFile.renameTo(file)) { "APK save failed" }
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

    private fun fetchManifest(latestUrl: String): Pair<UpdateManifest, String> {
        val request = Request.Builder().url(latestUrl).build()
        val raw = client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("检查更新失败：HTTP ${response.code}")
            response.body?.string() ?: error("latest.json 为空")
        }
        return Json.decodeFromString(UpdateManifest.serializer(), raw) to raw
    }

    private fun fetchManifestOrNull(client: OkHttpClient, latestUrl: String): Pair<UpdateManifest, String>? {
        return runCatching {
            val request = Request.Builder().url(latestUrl).build()
            val raw = client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) error("HTTP ${response.code}")
                response.body?.string() ?: error("empty")
            }
            Json.decodeFromString(UpdateManifest.serializer(), raw) to raw
        }.getOrNull()
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

    @Suppress("DEPRECATION")
    private fun localIpv4Prefix(): String? {
        val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager ?: return null
        val ip = wifi.connectionInfo?.ipAddress ?: return null
        if (ip == 0) return null
        val a = ip and 0xff
        val b = ip shr 8 and 0xff
        val c = ip shr 16 and 0xff
        return "$a.$b.$c"
    }

    private fun discoverySeedUrls(port: Int): List<String> {
        return listOf(
            "http://192.168.31.243:$port/latest.json",
            "http://192.168.31.1:$port/latest.json"
        ).distinct()
    }

    private fun discoveryPrefixes(): List<String> {
        return (listOfNotNull(localIpv4Prefix()) + listOf(
            "192.168.31",
            "192.168.1",
            "192.168.0",
            "10.0.0",
            "172.16.0"
        )).distinct()
    }
}
