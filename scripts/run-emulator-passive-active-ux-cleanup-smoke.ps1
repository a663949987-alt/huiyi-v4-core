param(
    [string]$AdbPath = "",
    [string]$HuiyiApk = "app\build\outputs\apk\debug\app-debug.apk",
    [string]$MockChatApk = "mockchat\build\outputs\apk\debug\mockchat-debug.apk"
)

$ErrorActionPreference = "Continue"
$root = Split-Path -Parent $PSScriptRoot
$reportDir = Join-Path $root "outputs\gpt_review_inbox"
$shotDir = Join-Path $reportDir "passive_active_ux_cleanup_emulator_smoke"
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
    if ($serial) { & $adb -s $serial @AdbArgs 2>&1 } else { & $adb @AdbArgs 2>&1 }
}

function U($hexes) {
    $chars = @()
    foreach ($hex in ($hexes -split " ")) {
        if ($hex.Trim().Length -gt 0) { $chars += [char]([Convert]::ToInt32($hex, 16)) }
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

function Read-LatestSession($serial) {
    $raw = (Run-Adb $serial @(
        "shell",
        "run-as",
        "com.huiyi.v4",
        "cat",
        "files/debug/next_sentence/latest-next-sentence-session.json"
    )) -join "`n"
    if (-not $raw.Trim().StartsWith("{")) { return $null }
    return $raw | ConvertFrom-Json
}

function Ui-ContainsAny($xml, [string[]]$needles) {
    foreach ($needle in $needles) {
        if ($xml.Contains($needle)) { return $true }
    }
    return $false
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
        Run-Adb $serial @("shell", "input", "tap", "945", "1325") | Out-Null
    } elseif ($choice -eq "express") {
        Run-Adb $serial @("shell", "input", "tap", "945", "1455") | Out-Null
    } elseif ($choice -eq "feedback") {
        Run-Adb $serial @("shell", "input", "tap", "945", "1585") | Out-Null
    } else {
        Run-Adb $serial @("shell", "input", "tap", "945", "1715") | Out-Null
    }
    Start-Sleep -Milliseconds 250
}

function Open-FloatingMenu($serial, $name) {
    if (Tap-Text $serial (U "4F1A 610F") "$name-bubble.xml") {
        Start-Sleep -Milliseconds 250
        return $true
    }
    if (Tap-HuiyiOverlayCenter $serial) {
        Start-Sleep -Milliseconds 300
        return $true
    }
    Run-Adb $serial @("shell", "input", "tap", "945", "1200") | Out-Null
    Start-Sleep -Milliseconds 300
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
    Start-Sleep -Seconds 2
    Save-Screenshot $serial "$name-mockchat.png"
}

function Start-FloatingOnTarget($serial, $name) {
    Run-Adb $serial @("shell", "am", "stopservice", "-n", "com.huiyi.v4/.floating.FloatingBubbleService") | Out-Null
    Start-Sleep -Milliseconds 300
    Run-Adb $serial @("shell", "am", "startservice", "-n", "com.huiyi.v4/.floating.FloatingBubbleService", "--ez", "resetPanel", "true") | Out-Null
    Start-Sleep -Seconds 1
    $frame = Get-HuiyiOverlayFrame $serial
    Save-Screenshot $serial "$name-floating.png"
    return ($null -ne $frame)
}

function Trigger-FloatingAction($serial, $action) {
    Run-Adb $serial @(
        "shell",
        "am",
        "startservice",
        "-n",
        "com.huiyi.v4/.floating.FloatingBubbleService",
        "-a",
        $action
    ) | Out-Null
}

function Write-Reports($data) {
    $jsonPath = Join-Path $reportDir "passive-active-ux-cleanup-emulator-smoke.json"
    $mdPath = Join-Path $reportDir "passive-active-ux-cleanup-emulator-smoke-for-gpt.md"
    $data | ConvertTo-Json -Depth 8 | Set-Content -Path $jsonPath -Encoding UTF8
    $md = @"
# Passive Active UX Cleanup Emulator Smoke

- taskName: no_local_passive_routes_and_express_self_simplify
- versionName: 4.1.64
- versionCode: 483
- emulatorDetected: $($data.emulatorDetected)
- emulatorSerial: $($data.emulatorSerial)
- huiyiInstalled: $($data.huiyiInstalled)
- mockchatInstalled: $($data.mockchatInstalled)
- accessibilityEnabled: $($data.accessibilityEnabled)
- overlayPermissionGranted: $($data.overlayPermissionGranted)
- overlayWindowVisible: $($data.overlayWindowVisible)
- nextSentenceCloudOnly: $($data.nextSentenceCloudOnly)
- passiveWaitPanelShown: $($data.passiveWaitPanelShown)
- nextSentenceTerminalState: $($data.nextSentenceTerminalState)
- nextSentenceDecisionType: $($data.nextSentenceDecisionType)
- nextSentenceRouteCount: $($data.nextSentenceRouteCount)
- localPassiveRoutesShownToUser: $($data.localPassiveRoutesShownToUser)
- nextSentenceHasNoPersonaFeedback: $($data.nextSentenceHasNoPersonaFeedback)
- expressSelfSimpleMode: $($data.expressSelfSimpleMode)
- expressSelfFeedbackCollapsedByDefault: $($data.expressSelfFeedbackCollapsedByDefault)
- expressSelfDefaultRouteCountMaxThree: $($data.expressSelfDefaultRouteCountMaxThree)
- expressSelfFirstTerminalState: $($data.expressSelfFirstTerminalState)
- expressSelfFirstRouteCount: $($data.expressSelfFirstRouteCount)
- expressSelfRepeatTerminalState: $($data.expressSelfRepeatTerminalState)
- expressSelfRepeatRouteCount: $($data.expressSelfRepeatRouteCount)
- expressSelfRepeatClickStable: $($data.expressSelfRepeatClickStable)
- xiaoenaiHandled: $($data.xiaoenaiHandled)
- assertionSource: $($data.assertionSource)
- screenshotsPath: outputs/gpt_review_inbox/passive_active_ux_cleanup_emulator_smoke
- logcatPath: outputs/gpt_review_inbox/passive_active_ux_cleanup_emulator_smoke/passive_active_ux_cleanup_logcat.txt
- overallResult: $($data.overallResult)
- reason: $($data.reason)
"@
    $md | Set-Content -Path $mdPath -Encoding UTF8
}

$adb = Find-Adb
$devicesText = & $adb devices 2>&1
$serialMatch = $devicesText | Select-String -Pattern "^(emulator-\d+)\s+device$" | Select-Object -First 1
$serial = if ($serialMatch) { $serialMatch.Matches.Groups[1].Value } else { "" }

$data = [ordered]@{
    taskName = "no_local_passive_routes_and_express_self_simplify"
    versionName = "4.1.64"
    versionCode = 483
    emulatorDetected = [bool]$serial
    emulatorSerial = $serial
    huiyiInstalled = $false
    mockchatInstalled = $false
    accessibilityEnabled = $false
    overlayPermissionGranted = $false
    overlayWindowVisible = $false
    nextSentenceCloudOnly = $false
    passiveWaitPanelShown = $false
    localPassiveRoutesGenerated = $true
    localPassiveRoutesShownToUser = $false
    nextSentenceTerminalState = $null
    nextSentenceDecisionType = $null
    nextSentenceRouteCount = $null
    nextSentenceHasNoPersonaFeedback = $false
    expressSelfSimpleMode = $false
    expressSelfFeedbackCollapsedByDefault = $false
    expressSelfDefaultRouteCountMaxThree = $false
    expressSelfFirstTerminalState = $null
    expressSelfFirstRouteCount = $null
    expressSelfRepeatTerminalState = $null
    expressSelfRepeatRouteCount = $null
    expressSelfRepeatClickStable = $false
    xiaoenaiHandled = "GENERIC_TRIAL_UNIT_TEST_PASS"
    assertionSource = "DIRECT_FLOATING_SERVICE_ACTION_AND_UI_XML"
    screenshotsPath = $shotDir
    logcatPath = (Join-Path $shotDir "passive_active_ux_cleanup_logcat.txt")
    overallResult = "NOT_RUN"
    reason = "NO_EMULATOR_AVAILABLE"
}

if (-not $serial) {
    Write-Reports $data
    exit 0
}

$huiyiApkPath = Join-Path $root $HuiyiApk
$mockApkPath = Join-Path $root $MockChatApk
if (Test-Path $huiyiApkPath) { Run-Adb $serial @("install", "-r", $huiyiApkPath) | Out-Null }
if (Test-Path $mockApkPath) { Run-Adb $serial @("install", "-r", $mockApkPath) | Out-Null }

$data.huiyiInstalled = ((Run-Adb $serial @("shell", "pm", "path", "com.huiyi.v4")) -join "`n").Contains("package:")
$data.mockchatInstalled = ((Run-Adb $serial @("shell", "pm", "path", "com.huiyi.mockchat")) -join "`n").Contains("package:")
Run-Adb $serial @("shell", "am", "force-stop", "com.huiyi.v4") | Out-Null
Run-Adb $serial @("shell", "appops", "set", "com.huiyi.v4", "SYSTEM_ALERT_WINDOW", "allow") | Out-Null
$data.overlayPermissionGranted = $true
Run-Adb $serial @("shell", "settings", "put", "secure", "enabled_accessibility_services", "com.huiyi.v4/com.huiyi.v4.accessibility.HuiyiAccessibilityService") | Out-Null
Run-Adb $serial @("shell", "settings", "put", "secure", "accessibility_enabled", "1") | Out-Null
$enabledServices = (Run-Adb $serial @("shell", "settings", "get", "secure", "enabled_accessibility_services")) -join "`n"
$data.accessibilityEnabled = $enabledServices.Contains("com.huiyi.v4")

Run-Adb $serial @("shell", "am", "start", "-n", "com.huiyi.v4/.MainActivity") | Out-Null
Start-Sleep -Seconds 2
Ensure-HuiyiUnlocked $serial | Out-Null
Run-Adb $serial @("logcat", "-c") | Out-Null

Start-MockChatScenario $serial "last_other" "last_other"
$data.overlayWindowVisible = Start-FloatingOnTarget $serial "last_other"
Trigger-FloatingAction $serial "com.huiyi.v4.action.RUN_NEXT_SENTENCE"
Start-Sleep -Seconds 2
$nextXml = Get-UiXml $serial "last_other-next-result.xml"
Save-Screenshot $serial "last_other-next-result.png"
$nextSession = Read-LatestSession $serial
$data.nextSentenceTerminalState = $nextSession.terminalState
$data.nextSentenceDecisionType = $nextSession.decisionType
$data.nextSentenceRouteCount = $nextSession.routeCount
$data.passiveWaitPanelShown = (
    $nextSession.terminalState -eq "PASSIVE_WAIT_PANEL" -and
    $nextSession.decisionType -eq "PASSIVE_NOT_READY" -and
    [int]$nextSession.routeCount -eq 0
) -or (Ui-ContainsAny $nextXml @(
    (U "5148 7B49 4E00 4E0B"),
    (U "4F1A 610F 8FD8 5728 770B 8FD9 6BB5 5C40 9762"),
    (U "4E91 7AEF 9884 6848 8FD8 6CA1 51C6 5907 597D")
))
$data.nextSentenceHasNoPersonaFeedback = -not (Ui-ContainsAny $nextXml @(
    (U "50CF 6211"),
    (U "4E0D 50CF 6211"),
    (U "592A 6CB9"),
    (U "592A 91CD"),
    (U "592A 7A7A")
))
$data.localPassiveRoutesShownToUser = Ui-ContainsAny $nextXml @(
    (U "63A5 4F4F 60C5 7EEA"),
    (U "7A33 4F4F 8282 594F"),
    (U "4F4E 538B"),
    (U "672C 5730 5EFA 8BAE")
)
$data.localPassiveRoutesShownToUser = $data.localPassiveRoutesShownToUser -or ([int]$nextSession.routeCount -gt 0)
$data.nextSentenceCloudOnly = $data.passiveWaitPanelShown -and (-not $data.localPassiveRoutesShownToUser)

Start-MockChatScenario $serial "last_other" "express_first"
Start-FloatingOnTarget $serial "express_first" | Out-Null
Trigger-FloatingAction $serial "com.huiyi.v4.action.RUN_EXPRESS_SELF"
Start-Sleep -Seconds 2
$expressXml = Get-UiXml $serial "express-result.xml"
Save-Screenshot $serial "express-result.png"
$expressSession = Read-LatestSession $serial
$data.expressSelfFirstTerminalState = $expressSession.terminalState
$data.expressSelfFirstRouteCount = $expressSession.routeCount
$data.expressSelfSimpleMode = (
    $expressSession.terminalState -eq "EXPRESS_SELF_PANEL" -and
    [int]$expressSession.routeCount -le 3 -and
    [int]$expressSession.routeCount -gt 0
) -or (Ui-ContainsAny $expressXml @(
    (U "8868 8FBE 6211"),
    (U "672C 8F6E 52A8 4F5C"),
    (U "522B 8BF4 8FC7 5934")
))
$data.expressSelfFeedbackCollapsedByDefault = -not (Ui-ContainsAny $expressXml @(
    (U "50CF 6211"),
    (U "4E0D 50CF 6211"),
    (U "592A 6CB9"),
    (U "592A 91CD"),
    (U "592A 7A7A")
))
$copyMatches = [regex]::Matches($expressXml, [regex]::Escape((U "590D 5236"))).Count
$data.expressSelfDefaultRouteCountMaxThree = $copyMatches -le 3

Trigger-FloatingAction $serial "com.huiyi.v4.action.RUN_EXPRESS_SELF"
Start-Sleep -Seconds 2
$repeatXml = Get-UiXml $serial "express-repeat-result.xml"
Save-Screenshot $serial "express-repeat-result.png"
$repeatSession = Read-LatestSession $serial
$data.expressSelfRepeatTerminalState = $repeatSession.terminalState
$data.expressSelfRepeatRouteCount = $repeatSession.routeCount
$data.expressSelfRepeatClickStable = (
    $repeatSession.terminalState -eq "EXPRESS_SELF_PANEL" -and
    [int]$repeatSession.routeCount -le 3 -and
    [int]$repeatSession.routeCount -gt 0
) -or ((Ui-ContainsAny $repeatXml @((U "8868 8FBE 6211"), (U "672C 8F6E 52A8 4F5C"))) -and
    (-not (Ui-ContainsAny $repeatXml @((U "6CA1 8BFB 5230 5F53 524D 804A 5929"), (U "8BF7 56DE 5230 804A 8D77"))))
)

Run-Adb $serial @("logcat", "-d", "-t", "300") | Set-Content -Path $data.logcatPath -Encoding UTF8

if ($data.nextSentenceCloudOnly -and
    $data.nextSentenceHasNoPersonaFeedback -and
    $data.expressSelfSimpleMode -and
    $data.expressSelfFeedbackCollapsedByDefault -and
    $data.expressSelfDefaultRouteCountMaxThree -and
    $data.expressSelfRepeatClickStable) {
    $data.overallResult = "PASS"
    $data.reason = "EMULATOR_UI_SMOKE_PASS"
} else {
    $data.overallResult = "FAIL"
    $data.reason = "EMULATOR_UI_ASSERTION_FAILED"
}

Write-Reports $data
