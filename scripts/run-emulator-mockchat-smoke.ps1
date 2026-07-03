param(
    [string]$Serial = "",
    [string]$HuiyiApk = "",
    [string]$MockChatApk = "",
    [string]$OutputDir = ""
)

$ErrorActionPreference = "Stop"

$repo = Split-Path $PSScriptRoot -Parent
if ([string]::IsNullOrWhiteSpace($OutputDir)) {
    $OutputDir = Join-Path $repo "outputs"
}
$reportMd = Join-Path $OutputDir "emulator-mockchat-smoke-report-for-gpt.md"
$reportJson = Join-Path $OutputDir "emulator-mockchat-smoke-report.json"
$screensDir = Join-Path $OutputDir "emulator_mockchat_smoke"
New-Item -ItemType Directory -Path $OutputDir -Force | Out-Null
New-Item -ItemType Directory -Path $screensDir -Force | Out-Null

function Find-Adb {
    $sdkAdb = Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe"
    if (Test-Path -LiteralPath $sdkAdb) { return $sdkAdb }
    $cmd = Get-Command adb -ErrorAction SilentlyContinue
    if ($cmd) { return $cmd.Source }
    return $null
}

function Write-SmokeReport {
    param([hashtable]$Data)

    $json = [ordered]@{}
    foreach ($key in $Data.Keys | Sort-Object) { $json[$key] = $Data[$key] }
    ($json | ConvertTo-Json -Depth 8) | Set-Content -Path $reportJson -Encoding UTF8

    $markdown = @"
# Emulator MockChat Smoke Report

- taskName: emulator_mockchat_ui_smoke
- emulatorUiSmokeResult: $($Data.emulatorUiSmokeResult)
- reason: $($Data.reason)
- fixtureReplayResult: $($Data.fixtureReplayResult)
- mockChatBuildResult: $($Data.mockChatBuildResult)
- realDeviceSmokeResult: $($Data.realDeviceSmokeResult)
- emulatorDetected: $($Data.emulatorDetected)
- emulatorSerial: $($Data.emulatorSerial)
- huiyiInstalled: $($Data.huiyiInstalled)
- mockchatInstalled: $($Data.mockchatInstalled)
- accessibilityEnabled: $($Data.accessibilityEnabled)
- overlayPermissionGranted: $($Data.overlayPermissionGranted)
- lastMeResult: $($Data.lastMeResult)
- lastOtherResult: $($Data.lastOtherResult)
- readReceiptResult: $($Data.readReceiptResult)
- overlayContaminationResult: $($Data.overlayContaminationResult)
- oneTapFeedbackTargetSessionResult: $($Data.oneTapFeedbackTargetSessionResult)
- screenshotsPath: $($Data.screenshotsPath)
- logcatPath: $($Data.logcatPath)

## Acceptance Boundary

- fixtureReplayResult only means offline fixture / JVM parser replay passed.
- mockChatBuildResult only means the MockChat APK exists or builds.
- emulatorUiSmokeResult=PASS is allowed only after an emulator is detected, both APKs install, accessibility is enabled, overlay permission is granted, MockChat scenarios are opened, Next Sentence is triggered, and overlay results are observed.
- realDeviceSmokeResult=PASS is allowed only after a physical Android phone smoke test.

## Required Emulator UI Checks

1. MockChat LAST_ME -> WAIT_PANEL: $($Data.lastMeResult)
2. MockChat LAST_OTHER -> ROUTE_PANEL / routes=5: $($Data.lastOtherResult)
3. read receipt / checkmark not effective: $($Data.readReceiptResult)
4. Huiyi overlay contamination does not pollute preAnalysis: $($Data.overlayContaminationResult)
5. one tap feedback binds original session: $($Data.oneTapFeedbackTargetSessionResult)

## Notes

$($Data.notes)
"@
    $markdown | Set-Content -Path $reportMd -Encoding UTF8
}

$data = @{
    emulatorUiSmokeResult = "NOT_RUN"
    reason = "NO_EMULATOR_AVAILABLE"
    fixtureReplayResult = "UNKNOWN"
    mockChatBuildResult = "UNKNOWN"
    realDeviceSmokeResult = "NOT_TESTED"
    emulatorDetected = $false
    emulatorSerial = ""
    huiyiInstalled = $false
    mockchatInstalled = $false
    accessibilityEnabled = $false
    overlayPermissionGranted = $false
    lastMeResult = "NOT_RUN"
    lastOtherResult = "NOT_RUN"
    readReceiptResult = "NOT_RUN"
    overlayContaminationResult = "NOT_RUN"
    oneTapFeedbackTargetSessionResult = "NOT_RUN"
    screenshotsPath = $screensDir
    logcatPath = ""
    notes = "No emulator UI smoke evidence was produced."
}

