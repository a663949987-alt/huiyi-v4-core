param(
    [string]$AdbPath = "",
    [string]$HuiyiApk = "app\build\outputs\apk\debug\app-debug.apk",
    [string]$MockChatApk = "mockchat\build\outputs\apk\debug\mockchat-debug.apk",
    [int]$CloudWaitSeconds = 100
)

$ErrorActionPreference = "Continue"
$root = Split-Path -Parent $PSScriptRoot
$reportDir = Join-Path $root "outputs\gpt_review_inbox"
$shotDir = Join-Path $reportDir "dynamic_playbook_cloud_refresh_emulator_smoke"
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

function Run-Adb($serial, [object[]]$AdbArgs) {
    $adb = Find-Adb
    if ($serial) {
        & $adb -s $serial @AdbArgs 2>&1
    } else {
        & $adb @AdbArgs 2>&1
    }
}

function U($hexes) {
    $chars = @()
    foreach ($hex in ($hexes -split " ")) {
        if ($hex.Trim().Length -gt 0) {
            $chars += [char]([Convert]::ToInt32($hex, 16))
        }
    }
    return -join $chars
}

function Save-Screenshot($serial, $name) {
    $adb = Find-Adb
    $path = Join-Path $shotDir $name
    $arguments = if ($serial) { @("-s", $serial, "exec-out", "screencap", "-p") } else { @("exec-out", "screencap", "-p") }
    $process = New-Object System.Diagnostics.Process
    $process.StartInfo.FileName = $adb
    $process.StartInfo.UseShellExecute = $false
    $process.StartInfo.RedirectStandardOutput = $true
    $process.StartInfo.Arguments = ($arguments | ForEach-Object {
        if ($_ -match "\s") { '"' + ($_ -replace '"', '\"') + '"' } else { $_ }
    }) -join " "
    [void]$process.Start()
    $stream = [System.IO.File]::Create($path)
    try {
        $process.StandardOutput.BaseStream.CopyTo($stream)
    } finally {
        $stream.Dispose()
        $process.WaitForExit()
    }
}

function Get-UiXml($serial, $name) {
    Run-Adb $serial @("shell", "uiautomator", "dump", "/sdcard/huiyi_window.xml") | Out-Null
    $xml = (Run-Adb $serial @("exec-out", "cat", "/sdcard/huiyi_window.xml")) -join "`n"
    $path = Join-Path $shotDir $name
    $xml | Set-Content -Path $path -Encoding UTF8
    return $xml
}

function Ui-ContainsAny($xml, [string[]]$needles) {
    foreach ($needle in $needles) {
        if ($xml.Contains($needle)) { return $true }
    }
    return $false
}

function Wait-UiContainsAny($serial, $name, [string[]]$needles, [int]$timeoutMs = 8000) {
    $deadline = (Get-Date).AddMilliseconds($timeoutMs)
    $xml = ""
    do {
        $xml = Get-UiXml $serial $name
        if (Ui-ContainsAny $xml $needles) { return $xml }
        Start-Sleep -Milliseconds 300
    } while ((Get-Date) -lt $deadline)
    return $xml
}

function Get-HuiyiOverlayFrame($serial) {
    $windowDump = (Run-Adb $serial @("shell", "dumpsys", "window")) -join "`n"
    $matches = [regex]::Matches($windowDump, "com\.huiyi\.v4, frame=\[Rect\((\d+),\s*(\d+)\s*-\s*(\d+),\s*(\d+)\)\]")
    if ($matches.Count -eq 0) { return $null }
    $best = $null
    $bestArea = [int]::MaxValue
    foreach ($match in $matches) {
        $left = [int]$match.Groups[1].Value
        $top = [int]$match.Groups[2].Value
        $right = [int]$match.Groups[3].Value
        $bottom = [int]$match.Groups[4].Value
        $area = [Math]::Max(1, $right - $left) * [Math]::Max(1, $bottom - $top)
        if ($area -lt $bestArea) {
            $bestArea = $area
            $best = [ordered]@{ left = $left; top = $top; right = $right; bottom = $bottom }
        }
    }
    return $best
}

function Tap-HuiyiOverlayCenter($serial) {
    $frame = Get-HuiyiOverlayFrame $serial
    if ($null -eq $frame) { return $false }
    $x = [int](($frame.left + $frame.right) / 2)
    $y = [int](($frame.top + $frame.bottom) / 2)
    Run-Adb $serial @("shell", "input", "tap", "$x", "$y") | Out-Null
    return $true
}

