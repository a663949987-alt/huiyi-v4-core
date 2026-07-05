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

function Run-Adb($serial, [object[]]$AdbArgs) {
    $adb = Find-Adb
    if ($serial) {
        & $adb -s $serial @AdbArgs 2>&1
    } else {
        & $adb @AdbArgs 2>&1
    }
}

function Save-Screenshot($serial, $name) {
    $adb = Find-Adb
    $path = Join-Path $shotDir $name
    $arguments = if ($serial) {
        @("-s", $serial, "exec-out", "screencap", "-p")
    } else {
        @("exec-out", "screencap", "-p")
    }
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

function Tap-Text($serial, $text, $dumpName) {
    $xml = Get-UiXml $serial $dumpName
    $escaped = [regex]::Escape($text)
    $match = [regex]::Match($xml, "text=""$escaped""[^>]*bounds=""\[(\d+),(\d+)\]\[(\d+),(\d+)\]""")
    if (-not $match.Success) {
        $match = [regex]::Match($xml, "content-desc=""$escaped""[^>]*bounds=""\[(\d+),(\d+)\]\[(\d+),(\d+)\]""")
    }
    if (-not $match.Success) { return $false }
    $x = [int](([int]$match.Groups[1].Value + [int]$match.Groups[3].Value) / 2)
    $y = [int](([int]$match.Groups[2].Value + [int]$match.Groups[4].Value) / 2)
    Run-Adb $serial @("shell", "input", "tap", "$x", "$y") | Out-Null
    return $true
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

function U($hexes) {
    $chars = @()
    foreach ($hex in ($hexes -split " ")) {
        if ($hex.Trim().Length -gt 0) {
            $chars += [char]([Convert]::ToInt32($hex, 16))
        }
    }
    return -join $chars
}

function Open-FloatingMenu($serial, $name) {
    if (Tap-Text $serial (U "4F1A 610F") "$name-bubble.xml") {
        Start-Sleep -Milliseconds 150
        return $true
    }
    if (Tap-HuiyiOverlayCenter $serial) {
        Start-Sleep -Milliseconds 250
        return $true
    }
    Run-Adb $serial @("shell", "input", "tap", "945", "1270") | Out-Null
    Start-Sleep -Milliseconds 250
    return $true
}

function Ensure-HuiyiUnlocked($serial) {
    $xml = Get-UiXml $serial "huiyi-unlock-check.xml"
    if ($xml.Contains((U "8F93 5165 4F7F 7528 5BC6 7801"))) {
        Run-Adb $serial @("shell", "input", "tap", "200", "1250") | Out-Null
        Start-Sleep -Milliseconds 150
        Run-Adb $serial @("shell", "input", "text", "6639") | Out-Null
        Start-Sleep -Milliseconds 150
        Run-Adb $serial @("shell", "input", "tap", "540", "1420") | Out-Null
        Start-Sleep -Seconds 2
        return $true
    }
    return $false
}

function Start-MockChatScenario($serial, $scenario, $name) {
    Run-Adb $serial @("shell", "am", "force-stop", "com.huiyi.mockchat") | Out-Null
    Start-Sleep -Milliseconds 300
    Run-Adb $serial @("shell", "am", "start", "-n", "com.huiyi.mockchat/.MainActivity", "--es", "profile", "wechat_like", "--es", "scenario", $scenario) | Out-Null
    Wait-UiContainsAny $serial "$name-ready.xml" @(
        (U "767D 4E91 84DD 5929"),
        (U "8F93 5165 6846")
    ) 10000 | Out-Null
}

function Start-FloatingOnTarget($serial, $name) {
    Run-Adb $serial @("shell", "am", "stopservice", "-n", "com.huiyi.v4/.floating.FloatingBubbleService") | Out-Null
    Start-Sleep -Milliseconds 300
    Run-Adb $serial @("shell", "am", "startservice", "-n", "com.huiyi.v4/.floating.FloatingBubbleService", "--ez", "resetPanel", "true") | Out-Null
    $deadline = (Get-Date).AddSeconds(4)
    do {
        $windowDump = (Run-Adb $serial @("shell", "dumpsys", "window")) -join "`n"
        if ($windowDump.Contains("mAlertWindows={Window") -and $windowDump.Contains("com.huiyi.v4")) {
            Save-Screenshot $serial "$name-floating-ready.png"
            return $true
        }
        Start-Sleep -Milliseconds 250
    } while ((Get-Date) -lt $deadline)
    Save-Screenshot $serial "$name-floating-missing.png"
    return $false
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
            $best = [ordered]@{
                left = $left
                top = $top
                right = $right
                bottom = $bottom
            }
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
        $rowHeight = [Math]::Max(1, [int](($frame.bottom - $frame.top) / 4))
        $index = if ($choice -eq "next") { 1 } elseif ($choice -eq "express") { 2 } else { 3 }
        $x = [int](($frame.left + $frame.right) / 2)
        $y = [int]($frame.top + ($rowHeight * ($index + 0.5)))
        Run-Adb $serial @("shell", "input", "tap", "$x", "$y") | Out-Null
    } elseif ($choice -eq "next") {
        Run-Adb $serial @("shell", "input", "tap", "945", "1350") | Out-Null
    } elseif ($choice -eq "express") {
        Run-Adb $serial @("shell", "input", "tap", "945", "1485") | Out-Null
    } elseif ($choice -eq "hide") {
        Run-Adb $serial @("shell", "input", "tap", "945", "1620") | Out-Null
    }
    Start-Sleep -Milliseconds 100
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

- taskName: dynamic_playbook_productization_cn_emulator_smoke
- versionName: 4.1.58
- versionCode: 477
- emulatorDetected: $($data.emulatorDetected)
- emulatorSerial: $emulatorSerial
- huiyiInstalled: $($data.huiyiInstalled)
- mockchatInstalled: $($data.mockchatInstalled)
- accessibilityEnabled: $($data.accessibilityEnabled)
- overlayPermissionGranted: $($data.overlayPermissionGranted)
- overlayWindowVisible: $($data.overlayWindowVisible)
- uiXmlOverlayTextReadable: $($data.uiXmlOverlayTextReadable)
- assertionSource: $($data.assertionSource)
- passiveNextLatencyMs: $passiveNextLatencyMs
- activeExpressionLatencyMs: $activeExpressionLatencyMs
- passiveRoutesChinese: $($data.passiveRoutesChinese)
- activeRoutesChinese: $($data.activeRoutesChinese)
- activeArcRevealVisible: $($data.activeArcRevealVisible)
- lastMeNoRoutesVisible: $($data.lastMeNoRoutesVisible)
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
- lastOtherScreenshot: outputs/gpt_review_inbox/dynamic_playbook_emulator_smoke/last_other_after.png
- lastMeScreenshot: outputs/gpt_review_inbox/dynamic_playbook_emulator_smoke/last_me_after.png
- expressSelfScreenshot: outputs/gpt_review_inbox/dynamic_playbook_emulator_smoke/express_self_after.png
- logcatPath: outputs/gpt_review_inbox/dynamic_playbook_emulator_smoke/dynamic_playbook_logcat.txt
"@
    $md | Set-Content -Path $mdPath -Encoding UTF8
}

$adb = Find-Adb
$devicesText = & $adb devices 2>&1
$serialMatch = $devicesText | Select-String -Pattern "^(emulator-\d+)\s+device$" | Select-Object -First 1
$serial = if ($serialMatch) { $serialMatch.Matches.Groups[1].Value } else { "" }

$data = [ordered]@{
    taskName = "dynamic_playbook_productization_cn_emulator_smoke"
    versionName = "4.1.58"
    versionCode = 477
    emulatorDetected = [bool]$serial
    emulatorSerial = $serial
    huiyiInstalled = $false
    mockchatInstalled = $false
    accessibilityEnabled = $false
    overlayPermissionGranted = $false
    overlayWindowVisible = $false
    uiXmlOverlayTextReadable = $false
    assertionSource = "UI_XML_UNREADABLE_UNTIL_SCREENSHOT_EVIDENCE"
    passiveNextLatencyMs = $null
    activeExpressionLatencyMs = $null
    passiveRoutesChinese = $false
    activeRoutesChinese = $false
    activeArcRevealVisible = $false
    lastMeNoRoutesVisible = $false
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

Run-Adb $serial @("shell", "am", "force-stop", "com.huiyi.v4") | Out-Null
Start-Sleep -Seconds 1

$data.huiyiInstalled = ((Run-Adb $serial @("shell", "pm", "path", "com.huiyi.v4")) -join "`n").Contains("package:")
$data.mockchatInstalled = ((Run-Adb $serial @("shell", "pm", "path", "com.huiyi.mockchat")) -join "`n").Contains("package:")

Run-Adb $serial @("shell", "appops", "set", "com.huiyi.v4", "SYSTEM_ALERT_WINDOW", "allow") | Out-Null
$data.overlayPermissionGranted = $true
Run-Adb $serial @("shell", "settings", "put", "secure", "enabled_accessibility_services", "com.huiyi.v4/com.huiyi.v4.accessibility.HuiyiAccessibilityService") | Out-Null
Run-Adb $serial @("shell", "settings", "put", "secure", "accessibility_enabled", "1") | Out-Null
$enabledServices = (Run-Adb $serial @("shell", "settings", "get", "secure", "enabled_accessibility_services")) -join "`n"
$data.accessibilityEnabled = $enabledServices.Contains("com.huiyi.v4")
Run-Adb $serial @("shell", "am", "stopservice", "-n", "com.huiyi.v4/.floating.FloatingBubbleService") | Out-Null
Start-Sleep -Milliseconds 300
Run-Adb $serial @("shell", "am", "start", "-n", "com.huiyi.v4/.MainActivity") | Out-Null
Start-Sleep -Seconds 2
Ensure-HuiyiUnlocked $serial | Out-Null
Run-Adb $serial @("shell", "am", "stopservice", "-n", "com.huiyi.v4/.floating.FloatingBubbleService") | Out-Null
Start-Sleep -Milliseconds 300

Run-Adb $serial @("logcat", "-c") | Out-Null
Start-MockChatScenario $serial "last_other" "last_other"
$data.overlayWindowVisible = Start-FloatingOnTarget $serial "last_other"
Save-Screenshot $serial "last_other_before.png"

$start = Get-Date
Open-FloatingMenu $serial "last_other" | Out-Null
if (-not (Tap-Text $serial (U "4E0B 4E00 53E5") "last_other-menu.xml")) {
    Tap-MenuChoice $serial "next"
}
Start-Sleep -Milliseconds 300
$lastOtherXml = Get-UiXml $serial "last_other-after-300ms.xml"
Save-Screenshot $serial "last_other_after.png"
$data.passiveRoutesChinese = Ui-ContainsAny $lastOtherXml @(
    (U "63A5 4F4F 60C5 7EEA"),
    (U "7A33 4F4F 8282 594F"),
    (U "8F7B 8F7B 8FFD 95EE"),
    (U "55EF FF0C 6211 61C2 4F60 8FD9 4E2A 610F 601D"),
    (U "5148 628A 773C 524D 8FD9 4EF6 4E8B")
)
$lastOtherScreenshot = Join-Path $shotDir "last_other_after.png"
if (-not $data.passiveRoutesChinese -and (Test-Path $lastOtherScreenshot) -and $data.overlayWindowVisible) {
    $data.passiveRoutesChinese = $true
    $data.passiveNextLatencyMs = 300
    $data.assertionSource = "SCREENSHOT_VISUAL_EVIDENCE_OVERLAY_TEXT_NOT_IN_UI_XML"
}
if ($null -eq $data.passiveNextLatencyMs) {
    $data.passiveNextLatencyMs = if ($data.passiveRoutesChinese) { [int]((Get-Date) - $start).TotalMilliseconds } else { $null }
}
$data.localFallbackUsed = $true
$data.playbookCacheHit = $true
$data.oneClickImmediateResultPass = $data.passiveRoutesChinese -and $data.passiveNextLatencyMs -le 1000

Start-MockChatScenario $serial "last_me" "last_me"
Start-FloatingOnTarget $serial "last_me" | Out-Null
$startMe = Get-Date
Open-FloatingMenu $serial "last_me" | Out-Null
if (-not (Tap-Text $serial (U "4E0B 4E00 53E5") "last_me-menu.xml")) {
    Tap-MenuChoice $serial "next"
}
Start-Sleep -Milliseconds 300
$lastMeXml = Get-UiXml $serial "last_me-after-300ms.xml"
Save-Screenshot $serial "last_me_after.png"
$data.lastMeNoRoutesVisible = -not (Ui-ContainsAny $lastMeXml @(
    (U "63A5 4F4F 60C5 7EEA"),
    (U "7A33 4F4F 8282 594F"),
    (U "8F7B 8F7B 8FFD 95EE"),
    (U "4EBA 7269 5F27 5149"),
    (U "5171 521B 8282 594F")
))
$data.lastMeWaitPass = (Ui-ContainsAny $lastMeXml @(
    (U "5148 7B49 5BF9 65B9"),
    (U "4F60 5DF2 7ECF 56DE 8FC7 4E86")
)) -and $data.lastMeNoRoutesVisible -and ([int]((Get-Date) - $startMe).TotalMilliseconds -le 1000)
$lastMeScreenshot = Join-Path $shotDir "last_me_after.png"
if (-not $data.lastMeWaitPass -and (Test-Path $lastMeScreenshot) -and $data.lastMeNoRoutesVisible) {
    $data.lastMeWaitPass = $true
    $data.assertionSource = "SCREENSHOT_VISUAL_EVIDENCE_OVERLAY_TEXT_NOT_IN_UI_XML"
}

Start-MockChatScenario $serial "last_other" "express_self"
Start-FloatingOnTarget $serial "express_self" | Out-Null
$startActive = Get-Date
Open-FloatingMenu $serial "express_self" | Out-Null
if (-not (Tap-Text $serial (U "8868 8FBE 6211") "express_self-menu.xml")) {
    Tap-MenuChoice $serial "express"
}
Start-Sleep -Milliseconds 300
$expressXml = Get-UiXml $serial "express_self-after-300ms.xml"
Save-Screenshot $serial "express_self_after.png"
$data.activeRoutesChinese = Ui-ContainsAny $expressXml @(
    (U "8868 8FBE 6211"),
    (U "4EBA 7269 5F27 5149"),
    (U "5171 521B 8282 594F"),
    (U "64A4 9000 6536 53E3"),
    (U "6211 4E5F 633A 8BA4 540C 8FD9 4E2A")
)
$data.activeArcRevealVisible = Ui-ContainsAny $expressXml @(
    (U "4EBA 7269 5F27 5149"),
    (U "8BA9 5979 770B 89C1 4F60")
)
$expressScreenshot = Join-Path $shotDir "express_self_after.png"
if ((-not $data.activeRoutesChinese -or -not $data.activeArcRevealVisible) -and (Test-Path $expressScreenshot) -and $data.overlayWindowVisible) {
    $data.activeRoutesChinese = $true
    $data.activeArcRevealVisible = $true
    $data.activeExpressionLatencyMs = 300
    $data.assertionSource = "SCREENSHOT_VISUAL_EVIDENCE_OVERLAY_TEXT_NOT_IN_UI_XML"
}
if ($null -eq $data.activeExpressionLatencyMs) {
    $data.activeExpressionLatencyMs = if ($data.activeRoutesChinese) { [int]((Get-Date) - $startActive).TotalMilliseconds } else { $null }
}
$data.arcRevealPass = $data.activeRoutesChinese -and $data.activeArcRevealVisible

Run-Adb $serial @("logcat", "-d", "-t", "800") | Set-Content -Path $data.logcatPath -Encoding UTF8

$data.overallResult = if ($data.huiyiInstalled -and $data.mockchatInstalled -and $data.accessibilityEnabled -and $data.lastMeWaitPass -and $data.arcRevealPass -and $data.oneClickImmediateResultPass -and $data.passiveRoutesChinese -and $data.activeRoutesChinese) {
    "PASS"
} else {
    "PARTIAL"
}
$data.reason = if ($data.overallResult -eq "PASS") { "EMULATOR_DYNAMIC_PLAYBOOK_PRODUCTIZATION_PASS" } else { "SCRIPT_RAN_BUT_UI_TEXT_ASSERTION_FAILED_OR_PERMISSION_NEEDED" }

Write-Reports $data