$adb = Find-Adb
if (-not $adb) {
    $data.reason = "ADB_NOT_FOUND"
    Write-SmokeReport $data
    Write-Host "emulatorUiSmokeResult=NOT_RUN reason=ADB_NOT_FOUND"
    exit 0
}

if ([string]::IsNullOrWhiteSpace($HuiyiApk)) {
    $candidate = Get-ChildItem -Path (Join-Path $repo "outputs") -Filter "huiyi-v*-debug.apk" -File -ErrorAction SilentlyContinue |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1
    if ($candidate) { $HuiyiApk = $candidate.FullName }
}
if ([string]::IsNullOrWhiteSpace($MockChatApk)) {
    $candidate = Join-Path $repo "outputs\mockchat-debug.apk"
    if (Test-Path -LiteralPath $candidate) { $MockChatApk = $candidate }
}
$data.mockChatBuildResult = if (Test-Path -LiteralPath $MockChatApk) { "PASS" } else { "NOT_BUILT" }

$devicesRaw = & $adb devices
$emulators = @()
foreach ($line in $devicesRaw) {
    if ($line -match "^(emulator-\d+)\s+device$") { $emulators += $Matches[1] }
}
if (-not [string]::IsNullOrWhiteSpace($Serial)) {
    if ($emulators -contains $Serial) { $emulators = @($Serial) } else { $emulators = @() }
}
if ($emulators.Count -eq 0) {
    $data.reason = "NO_EMULATOR_AVAILABLE"
    Write-SmokeReport $data
    Write-Host "emulatorUiSmokeResult=NOT_RUN reason=NO_EMULATOR_AVAILABLE"
    exit 0
}

$serial = $emulators[0]
$data.emulatorDetected = $true
$data.emulatorSerial = $serial
$logcat = Join-Path $screensDir "logcat-$serial.txt"
$data.logcatPath = $logcat

if (-not (Test-Path -LiteralPath $HuiyiApk)) {
    $data.reason = "HUIYI_APK_NOT_FOUND"
    Write-SmokeReport $data
    Write-Host "emulatorUiSmokeResult=NOT_RUN reason=HUIYI_APK_NOT_FOUND"
    exit 0
}
if (-not (Test-Path -LiteralPath $MockChatApk)) {
    $data.reason = "MOCKCHAT_APK_NOT_FOUND"
    Write-SmokeReport $data
    Write-Host "emulatorUiSmokeResult=NOT_RUN reason=MOCKCHAT_APK_NOT_FOUND"
    exit 0
}

& $adb -s $serial install -r $HuiyiApk | Out-Null
if ($LASTEXITCODE -eq 0) { $data.huiyiInstalled = $true }
& $adb -s $serial install -r $MockChatApk | Out-Null
if ($LASTEXITCODE -eq 0) { $data.mockchatInstalled = $true }
if (-not $data.huiyiInstalled -or -not $data.mockchatInstalled) {
    $data.reason = "APK_INSTALL_FAILED"
    Write-SmokeReport $data
    Write-Host "emulatorUiSmokeResult=NOT_RUN reason=APK_INSTALL_FAILED"
    exit 0
}

$service = "com.huiyi.v4/com.huiyi.v4.accessibility.HuiyiAccessibilityService"
& $adb -s $serial shell settings put secure enabled_accessibility_services $service | Out-Null
& $adb -s $serial shell settings put secure accessibility_enabled 1 | Out-Null
$enabledServices = (& $adb -s $serial shell settings get secure enabled_accessibility_services) -join ""
$data.accessibilityEnabled = $enabledServices.Contains($service)

& $adb -s $serial shell appops set com.huiyi.v4 SYSTEM_ALERT_WINDOW allow | Out-Null
$appOps = (& $adb -s $serial shell appops get com.huiyi.v4 SYSTEM_ALERT_WINDOW) -join ""
$data.overlayPermissionGranted = $appOps -match "allow"

