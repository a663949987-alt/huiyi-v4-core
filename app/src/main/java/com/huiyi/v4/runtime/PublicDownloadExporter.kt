package com.huiyi.v4.runtime

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File

data class ExportedTextFile(
    val displayPath: String,
    val privateFallbackFile: File? = null
)

class PublicDownloadExporter(
    private val context: Context
) {
    fun exportText(
        fileName: String,
        text: String,
        mimeType: String = "text/markdown",
        relativePath: String = "HuiyiV4"
    ): Result<ExportedTextFile> = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, mimeType)
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/$relativePath")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                ?: error("无法创建下载文件")
            resolver.openOutputStream(uri)?.use { stream ->
                stream.write(text.toByteArray(Charsets.UTF_8))
            } ?: error("无法写入下载文件")
            values.clear()
            values.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            ExportedTextFile("Download/$relativePath/$fileName")
        } else {
            val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), relativePath)
            dir.mkdirs()
            val file = File(dir, fileName)
            file.writeText(text, Charsets.UTF_8)
            ExportedTextFile(file.absolutePath)
        }
    }

    fun fallbackToPrivate(fileName: String, text: String, subDirectory: String = "debug"): ExportedTextFile {
        val file = File(context.filesDir, "$subDirectory/$fileName")
        file.parentFile?.mkdirs()
        file.writeText(text, Charsets.UTF_8)
        return ExportedTextFile(file.absolutePath, file)
    }
}
