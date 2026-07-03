param(
    [string]$ApkPath = "outputs/huiyi-v4.1.9-debug.apk",
    [string]$DeviceReviewDir = "/sdcard/Download/Huiyi/review",
    [string]$PackageName = "com.huiyi.v4",
    [string]$TargetChatPackage = "com.bajiao.im.liaoqi"
)

$ErrorActionPreference = "Stop"

function Invoke-Adb {
    param([Parameter(ValueFromRemainingArguments = $true)][string[]]$Args)
    & adb @Args
    if ($LASTEXITCODE -ne 0) {
        throw "adb failed: $($Args -join ' ')"
    }
}

Write-Host "1. Installing latest Huiyi APK: $ApkPath"
if (!(Test-Path $ApkPath)) {
    throw "APK not found: $ApkPath"
}
Invoke-Adb install -r $ApkPath

Write-Host "2. Confirm accessibility is enabled for Huiyi. Enable it manually if missing."
Invoke-Adb shell settings get secure enabled_accessibility_services

Write-Host "3. Checking overlay permission state for $PackageName."
Invoke-Adb shell appops get $PackageName SYSTEM_ALERT_WINDOW

Write-Host "4. Open target chat page in $TargetChatPackage. Waiting 2 seconds for lastStableChatSnapshot."
Start-Sleep -Seconds 2

Write-Host "5. Tap Huiyi floating bubble '下一句'. Wait for route/wait panel or controlled fail, then press Enter."
Read-Host

Write-Host "6. Pulling screenshot-failure smoke diagnostics from phone Downloads."
New-Item -ItemType Directory -Force -Path "outputs\real_device_pulled" | Out-Null
Invoke-Adb pull "$DeviceReviewDir/latest-next-sentence-failure.md" "outputs\real_device_pulled\latest-next-sentence-failure.md"
Invoke-Adb pull "$DeviceReviewDir/latest-next-sentence-failure.json" "outputs\real_device_pulled\latest-next-sentence-failure.json"
Invoke-Adb pull "$DeviceReviewDir/accessibility-click-diagnostic-report-for-gpt.md" "outputs\real_device_pulled\accessibility-click-diagnostic-report-for-gpt.md"
Invoke-Adb pull "$DeviceReviewDir/real-device-overlay-accessibility-report.json" "outputs\real_device_pulled\real-device-overlay-accessibility-report.json"
Invoke-Adb pull "$DeviceReviewDir/huiyi-v4-review-for-gpt.md" "outputs\real_device_pulled\huiyi-v4-review-for-gpt.md"

Write-Host "Done. Pulled files are in outputs\real_device_pulled."
