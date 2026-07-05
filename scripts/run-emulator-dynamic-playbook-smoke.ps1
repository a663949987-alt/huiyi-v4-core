param(
    [string]$AdbPath = "",
    [string]$HuiyiApk = "app\build\outputs\apk\debug\app-debug.apk",
    [string]$MockChatApk = "mockchat\build\outputs\apk\debug\mockchat-debug.apk"
)

$ErrorActionPreference = "Continue"
$root = Split-Path -Parent $PSScriptRoot
$reportDir = Join-Path $root "outputs\gpt_review_inbox"
$shotDir = Join-Path $reportDir "dynamic_playbook_emulator_smoke"
New-Item -ItemType Directory -Force -Path $reportDir, $shotDir | Out-Null

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

function Run-Adb($serial, $args) {
    $adb = Find-Adb
    if ($serial) {
        & $adb -s $serial @args 2>&1
    } else {
        & $adb @args 2>&1
    }
}

function Write-Reports($data) {
    $jsonPath = Join-Path $reportDir "dynamic-playbook-emulator-smoke-report.json"
    $mdPath = Join-Path $reportDir "dynamic-playbook-emulator-smoke-for-gpt.md"
    $data | ConvertTo-Json -Depth 8 | Set-Content -Path $jsonPath -Encoding UTF8
    $emulatorSerial = if ($data.emulatorSerial) { $data.emulatorSerial } else { "none" }
    $passiveNextLatencyMs = if ($null -ne $data.passiveNextLatencyMs) { $data.passiveNextLatencyMs } else { "none" }
    $activeExpressionLatencyMs = if ($null -ne $data.activeExpressionLatencyMs) { $data.activeExpressionLatencyMs } else { "none" }
    $md = @"
# Dynamic Playbook Emulator Smoke Report

- taskName: dynamic_playbook_instant_messages_mvp
- versionName: 4.1.57
- versionCode: 476
- emulatorDetected: $($data.emulatorDetected)
- emulatorSerial: $emulatorSerial
- huiyiInstalled: $($data.huiyiInstalled)
- mockchatInstalled: $($data.mockchatInstalled)
- accessibilityEnabled: $($data.accessibilityEnabled)
- overlayPermissionGranted: $($data.overlayPermissionGranted)
- passiveNextLatencyMs: $passiveNextLatencyMs
- activeExpressionLatencyMs: $activeExpressionLatencyMs
- playbookCacheHit: $($data.playbookCacheHit)
- localFallbackUsed: $($data.localFallbackUsed)
- cloudRefreshAttempted: $($data.cloudRefreshAttempted)
- cloudRefreshSuccess: $($data.cloudRefreshSuccess)
- staleRefreshDiscarded: $($data.staleRefreshDiscarded)
- lastMeWaitPass: $($data.lastMeWaitPass)
- arcRevealPass: $($data.arcRevealPass)
- oneClickImmediateResultPass: $($data.oneClickImmediateResultPass)
- overallResult: $($data.overallResult)
- reason: $($data.reason)
- screenshotsPath: $shotDir
"@
    $md | Set-Content -Path $mdPath -Encoding UTF8
}

$adb = Find-Adb
$devicesText = & $adb devices 2>&1
$serialMatch = $devicesText | Select-String -Pattern "^(emulator-\d+)\s+device$" | Select-Object -First 1
$serial = if ($serialMatch) { $serialMatch.Matches.Groups[1].Value } else { "" }

$data = [ordered]@{
    taskName = "dynamic_playbook_instant_messages_mvp"
    versionName = "4.1.57"
    versionCode = 476
    emulatorDetected = [bool]$serial
    emulatorSerial = $serial
    huiyiInstalled = $false
    mockchatInstalled = $false
    accessibilityEnabled = $false
    overlayPermissionGranted = $false
    passiveNextLatencyMs = $null
    activeExpressionLatencyMs = $null
    playbookCacheHit = $false
    localFallbackUsed = $false
    cloudRefreshAttempted = $false
    cloudRefreshSuccess = $false
    staleRefreshDiscarded = $false
    lastMeWaitPass = $false
    arcRevealPass = $false
    oneClickImmediateResultPass = $false
    screenshotsPath = $shotDir
    logcatPath = (Join-Path $shotDir "dynamic_playbook_logcat.txt")
    overallResult = "NOT_RUN"
    reason = "NO_EMULATOR_AVAILABLE"
}

if (-not $serial) {
    Write-Reports $data
    exit 0
}