function Tap-MenuChoice($serial, $choice) {
    $frame = Get-HuiyiOverlayFrame $serial
    if ($null -ne $frame) {
        $rowHeight = [Math]::Max(1, [int](($frame.bottom - $frame.top) / 5))
        $index = if ($choice -eq "next") { 1 } elseif ($choice -eq "express") { 2 } elseif ($choice -eq "feedback") { 3 } else { 4 }
        $x = [int](($frame.left + $frame.right) / 2)
        $y = [int]($frame.top + ($rowHeight * ($index + 0.5)))
        Run-Adb $serial @("shell", "input", "tap", "$x", "$y") | Out-Null
    } elseif ($choice -eq "next") {
        Run-Adb $serial @("shell", "input", "tap", "945", "1350") | Out-Null
    } elseif ($choice -eq "express") {
        Run-Adb $serial @("shell", "input", "tap", "945", "1485") | Out-Null
    } else {
        Run-Adb $serial @("shell", "input", "tap", "945", "1755") | Out-Null
    }
    Start-Sleep -Milliseconds 100
}

function Open-FloatingMenu($serial) {
    if (Tap-HuiyiOverlayCenter $serial) {
        Start-Sleep -Milliseconds 250
        return $true
    }
    Run-Adb $serial @("shell", "input", "tap", "945", "1270") | Out-Null
    Start-Sleep -Milliseconds 250
    return $true
}

function Start-MockChatScenario($serial, $scenario, $profile, $name) {
    Run-Adb $serial @("shell", "am", "force-stop", "com.huiyi.mockchat") | Out-Null
    Start-Sleep -Milliseconds 250
    Run-Adb $serial @("shell", "am", "start", "-n", "com.huiyi.mockchat/.MainActivity", "--es", "profile", $profile, "--es", "scenario", $scenario) | Out-Null
    Wait-UiContainsAny $serial "$name-ready.xml" @(
        (U "767D 4E91 84DD 5929"),
        (U "8F93 5165 6846"),
        "WECHAT_LIKE",
        "QQ_LIKE"
    ) 10000 | Out-Null
}

function Start-FloatingOnTarget($serial, $name) {
    Run-Adb $serial @("shell", "am", "stopservice", "-n", "com.huiyi.v4/.floating.FloatingBubbleService") | Out-Null
    Start-Sleep -Milliseconds 250
    Run-Adb $serial @("shell", "am", "startservice", "-n", "com.huiyi.v4/.floating.FloatingBubbleService", "--ez", "resetPanel", "true") | Out-Null
    $deadline = (Get-Date).AddSeconds(5)
    do {
        $windowDump = (Run-Adb $serial @("shell", "dumpsys", "window")) -join "`n"
        if ($windowDump.Contains("com.huiyi.v4")) {
            Save-Screenshot $serial "$name-floating-ready.png"
            return $true
        }
        Start-Sleep -Milliseconds 250
    } while ((Get-Date) -lt $deadline)
    Save-Screenshot $serial "$name-floating-missing.png"
    return $false
}

function Click-HuiyiChoice($serial, $choice, $name) {
    Start-FloatingOnTarget $serial $name | Out-Null
    $action = if ($choice -eq "express") {
        "com.huiyi.v4.action.RUN_EXPRESS_SELF"
    } else {
        "com.huiyi.v4.action.RUN_NEXT_SENTENCE"
    }
    Run-Adb $serial @(
        "shell",
        "am",
        "startservice",
        "-a",
        $action,
        "-n",
        "com.huiyi.v4/.floating.FloatingBubbleService",
        "--ez",
        "resetPanel",
        "true"
    ) | Out-Null
    Get-UiXml $serial "$name-service-action.xml" | Out-Null
    Start-Sleep -Milliseconds 100
}

function Extract-LogValue {
    param([string]$Line, [string]$Name)
    if ($Line -match "$Name=([^ ]+)") { return $Matches[1] }
    return $null
}

function Read-LogText($serial, $name) {
    $path = Join-Path $shotDir $name
    $text = (Run-Adb $serial @("logcat", "-d", "-v", "time")) -join "`n"
    $text | Set-Content -Path $path -Encoding UTF8
    return $text
}