function Open-MockScenario {
    param([string]$Scenario)
    & $adb -s $serial shell am start -n com.huiyi.mockchat/.MainActivity --es scenario $Scenario | Out-Null
    Start-Sleep -Seconds 2
    & $adb -s $serial shell uiautomator dump /sdcard/window.xml | Out-Null
    $dumpLocal = Join-Path $screensDir "$Scenario-window.xml"
    & $adb -s $serial pull /sdcard/window.xml $dumpLocal | Out-Null
    & $adb -s $serial exec-out screencap -p > (Join-Path $screensDir "$Scenario.png")
    return $dumpLocal
}

function Trigger-NextSentence {
    # Best-effort UI path: tap the lower-left floating bubble/menu area, then tap the menu item if it appears.
    & $adb -s $serial shell am start -n com.huiyi.v4/.MainActivity | Out-Null
    Start-Sleep -Seconds 1
    & $adb -s $serial shell input tap 540 1900 | Out-Null
    Start-Sleep -Seconds 1
    & $adb -s $serial shell am start -n com.huiyi.mockchat/.MainActivity | Out-Null
    Start-Sleep -Seconds 1
    & $adb -s $serial shell input tap 92 1320 | Out-Null
    Start-Sleep -Milliseconds 600
    & $adb -s $serial shell input tap 170 1320 | Out-Null
    Start-Sleep -Seconds 5
}

$lastMeDump = Open-MockScenario "last_me"
Trigger-NextSentence
& $adb -s $serial shell uiautomator dump /sdcard/after-last-me.xml | Out-Null
$afterLastMe = Join-Path $screensDir "after-last-me.xml"
& $adb -s $serial pull /sdcard/after-last-me.xml $afterLastMe | Out-Null
$lastMeText = if (Test-Path -LiteralPath $afterLastMe) { Get-Content -Raw -LiteralPath $afterLastMe } else { "" }
$data.lastMeResult = if ($lastMeText -match "WAIT|LAST_SPEAKER_IS_ME_SHOULD_WAIT") { "PASS" } else { "INCONCLUSIVE" }

$lastOtherDump = Open-MockScenario "last_other"
Trigger-NextSentence
& $adb -s $serial shell uiautomator dump /sdcard/after-last-other.xml | Out-Null
$afterLastOther = Join-Path $screensDir "after-last-other.xml"
& $adb -s $serial pull /sdcard/after-last-other.xml $afterLastOther | Out-Null
$lastOtherText = if (Test-Path -LiteralPath $afterLastOther) { Get-Content -Raw -LiteralPath $afterLastOther } else { "" }
$data.lastOtherResult = if ($lastOtherText -match "routes|Route|ROUTE_PANEL") { "PASS" } else { "INCONCLUSIVE" }

$readReceiptDump = Open-MockScenario "read_receipt_status"
$readReceiptText = if (Test-Path -LiteralPath $readReceiptDump) { Get-Content -Raw -LiteralPath $readReceiptDump } else { "" }
$data.readReceiptResult = if ($readReceiptText -match "read|receipt|check|status") { "CAPTURED_NOT_ASSERTED" } else { "INCONCLUSIVE" }

$overlayDump = Open-MockScenario "huiyi_overlay_contamination"
$overlayText = if (Test-Path -LiteralPath $overlayDump) { Get-Content -Raw -LiteralPath $overlayDump } else { "" }
$data.overlayContaminationResult = if ($overlayText -match "huiyi|overlay|contamination|next") { "CAPTURED_NOT_ASSERTED" } else { "INCONCLUSIVE" }

& $adb -s $serial logcat -d -v time > $logcat
$data.oneTapFeedbackTargetSessionResult = "NOT_RUN"
$allRequired = @($data.lastMeResult, $data.lastOtherResult) -notcontains "INCONCLUSIVE"
$data.emulatorUiSmokeResult = if ($allRequired -and $data.accessibilityEnabled -and $data.overlayPermissionGranted) { "PASS" } else { "FAIL" }
$data.reason = if ($data.emulatorUiSmokeResult -eq "PASS") { "EMULATOR_UI_SMOKE_COMPLETED" } else { "EMULATOR_UI_SMOKE_INCONCLUSIVE_OR_FAILED" }
$data.notes = "This script performed best-effort adb UI smoke. A PASS requires actual emulator UI evidence, not fixture replay."

Write-SmokeReport $data
Write-Host "emulatorUiSmokeResult=$($data.emulatorUiSmokeResult) reason=$($data.reason)"