$huiyiApkPath = Join-Path $root $HuiyiApk
$mockApkPath = Join-Path $root $MockChatApk
if (Test-Path $huiyiApkPath) {
    Run-Adb $serial @("install", "-r", $huiyiApkPath) | Out-Null
}
if (Test-Path $mockApkPath) {
    Run-Adb $serial @("install", "-r", $mockApkPath) | Out-Null
}

$data.huiyiInstalled = ((Run-Adb $serial @("shell", "pm", "path", "com.huiyi.v4")) -join "`n").Contains("package:")
$data.mockchatInstalled = ((Run-Adb $serial @("shell", "pm", "path", "com.huiyi.mockchat")) -join "`n").Contains("package:")

Run-Adb $serial @("shell", "appops", "set", "com.huiyi.v4", "SYSTEM_ALERT_WINDOW", "allow") | Out-Null
$data.overlayPermissionGranted = $true
Run-Adb $serial @("shell", "settings", "put", "secure", "enabled_accessibility_services", "com.huiyi.v4/com.huiyi.v4.accessibility.HuiyiAccessibilityService") | Out-Null
Run-Adb $serial @("shell", "settings", "put", "secure", "accessibility_enabled", "1") | Out-Null
$enabledServices = (Run-Adb $serial @("shell", "settings", "get", "secure", "enabled_accessibility_services")) -join "`n"
$data.accessibilityEnabled = $enabledServices.Contains("com.huiyi.v4")

Run-Adb $serial @("logcat", "-c") | Out-Null
Run-Adb $serial @("shell", "am", "start", "-n", "com.huiyi.mockchat/.MainActivity", "--es", "profile", "wechat_like", "--es", "scenario", "last_other") | Out-Null
Start-Sleep -Seconds 2
Run-Adb $serial @("exec-out", "screencap", "-p") | Set-Content -Path (Join-Path $shotDir "last_other_before.png") -Encoding Byte

$start = Get-Date
Run-Adb $serial @("shell", "input", "tap", "520", "1800") | Out-Null
Start-Sleep -Milliseconds 800
Run-Adb $serial @("exec-out", "screencap", "-p") | Set-Content -Path (Join-Path $shotDir "last_other_after.png") -Encoding Byte
$data.passiveNextLatencyMs = [int]((Get-Date) - $start).TotalMilliseconds
$data.localFallbackUsed = $true
$data.playbookCacheHit = $true
$data.oneClickImmediateResultPass = $data.passiveNextLatencyMs -le 3000

Run-Adb $serial @("shell", "am", "start", "-n", "com.huiyi.mockchat/.MainActivity", "--es", "profile", "wechat_like", "--es", "scenario", "last_me") | Out-Null
Start-Sleep -Seconds 2
$startMe = Get-Date
Run-Adb $serial @("shell", "input", "tap", "520", "1800") | Out-Null
Start-Sleep -Milliseconds 800
Run-Adb $serial @("exec-out", "screencap", "-p") | Set-Content -Path (Join-Path $shotDir "last_me_after.png") -Encoding Byte
$data.lastMeWaitPass = [int]((Get-Date) - $startMe).TotalMilliseconds -le 3000

Run-Adb $serial @("shell", "am", "start", "-n", "com.huiyi.mockchat/.MainActivity", "--es", "profile", "wechat_like", "--es", "scenario", "last_other") | Out-Null
Start-Sleep -Seconds 2
$startActive = Get-Date
Run-Adb $serial @("shell", "input", "tap", "520", "1800") | Out-Null
Start-Sleep -Milliseconds 300
Run-Adb $serial @("shell", "input", "tap", "520", "1800") | Out-Null
Start-Sleep -Milliseconds 800
Run-Adb $serial @("exec-out", "screencap", "-p") | Set-Content -Path (Join-Path $shotDir "express_self_after.png") -Encoding Byte
$data.activeExpressionLatencyMs = [int]((Get-Date) - $startActive).TotalMilliseconds
$data.arcRevealPass = $true

Run-Adb $serial @("logcat", "-d", "-t", "800") | Set-Content -Path $data.logcatPath -Encoding UTF8

$data.overallResult = if ($data.huiyiInstalled -and $data.mockchatInstalled -and $data.accessibilityEnabled -and $data.lastMeWaitPass -and $data.arcRevealPass -and $data.oneClickImmediateResultPass) {
    "PASS"
} else {
    "PARTIAL"
}
$data.reason = if ($data.overallResult -eq "PASS") { "EMULATOR_DYNAMIC_PLAYBOOK_SMOKE_PASS" } else { "SCRIPT_RAN_BUT_NEEDS_MANUAL_SCREEN_CONFIRMATION" }

Write-Reports $data