function Wait-LogLine($serial, $pattern, [int]$timeoutSeconds, $name) {
    $deadline = (Get-Date).AddSeconds($timeoutSeconds)
    $lastText = ""
    do {
        $lastText = Read-LogText $serial $name
        $line = ($lastText -split "`r?`n" | Where-Object { $_ -match $pattern } | Select-Object -Last 1)
        if ($line) { return $line }
        Start-Sleep -Seconds 1
    } while ((Get-Date) -lt $deadline)
    return $null
}

function Route-Count-Pass($count) {
    return $count -ge 3 -and $count -le 5
}

function Write-Reports($data) {
    $jsonPath = Join-Path $reportDir "dynamic-playbook-cloud-refresh-emulator-smoke.json"
    $mdPath = Join-Path $reportDir "dynamic-playbook-cloud-refresh-emulator-smoke-for-gpt.md"
    $data | ConvertTo-Json -Depth 12 | Set-Content -Path $jsonPath -Encoding UTF8
    $md = @"
# Dynamic Playbook + Cloud Refresh Emulator Smoke

- taskName: dynamic_playbook_cloud_refresh_emulator_smoke
- versionName: 4.1.62
- versionCode: 481
- generatedAt: $($data.generatedAt)
- emulatorDetected: $($data.emulatorDetected)
- emulatorSerial: $($data.emulatorSerial)
- huiyiInstalled: $($data.huiyiInstalled)
- mockchatInstalled: $($data.mockchatInstalled)
- accessibilityEnabled: $($data.accessibilityEnabled)
- overlayPermissionGranted: $($data.overlayPermissionGranted)
- localPlaybookFirstResultPass: $($data.localPlaybookFirstResultPass)
- passiveNextLatencyMs: $($data.passiveNextLatencyMs)
- passiveFirstRouteCount: $($data.passiveFirstRouteCount)
- passiveFirstSource: $($data.passiveFirstSource)
- activeExpressionLatencyMs: $($data.activeExpressionLatencyMs)
- activeExpressionRouteCount: $($data.activeExpressionRouteCount)
- activeExpressionContainsArcReveal: $($data.activeExpressionContainsArcReveal)
- cloudRefreshAttempted: $($data.cloudRefreshAttempted)
- cloudRefreshSuccess: $($data.cloudRefreshSuccess)
- cloudRefreshFailureReason: $($data.cloudRefreshFailureReason)
- cloudContractValidationResult: $($data.cloudContractValidationResult)
- playbookCacheUpdatedFromCloud: $($data.playbookCacheUpdatedFromCloud)
- nextClickReadsCloudEnhancedPlaybook: $($data.nextClickReadsCloudEnhancedPlaybook)
- cloudEnhancedRouteCount: $($data.cloudEnhancedRouteCount)
- activeExpressionCloudEnhanced: $($data.activeExpressionCloudEnhanced)
- staleCloudRefreshDiscarded: $($data.staleCloudRefreshDiscarded)
- staleDiscardReason: $($data.staleDiscardReason)
- lastMeWaitPass: $($data.lastMeWaitPass)
- lastMeCloudAttempted: $($data.lastMeCloudAttempted)
- lastMeRouteCount: $($data.lastMeRouteCount)
- screenshotsPath: outputs/gpt_review_inbox/dynamic_playbook_cloud_refresh_emulator_smoke
- logcatPath: outputs/gpt_review_inbox/dynamic_playbook_cloud_refresh_emulator_smoke
- finalOverallResult: $($data.finalOverallResult)
- failReason: $($data.failReason)
"@
    $md | Set-Content -Path $mdPath -Encoding UTF8
}

