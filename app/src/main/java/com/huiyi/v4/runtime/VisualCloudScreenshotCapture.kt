package com.huiyi.v4.runtime

import android.accessibilityservice.AccessibilityService
import android.graphics.Bitmap
import android.os.Build
import android.util.Base64
import android.view.Display
import com.huiyi.v4.accessibility.HuiyiAccessibilityService
import com.huiyi.v4.domain.cloud.CloudVisualEvidence
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resume

class VisualCloudScreenshotCapture {
    suspend fun capture(service: HuiyiAccessibilityService?): CloudVisualEvidence? {
        val bitmap = captureBitmap(service).getOrNull() ?: return null
        val scaled = bitmap.scaleForCloud()
        val bytes = ByteArrayOutputStream().use { out ->
            scaled.compress(Bitmap.CompressFormat.JPEG, 72, out)
            out.toByteArray()
        }
        if (scaled !== bitmap) scaled.recycle()
        bitmap.recycle()
        return CloudVisualEvidence(
            imageBase64 = Base64.encodeToString(bytes, Base64.NO_WRAP),
            mimeType = "image/jpeg",
            width = scaled.width,
            height = scaled.height,
            source = "accessibility_take_screenshot_runtime_only"
        )
    }

    private suspend fun captureBitmap(service: HuiyiAccessibilityService?): Result<Bitmap> {
        if (service == null) return Result.failure(IllegalStateException("accessibility_service_not_connected"))
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return Result.failure(IllegalStateException("takeScreenshot_requires_android_11"))
        }
        return suspendCancellableCoroutine { continuation ->
            try {
                service.takeScreenshot(
                    Display.DEFAULT_DISPLAY,
                    service.mainExecutor,
                    object : AccessibilityService.TakeScreenshotCallback {
                        override fun onSuccess(screenshot: AccessibilityService.ScreenshotResult) {
                            val hardwareBuffer = screenshot.hardwareBuffer
                            val bitmap = Bitmap.wrapHardwareBuffer(hardwareBuffer, screenshot.colorSpace)
                            val copy = bitmap?.copy(Bitmap.Config.ARGB_8888, false)
                            hardwareBuffer.close()
                            continuation.resume(
                                if (copy == null) {
                                    Result.failure(IllegalStateException("screenshot_bitmap_null"))
                                } else {
                                    Result.success(copy)
                                }
                            )
                        }

                        override fun onFailure(errorCode: Int) {
                            continuation.resume(Result.failure(IllegalStateException("takeScreenshot_failed_$errorCode")))
                        }
                    }
                )
            } catch (error: Throwable) {
                continuation.resume(Result.failure(error))
            }
        }
    }

    private fun Bitmap.scaleForCloud(): Bitmap {
        val maxWidth = 864
        if (width <= maxWidth) return this
        val ratio = maxWidth.toFloat() / width.toFloat()
        return Bitmap.createScaledBitmap(this, maxWidth, (height * ratio).toInt().coerceAtLeast(1), true)
    }
}
