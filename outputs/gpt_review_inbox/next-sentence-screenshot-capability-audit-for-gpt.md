# Next Sentence Screenshot Capability Audit

## Basic
- versionName: 4.1.9
- versionCode: 423
- taskName: next_sentence_screenshot_capability_failure_fix
- currentOverallResult: NOT_TESTED
- real_device_screenshot_failure_smoke: NOT_TESTED

## Root cause from user diagnostics
- pipelineException: `java.lang.SecurityException: Services don't have the capability of taking the screenshot.`
- root cause: optional visual debug screenshot escaped into the next-sentence success path and aborted the pipeline.
- not root cause: accessibility permission, floating bubble lifecycle, service connection.

## Code audit
- `takeScreenshot` only appears in `VisualDebugCapture`.
- `CurrentScreenCaptureUseCase` uses Accessibility Node Tree and lastStableChatSnapshot; it does not call screenshot APIs.
- `HuiyiRuntime.runNextSentence()` now treats visual debug screenshot as best-effort optional diagnostics after node tree pipeline returns.

## Fixes
- Added screenshot error codes: `SCREENSHOT_CAPABILITY_MISSING`, `SCREENSHOT_NOT_ALLOWED`, `SCREENSHOT_SECURE_WINDOW`, `SCREENSHOT_RATE_LIMITED`, `SCREENSHOT_FAILED`.
- `SecurityException("Services don't have the capability of taking the screenshot.")` maps to `SCREENSHOT_CAPABILITY_MISSING`.
- `VisualDebugCapture.captureScreenshot()` catches synchronous `takeScreenshot` exceptions and callback failures.
- Screenshot failure is written as `secondaryErrorCode` when node tree analysis succeeds.
- Failure report now separates `primaryCapturePath`, `nodeTreeAttempted`, `nodeTreeSuccess`, `screenshotAttempted`, `screenshotSuccess`, screenshot exception fields, and pipeline exception fields.
- Accessibility service metadata now declares `android:canTakeScreenshot="true"` while keeping node tree as main path.

## Expected behavior
- Node tree success + screenshot capability missing => route/wait panel still shows.
- If analysis still fails, latest failure report must include a specific non-UNKNOWN error code and pipeline exception fields.

## Local validation
- testDebugUnitTest: PASS
- assembleDebug: PASS
- assembleRelease: PASS
- real-device screenshot failure smoke: NOT_TESTED, no physical Android device available in this Codex environment.