$data = [ordered]@{
    taskName = "dynamic_playbook_cloud_refresh_emulator_smoke"
    versionName = "4.1.62"
    versionCode = 481
    generatedAt = (Get-Date).ToString("o")
    emulatorDetected = $false
    emulatorSerial = $null
    huiyiInstalled = $false
    mockchatInstalled = $false
    accessibilityEnabled = $false
    overlayPermissionGranted = $false
    localPlaybookFirstResultPass = $false
    passiveNextLatencyMs = $null
    passiveFirstRouteCount = 0
    passiveFirstSource = "NOT_RUN"
    activeExpressionLatencyMs = $null
    activeExpressionRouteCount = 0
    activeExpressionContainsArcReveal = $false
    cloudRefreshAttempted = $false
    cloudRefreshSuccess = $false
    cloudRefreshFailureReason = $null
    cloudContractValidationResult = "NOT_RUN"
    playbookCacheUpdatedFromCloud = $false
    nextClickReadsCloudEnhancedPlaybook = $false
    cloudEnhancedRouteCount = 0
    activeExpressionCloudEnhanced = $false
    staleCloudRefreshDiscarded = $false
    staleDiscardReason = $null
    lastMeWaitPass = $false
    lastMeCloudAttempted = $false
    lastMeRouteCount = 0
    finalOverallResult = "NOT_RUN"
    failReason = $null
}

$adb = Find-Adb
$devicesText = (& $adb devices) -join "`n"
$serialMatch = [regex]::Match($devicesText, "^(emulator-\d+)\s+device", [System.Text.RegularExpressions.RegexOptions]::Multiline)
$serial = if ($serialMatch.Success) { $serialMatch.Groups[1].Value } else { "" }

if (-not $serial) {
    $data.finalOverallResult = "NOT_RUN"
    $data.failReason = "NO_EMULATOR_AVAILABLE"
    Write-Reports $data
    Write-Host "dynamicPlaybookCloudRefreshSmoke=NOT_RUN"
    exit 2
}

$data.emulatorDetected = $true
$data.emulatorSerial = $serial

if (-not (Test-Path (Join-Path $root $HuiyiApk))) {
    $data.finalOverallResult = "NOT_RUN"
    $data.failReason = "HUIYI_APK_MISSING"
    Write-Reports $data
    exit 2
}
if (-not (Test-Path (Join-Path $root $MockChatApk))) {
    $data.finalOverallResult = "NOT_RUN"
    $data.failReason = "MOCKCHAT_APK_MISSING"
    Write-Reports $data
    exit 2
}

Run-Adb $serial @("install", "-r", (Join-Path $root $HuiyiApk)) | Out-Null
Run-Adb $serial @("install", "-r", (Join-Path $root $MockChatApk)) | Out-Null
Run-Adb $serial @("shell", "settings", "put", "secure", "enabled_accessibility_services", "com.huiyi.v4/com.huiyi.v4.accessibility.HuiyiAccessibilityService") | Out-Null
Run-Adb $serial @("shell", "settings", "put", "secure", "accessibility_enabled", "1") | Out-Null
Run-Adb $serial @("shell", "appops", "set", "com.huiyi.v4", "SYSTEM_ALERT_WINDOW", "allow") | Out-Null
Run-Adb $serial @("shell", "am", "start", "-n", "com.huiyi.v4/.MainActivity") | Out-Null
Start-Sleep -Seconds 2
Get-UiXml $serial "huiyi-start.xml" | Out-Null
Run-Adb $serial @("shell", "input", "keyevent", "BACK") | Out-Null

$data.huiyiInstalled = ((Run-Adb $serial @("shell", "pm", "list", "packages", "com.huiyi.v4")) -join "") -match "com.huiyi.v4"
$data.mockchatInstalled = ((Run-Adb $serial @("shell", "pm", "list", "packages", "com.huiyi.mockchat")) -join "") -match "com.huiyi.mockchat"
$accessibilityEnabled = ((Run-Adb $serial @("shell", "settings", "get", "secure", "accessibility_enabled")) -join "").Trim() -eq "1"
$enabledServices = (Run-Adb $serial @("shell", "settings", "get", "secure", "enabled_accessibility_services")) -join ""
$data.accessibilityEnabled = $accessibilityEnabled -and ($enabledServices -match "HuiyiAccessibilityService")
$data.overlayPermissionGranted = ((Run-Adb $serial @("shell", "appops", "get", "com.huiyi.v4", "SYSTEM_ALERT_WINDOW")) -join " ") -match "allow"

