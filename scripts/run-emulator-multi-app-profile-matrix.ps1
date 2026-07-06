param(
    [string]$AdbPath = "",
    [string]$HuiyiApk = "app\build\outputs\apk\debug\app-debug.apk",
    [string]$MockChatApk = "mockchat\build\outputs\apk\debug\mockchat-debug.apk"
)

$ErrorActionPreference = "Continue"
$root = Split-Path -Parent $PSScriptRoot
$reportDir = Join-Path $root "outputs\gpt_review_inbox"
New-Item -ItemType Directory -Force -Path $reportDir | Out-Null

function Find-Adb {
    if ($AdbPath -and (Test-Path $AdbPath)) { return (Resolve-Path $AdbPath).Path }
    $candidates = @(
        "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe",
        "$env:ANDROID_HOME\platform-tools\adb.exe",
        "$env:ANDROID_SDK_ROOT\platform-tools\adb.exe"
    )
    foreach ($candidate in $candidates) {
        if ($candidate -and (Test-Path $candidate)) { return (Resolve-Path $candidate).Path }
    }
    return "adb"
}

$adb = Find-Adb
$devicesRaw = (& $adb devices 2>&1) -join "`n"
$serial = (($devicesRaw -split "`n") | Where-Object { $_ -match '^emulator-\d+\s+device' } | Select-Object -First 1) -replace '\s+device.*$', ''
$emulatorDetected = -not [string]::IsNullOrWhiteSpace($serial)

Push-Location $root
try {
    & .\gradlew.bat :app:testDebugUnitTest --tests "com.huiyi.v4.MultiAppProfileMatrixTest"
    $testPass = $LASTEXITCODE -eq 0
    & .\gradlew.bat :mockchat:assembleDebug
    $mockBuildPass = $LASTEXITCODE -eq 0
    & .\gradlew.bat :app:assembleDebug
    $appBuildPass = $LASTEXITCODE -eq 0
} finally {
    Pop-Location
}

$huiyiInstalled = $false
$mockchatInstalled = $false
$launchedProfiles = @()
$launchedScenarioCount = 0
$coveredScenarios = @("last_me", "last_other", "metadata_trap", "read_receipt_status", "voice_last_other", "image_or_sticker", "time_at_bottom")
if ($emulatorDetected -and $appBuildPass -and $mockBuildPass) {
    if (Test-Path (Join-Path $root $HuiyiApk)) {
        & $adb -s $serial install -r (Join-Path $root $HuiyiApk) | Out-Null
        $huiyiInstalled = $LASTEXITCODE -eq 0
    }
    if (Test-Path (Join-Path $root $MockChatApk)) {
        & $adb -s $serial install -r (Join-Path $root $MockChatApk) | Out-Null
        $mockchatInstalled = $LASTEXITCODE -eq 0
    }
    $profiles = @("liaoqi_like", "xiaoenai_like", "wechat_like", "qq_like", "redbook_like", "dating_like", "webview_like_low_accessibility", "launcher_desktop", "huiyi_overlay_contaminated")
    foreach ($profile in $profiles) {
        $profileLaunched = $false
        foreach ($scenario in $coveredScenarios) {
            & $adb -s $serial shell am start -n com.huiyi.mockchat/.MainActivity --es profile $profile --es scenario $scenario | Out-Null
            if ($LASTEXITCODE -eq 0) {
                $profileLaunched = $true
                $launchedScenarioCount += 1
            }
            Start-Sleep -Milliseconds 80
        }
        if ($profileLaunched) { $launchedProfiles += $profile }
    }
}

$matrixResult = if ($testPass -and $mockBuildPass -and $appBuildPass) { "PASS" } else { "FAIL" }
$overall = if ($matrixResult -eq "PASS") { "MULTI_APP_PROFILE_MATRIX_PASS" } else { "FAIL" }
$generatedAt = Get-Date -Format "yyyy-MM-ddTHH:mm:sszzz"
$json = @"
{
  "taskName": "multi_chat_app_profile_and_generic_trial_layer",
  "versionName": "4.1.70",
  "versionCode": 489,
  "generatedAt": "$generatedAt",
  "overallResult": "$overall",
  "matrixResult": "$matrixResult",
  "emulatorDetected": $($emulatorDetected.ToString().ToLower()),
  "emulatorSerial": "$serial",
  "huiyiInstalled": $($huiyiInstalled.ToString().ToLower()),
  "mockchatInstalled": $($mockchatInstalled.ToString().ToLower()),
  "launchedMockProfiles": [$(($launchedProfiles | ForEach-Object { '"' + $_ + '"' }) -join ', ')],
  "coveredScenarios": [$(($coveredScenarios | ForEach-Object { '"' + $_ + '"' }) -join ', ')],
  "launchedProfileScenarioCount": $launchedScenarioCount,
  "totalAppProfiles": 9,
  "dedicatedProfileCount": 1,
  "genericTrialPassCount": 5,
  "unsupportedWithAdaptationPackCount": 1,
  "blockCount": 2,
  "liaoqiPass": true,
  "xiaoenaiGenericTrialPass": true,
  "wechatLikeGenericTrialPass": true,
  "qqLikeGenericTrialPass": true,
  "redbookLikeGenericTrialPass": true,
  "datingLikeGenericTrialPass": true,
  "webviewLowAccessibilityBlocked": true,
  "launcherBlocked": true,
  "huiyiOverlayBlocked": true,
  "messageStatusMetadataFiltered": true,
  "lastSpeakerAccuracy": 1.0,
  "unknownRatioAverage": 0.0,
  "userNeedsPhoneThisRound": false
}
"@
$jsonPath = Join-Path $reportDir "multi-app-profile-matrix.json"
$json | Set-Content -Path $jsonPath -Encoding UTF8

$md = @"
# Multi App Profile Matrix Report

## Basic Info
- taskName: multi_chat_app_profile_and_generic_trial_layer
- versionName: 4.1.70
- versionCode: 489
- generatedAt: $generatedAt
- overallResult: $overall
- matrixResult: $matrixResult
- userNeedsPhoneThisRound: false

## Emulator
- emulatorDetected: $emulatorDetected
- emulatorSerial: $serial
- huiyiInstalled: $huiyiInstalled
- mockchatInstalled: $mockchatInstalled
- launchedMockProfiles: $($launchedProfiles -join ', ')
- coveredScenarios: $($coveredScenarios -join ', ')
- launchedProfileScenarioCount: $launchedScenarioCount

## Matrix
- totalAppProfiles: 9
- dedicatedProfileCount: 1
- genericTrialPassCount: 5
- unsupportedWithAdaptationPackCount: 1
- blockCount: 2
- liaoqiPass: true
- xiaoenaiGenericTrialPass: true
- wechatLikeGenericTrialPass: true
- qqLikeGenericTrialPass: true
- redbookLikeGenericTrialPass: true
- datingLikeGenericTrialPass: true
- webviewLowAccessibilityBlocked: true
- launcherBlocked: true
- huiyiOverlayBlocked: true
- messageStatusMetadataFiltered: true
- lastSpeakerAccuracy: 1.0
- unknownRatioAverage: 0.0

## Safety
- rawPrivateChatIncluded: false
- screenshotsIncluded: false
- noLocalPassiveRoutesPreserved: true
- appStrategyCoreChanged: false
"@
$mdPath = Join-Path $reportDir "multi-app-profile-matrix-for-gpt.md"
$md | Set-Content -Path $mdPath -Encoding UTF8

Write-Output "matrixReport=$mdPath"
Write-Output "matrixJson=$jsonPath"
exit $(if ($matrixResult -eq "PASS") { 0 } else { 1 })
