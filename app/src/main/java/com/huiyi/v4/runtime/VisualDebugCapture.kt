package com.huiyi.v4.runtime

import android.accessibilityservice.AccessibilityService
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.view.Display
import com.huiyi.v4.accessibility.HuiyiAccessibilityService
import com.huiyi.v4.domain.capture.VisualDebugResult
import com.huiyi.v4.domain.model.Speaker
import com.huiyi.v4.domain.model.VisualBounds
import com.huiyi.v4.domain.pipeline.CurrentScreenPipelineResult
import com.huiyi.v4.domain.pipeline.RealDeviceScenario
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume

class VisualDebugCapture(
    private val outputDir: File
) {
    suspend fun capture(
        service: HuiyiAccessibilityService?,
        result: CurrentScreenPipelineResult,
        scenario: RealDeviceScenario
    ): VisualDebugResult {
        outputDir.mkdirs()
        val width = result.captureResult?.snapshot?.screenWidth?.takeIf { it > 0 } ?: 1080
        val height = result.captureResult?.snapshot?.screenHeight?.takeIf { it > 0 } ?: 2400
        val screenshot = captureScreenshot(service).getOrElse { error ->
            val placeholder = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            Canvas(placeholder).drawColor(Color.rgb(245, 245, 245))
            val overlay = drawOverlay(placeholder, result, scenario, screenshotCaptured = false, reason = error.message)
            val overlayFile = File(outputDir, "current_screen_overlay.png")
            overlayFile.outputStream().use { overlay.compress(Bitmap.CompressFormat.PNG, 92, it) }
            return VisualDebugResult(
                screenshotCaptured = false,
                screenshotUnavailable = true,
                reason = error.message ?: "takeScreenshot unavailable",
                screenshotPath = null,
                overlayImagePath = overlayFile.absolutePath,
                screenshotWidth = width,
                screenshotHeight = height,
                accessibilityBoundsProjected = result.captureResult?.accessibilityBoundsProjected == true,
                ocrUsed = false,
                visualTruthAvailable = result.captureResult?.visualTruthAvailable == true
            )
        }
        val screenshotFile = File(outputDir, "current_screen.png")
        screenshotFile.outputStream().use { screenshot.compress(Bitmap.CompressFormat.PNG, 92, it) }
        val overlay = drawOverlay(screenshot, result, scenario, screenshotCaptured = true, reason = null)
        val overlayFile = File(outputDir, "current_screen_overlay.png")
        overlayFile.outputStream().use { overlay.compress(Bitmap.CompressFormat.PNG, 92, it) }
        return VisualDebugResult(
            screenshotCaptured = true,
            screenshotUnavailable = false,
            reason = null,
            screenshotPath = screenshotFile.absolutePath,
            overlayImagePath = overlayFile.absolutePath,
            screenshotWidth = screenshot.width,
            screenshotHeight = screenshot.height,
            accessibilityBoundsProjected = result.captureResult?.accessibilityBoundsProjected == true,
            ocrUsed = false,
            visualTruthAvailable = result.captureResult?.visualTruthAvailable == true
        )
    }

    private suspend fun captureScreenshot(service: HuiyiAccessibilityService?): Result<Bitmap> {
        if (service == null) return Result.failure(IllegalStateException("accessibility_service_not_connected"))
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            return Result.failure(IllegalStateException("takeScreenshot_requires_android_11"))
        }
        return suspendCancellableCoroutine { continuation ->
            service.takeScreenshot(
                Display.DEFAULT_DISPLAY,
                service.mainExecutor,
                object : AccessibilityService.TakeScreenshotCallback {
                    override fun onSuccess(screenshot: AccessibilityService.ScreenshotResult) {
                        val hardwareBuffer = screenshot.hardwareBuffer
                        val bitmap = Bitmap.wrapHardwareBuffer(hardwareBuffer, screenshot.colorSpace)
                        val copy = bitmap?.copy(Bitmap.Config.ARGB_8888, false)
                        hardwareBuffer.close()
                        if (copy == null) {
                            continuation.resume(Result.failure(IllegalStateException("screenshot_bitmap_null")))
                        } else {
                            continuation.resume(Result.success(copy))
                        }
                    }

                    override fun onFailure(errorCode: Int) {
                        continuation.resume(Result.failure(IllegalStateException("takeScreenshot_failed_$errorCode")))
                    }
                }
            )
        }
    }

    private fun drawOverlay(
        source: Bitmap,
        result: CurrentScreenPipelineResult,
        scenario: RealDeviceScenario,
        screenshotCaptured: Boolean,
        reason: String?
    ): Bitmap {
        val bitmap = source.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(bitmap)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 34f
            style = Paint.Style.FILL
        }
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 28f
            style = Paint.Style.FILL
        }
        val messages = result.captureResult?.messages.orEmpty()
        val lastId = result.lastSpeakerDecision.lastEffectiveMessage?.id
        messages.forEachIndexed { index, message ->
            val color = when (message.speaker) {
                Speaker.ME -> Color.rgb(33, 117, 255)
                Speaker.OTHER -> Color.rgb(28, 150, 80)
                Speaker.SYSTEM -> Color.rgb(120, 120, 120)
                Speaker.UNKNOWN -> Color.rgb(220, 30, 30)
            }
            val projected = message.projectedBox ?: message.bubbleBounds ?: message.rowBounds ?: message.textBounds ?: message.bounds
            message.textBounds?.let { drawBox(canvas, it, Color.rgb(255, 180, 0), stroke = 2f) }
            projected?.let { box ->
                drawBox(canvas, box, color, stroke = if (message.id == lastId) 8f else 4f)
                drawLabel(canvas, box, "m${(index + 1).toString().padStart(3, '0')} ${message.speaker}", color, labelPaint)
            }
        }
        canvas.drawText("LastSpeakerDecision = ${result.lastSpeakerDecision.lastSpeaker ?: "UNKNOWN"}", 24f, 54f, textPaint)
        canvas.drawText("scenarioName = ${scenario.id}", 24f, 96f, textPaint)
        canvas.drawText("expected = ${scenario.expectedLastSpeaker ?: "NO_FIXED_EXPECTATION"}", 24f, 138f, textPaint)
        canvas.drawText("actual = ${result.lastSpeakerDecision.lastSpeaker ?: "UNKNOWN"}", 24f, 180f, textPaint)
        if (!screenshotCaptured) {
            canvas.drawText("screenshotUnavailable = true reason=${reason ?: "unknown"}", 24f, 222f, textPaint)
        }
        return bitmap
    }

    private fun drawBox(canvas: Canvas, bounds: VisualBounds, color: Int, stroke: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            style = Paint.Style.STROKE
            strokeWidth = stroke
        }
        canvas.drawRect(bounds.left.toFloat(), bounds.top.toFloat(), bounds.right.toFloat(), bounds.bottom.toFloat(), paint)
    }

    private fun drawLabel(canvas: Canvas, bounds: VisualBounds, label: String, color: Int, textPaint: Paint) {
        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            style = Paint.Style.FILL
        }
        val top = (bounds.top - 36).coerceAtLeast(0)
        canvas.drawRect(bounds.left.toFloat(), top.toFloat(), (bounds.left + 170).toFloat(), (top + 34).toFloat(), bg)
        canvas.drawText(label, (bounds.left + 6).toFloat(), (top + 25).toFloat(), textPaint)
    }
}