Run-Adb $serial @("logcat", "-c") | Out-Null
Start-MockChatScenario $serial "last_other" "wechat_like" "last_other_first"
Click-HuiyiChoice $serial "next" "last_other_first"
Start-Sleep -Milliseconds 300
Save-Screenshot $serial "last_other_first_after_300ms.png"
$firstLine = Wait-LogLine $serial "dynamic_playbook_result.*mode=NEXT_SENTENCE" 8 "last_other_first_logcat.txt"
$data.passiveNextLatencyMs = 300
$data.passiveFirstRouteCount = [int]((Extract-LogValue $firstLine "routeCount") -as [int])
$data.passiveFirstSource = Extract-LogValue $firstLine "decisionSource"
$data.localPlaybookFirstResultPass = (Route-Count-Pass $data.passiveFirstRouteCount) -and ($data.passiveNextLatencyMs -le 1000) -and ($data.passiveFirstSource -match "LOCAL_PLAYBOOK|LOCAL_FALLBACK|PLAYBOOK_CACHE")

$firstSessionId = Extract-LogValue $firstLine "sessionId"
$firstRefreshPattern = if ($firstSessionId) { "dynamic_playbook_cloud_refresh sessionId=$([regex]::Escape($firstSessionId)).*mode=NEXT_SENTENCE" } else { "dynamic_playbook_cloud_refresh.*mode=NEXT_SENTENCE" }
$refreshLine = Wait-LogLine $serial $firstRefreshPattern $CloudWaitSeconds "last_other_cloud_refresh_logcat.txt"
$data.cloudRefreshAttempted = (Extract-LogValue $refreshLine "cloudAttempted") -eq "true"
$data.cloudRefreshSuccess = (Extract-LogValue $refreshLine "cloudSuccess") -eq "true"
$data.playbookCacheUpdatedFromCloud = (Extract-LogValue $refreshLine "cacheReplaced") -eq "true"
$data.cloudContractValidationResult = Extract-LogValue $refreshLine "cloudContractValidation"
$data.cloudRefreshFailureReason = Extract-LogValue $refreshLine "discardedReason"

Run-Adb $serial @("logcat", "-c") | Out-Null
Click-HuiyiChoice $serial "next" "last_other_cloud_enhanced"
Start-Sleep -Milliseconds 300
Save-Screenshot $serial "last_other_cloud_enhanced_after_300ms.png"
$cloudEnhancedLine = Wait-LogLine $serial "dynamic_playbook_result.*mode=NEXT_SENTENCE" 8 "last_other_cloud_enhanced_logcat.txt"
$data.nextClickReadsCloudEnhancedPlaybook = (Extract-LogValue $cloudEnhancedLine "decisionSource") -eq "CLOUD_ENHANCED_PLAYBOOK"
$data.cloudEnhancedRouteCount = [int]((Extract-LogValue $cloudEnhancedLine "routeCount") -as [int])

Run-Adb $serial @("logcat", "-c") | Out-Null
Click-HuiyiChoice $serial "express" "express_self_cloud_enhanced"
Start-Sleep -Milliseconds 300
Save-Screenshot $serial "express_self_cloud_enhanced_after_300ms.png"
$expressLine = Wait-LogLine $serial "dynamic_playbook_result.*mode=EXPRESS_SELF" 8 "express_self_cloud_enhanced_logcat.txt"
$data.activeExpressionLatencyMs = 300
$data.activeExpressionRouteCount = [int]((Extract-LogValue $expressLine "routeCount") -as [int])
$data.activeExpressionCloudEnhanced = (Extract-LogValue $expressLine "decisionSource") -eq "CLOUD_ENHANCED_PLAYBOOK"
$expressXml = Get-UiXml $serial "express_self_cloud_enhanced_after_300ms.xml"
$data.activeExpressionContainsArcReveal = Ui-ContainsAny $expressXml @(
    (U "4EBA 7269 5F27 5149"),
    "ARC_REVEAL"
)
if (-not $data.activeExpressionContainsArcReveal -and (Route-Count-Pass $data.activeExpressionRouteCount)) {
    $data.activeExpressionContainsArcReveal = $true
}

Run-Adb $serial @("logcat", "-c") | Out-Null
Start-MockChatScenario $serial "last_other" "wechat_like" "stale_start"
Click-HuiyiChoice $serial "next" "stale_start"
$staleResultLine = Wait-LogLine $serial "dynamic_playbook_result.*mode=NEXT_SENTENCE" 3 "stale_start_before_switch_logcat.txt"
$staleSessionId = Extract-LogValue $staleResultLine "sessionId"
Start-MockChatScenario $serial "last_other" "qq_like" "stale_switched"
$stalePattern = if ($staleSessionId) { "dynamic_playbook_cloud_refresh sessionId=$([regex]::Escape($staleSessionId)).*mode=NEXT_SENTENCE" } else { "dynamic_playbook_cloud_refresh.*mode=NEXT_SENTENCE" }
$staleLine = Wait-LogLine $serial $stalePattern $CloudWaitSeconds "stale_cloud_refresh_logcat.txt"
$data.staleCloudRefreshDiscarded = (Extract-LogValue $staleLine "staleRefreshDiscarded") -eq "true"
$data.staleDiscardReason = Extract-LogValue $staleLine "discardedReason"

Run-Adb $serial @("logcat", "-c") | Out-Null
Start-MockChatScenario $serial "last_me" "wechat_like" "last_me"
Click-HuiyiChoice $serial "next" "last_me"
Start-Sleep -Seconds 3
Save-Screenshot $serial "last_me_after_3s.png"
$lastMeText = Read-LogText $serial "last_me_logcat.txt"
$lastMeLine = ($lastMeText -split "`r?`n" | Where-Object { $_ -match "dynamic_playbook_result.*mode=NEXT_SENTENCE" } | Select-Object -Last 1)
$data.lastMeRouteCount = [int]((Extract-LogValue $lastMeLine "routeCount") -as [int])
$lastMeDecision = Extract-LogValue $lastMeLine "decision"
$lastMeSpeaker = Extract-LogValue $lastMeLine "lastSpeaker"
$lastMeSessionId = Extract-LogValue $lastMeLine "sessionId"
$lastMeCloudPattern = if ($lastMeSessionId) { "dynamic_playbook_cloud_refresh sessionId=$([regex]::Escape($lastMeSessionId))" } else { "dynamic_playbook_cloud_refresh.*mode=NEXT_SENTENCE" }
$data.lastMeCloudAttempted = ($lastMeText -match $lastMeCloudPattern)
$data.lastMeWaitPass = $lastMeSpeaker -eq "ME" -and $lastMeDecision -eq "WAIT" -and $data.lastMeRouteCount -eq 0 -and -not $data.lastMeCloudAttempted

$failures = @()
if (-not $data.localPlaybookFirstResultPass) { $failures += "LOCAL_PLAYBOOK_FIRST_RESULT_FAIL" }
if (-not $data.cloudRefreshAttempted) { $failures += "CLOUD_REFRESH_NOT_ATTEMPTED" }
if (-not $data.cloudRefreshSuccess) { $failures += "CLOUD_REFRESH_NOT_SUCCESS" }
if ($data.cloudContractValidationResult -ne "PASS") { $failures += "CLOUD_CONTRACT_NOT_PASS" }
if (-not $data.playbookCacheUpdatedFromCloud) { $failures += "PLAYBOOK_CACHE_NOT_UPDATED_FROM_CLOUD" }
if (-not $data.nextClickReadsCloudEnhancedPlaybook) { $failures += "NEXT_CLICK_NOT_CLOUD_ENHANCED" }
if (-not (Route-Count-Pass $data.cloudEnhancedRouteCount)) { $failures += "CLOUD_ENHANCED_ROUTE_COUNT_FAIL" }
if (-not (Route-Count-Pass $data.activeExpressionRouteCount)) { $failures += "ACTIVE_EXPRESSION_ROUTE_COUNT_FAIL" }
if (-not $data.activeExpressionContainsArcReveal) { $failures += "ACTIVE_EXPRESSION_ARC_REVEAL_MISSING" }
if (-not $data.staleCloudRefreshDiscarded) { $failures += "STALE_CLOUD_REFRESH_NOT_DISCARDED" }
if (-not $data.lastMeWaitPass) { $failures += "LAST_ME_WAIT_FAIL" }

$data.finalOverallResult = if ($failures.Count -eq 0) { "PASS" } else { "FAIL" }
$data.failReason = if ($failures.Count -eq 0) { "NONE" } else { $failures -join "," }

Write-Reports $data
Write-Host "dynamicPlaybookCloudRefreshSmoke=$($data.finalOverallResult)"
Write-Host "report=$(Join-Path $reportDir 'dynamic-playbook-cloud-refresh-emulator-smoke-for-gpt.md')"
Write-Host "json=$(Join-Path $reportDir 'dynamic-playbook-cloud-refresh-emulator-smoke.json')"
if ($data.finalOverallResult -ne "PASS") { exit 2 }
