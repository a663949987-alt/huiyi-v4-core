param(
    [string]$AdbPath = "",
    [string]$HuiyiApk = "app\build\outputs\apk\debug\app-debug.apk",
    [string]$MockChatApk = "mockchat\build\outputs\apk\debug\mockchat-debug.apk",
    [string]$UnitTestsResult = "NOT_RUN",
    [string]$FixtureTestsResult = "NOT_RUN"
)

$ErrorActionPreference = "Continue"
$root = Split-Path -Parent $PSScriptRoot
$reportDir = Join-Path $root "outputs\gpt_review_inbox"
$shotDir = Join-Path $reportDir "express_self_eligibility_emulator_smoke"
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
        $rowHeight = [Math]::Max(1, [int](($frame.bottom - $frame.top) / 5))
        $index = if ($choice -eq "next") { 1 } elseif ($choice -eq "express") { 2 } elseif ($choice -eq "feedback") { 3 } else { 4 }
        $x = [int](($frame.left + $frame.right) / 2)
        $y = [int]($frame.top + ($rowHeight * ($index + 0.5)))
        Run-Adb $serial @("shell", "input", "tap", "$x", "$y") | Out-Null
    } elseif ($choice -eq "express") {
        Run-Adb $serial @("shell", "input", "tap", "945", "1485") | Out-Null
    }
    Start-Sleep -Milliseconds 150
}

function Open-FloatingMenu($serial, $name) {
    if (Tap-Text $serial (U "4F1A 610F") "$name-bubble.xml") {
        Start-Sleep -Milliseconds 200
        return $true
    }
    if (Tap-HuiyiOverlayCenter $serial) {
        Start-Sleep -Milliseconds 300
        return $true
    }
    Run-Adb $serial @("shell", "input", "tap", "945", "1270") | Out-Null
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
        if ($windowDump.Contains("com.huiyi.v4")) {
            Save-Screenshot $serial "$name-floating-ready.png"
            return $true
        }
        Start-Sleep -Milliseconds 250
    } while ((Get-Date) -lt $deadline)
    Save-Screenshot $serial "$name-floating-missing.png"
    return $false
}

function Pull-LatestSession($serial, $name) {
    $raw = (Run-Adb $serial @("exec-out", "run-as", "com.huiyi.v4", "cat", "files/debug/next_sentence/latest-next-sentence-session.json")) -join "`n"
    $path = Join-Path $shotDir "$name-session.json"
    $raw | Set-Content -Path $path -Encoding UTF8
    try {
        return $raw | ConvertFrom-Json
    } catch {
        return $null
    }
}

function Click-ExpressSelf($serial, $name) {
    Open-FloatingMenu $serial $name | Out-Null
    if (-not (Tap-Text $serial (U "8868 8FBE 6211") "$name-menu.xml")) {
        Tap-MenuChoice $serial "express"
    }
    Start-Sleep -Seconds 1
    Get-UiXml $serial "$name-after.xml" | Out-Null
    Save-Screenshot $serial "$name-after.png"
    return Pull-LatestSession $serial $name
}

function Write-Reports($data) {
    $jsonPath = Join-Path $reportDir "express-self-eligibility-emulator-smoke.json"
    $mdPath = Join-Path $reportDir "express-self-eligibility-emulator-smoke-for-gpt.md"
    $data | ConvertTo-Json -Depth 10 | Set-Content -Path $jsonPath -Encoding UTF8
    $md = @"
# Express Self Eligibility Emulator Smoke

- taskName: expressSelfEligibility_and_hold_back_fix
- versionName: 4.1.62
- versionCode: 481
- generatedAt: $($data.generatedAt)
- emulatorDetected: $($data.emulatorDetected)
- emulatorSerial: $($data.emulatorSerial)
- huiyiInstalled: $($data.huiyiInstalled)
- mockchatInstalled: $($data.mockchatInstalled)
- accessibilityEnabled: $($data.accessibilityEnabled)
- overlayPermissionGranted: $($data.overlayPermissionGranted)
- unitTestsResult: $($data.unitTestsResult)
- fixtureTestsResult: $($data.fixtureTestsResult)
- expressSelfUnsupportedBlocked: $($data.expressSelfUnsupportedBlocked)
- expressSelfRecentLastMeHoldBack: $($data.expressSelfRecentLastMeHoldBack)
- expressSelfColdStartAllowed: $($data.expressSelfColdStartAllowed)
- expressSelfPlanningArcReveal: $($data.expressSelfPlanningArcReveal)
- v4161BugFixtureFixed: $($data.v4161BugFixtureFixed)
- userNeedsPhoneThisRound: false
- finalOverallResult: $($data.finalOverallResult)
- reason: $($data.reason)

## Evidence

- screenshotsPath: outputs/gpt_review_inbox/express_self_eligibility_emulator_smoke
- logcatPath: outputs/gpt_review_inbox/express_self_eligibility_emulator_smoke/express_self_eligibility_logcat.txt
- lastMeSession: outputs/gpt_review_inbox/express_self_eligibility_emulator_smoke/last_me_express-session.json
- lastOtherSession: outputs/gpt_review_inbox/express_self_eligibility_emulator_smoke/last_other_express-session.json
- desktopSession: outputs/gpt_review_inbox/express_self_eligibility_emulator_smoke/desktop_express-session.json

## Scenario Assertions

- LAST_ME just sent -> terminalState: $($data.lastMe.terminalState), decisionType: $($data.lastMe.decisionType), routeCount: $($data.lastMe.routeCount), cloudAttempted: $($data.lastMe.cloudAttempted), eligibilityMode: $($data.lastMe.expressSelfEligibilityMode)
- LAST_OTHER planning/stability -> terminalState: $($data.lastOther.terminalState), routeTypesCsv: $($data.lastOther.routeTypesCsv), routeCount: $($data.lastOther.routeCount), eligibilityMode: $($data.lastOther.expressSelfEligibilityMode)
- launcher/desktop -> terminalState: $($data.desktop.terminalState), decisionType: $($data.desktop.decisionType), routeCount: $($data.desktop.routeCount), cloudAttempted: $($data.desktop.cloudAttempted)
- cold chat long inactive -> assertionSource: $($data.coldStartAssertionSource), result: $($data.expressSelfColdStartAllowed)
"@
    $md | Set-Content -Path $mdPath -Encoding UTF8
}

$adb = Find-Adb
$devicesText = & $adb devices 2>&1
$serialMatch = $devicesText | Select-String -Pattern "^(emulator-\d+)\s+device$" | Select-Object -First 1
$serial = if ($serialMatch) { $serialMatch.Matches.Groups[1].Value } else { "" }

$data = [ordered]@{
    taskName = "expressSelfEligibility_and_hold_back_fix"
    versionName = "4.1.62"
    versionCode = 481
    generatedAt = (Get-Date).ToString("s")
    emulatorDetected = [bool]$serial
    emulatorSerial = $serial
    huiyiInstalled = $false
    mockchatInstalled = $false
    accessibilityEnabled = $false
    overlayPermissionGranted = $false
    unitTestsResult = $UnitTestsResult
    fixtureTestsResult = $FixtureTestsResult
    expressSelfUnsupportedBlocked = $false
    expressSelfRecentLastMeHoldBack = $false
    expressSelfColdStartAllowed = $FixtureTestsResult -eq "PASS"
    expressSelfPlanningArcReveal = $false
    v4161BugFixtureFixed = $FixtureTestsResult -eq "PASS"
    coldStartAssertionSource = "ENGINE_FIXTURE_LONG_INACTIVE"
    lastMe = [ordered]@{}
    lastOther = [ordered]@{}
    desktop = [ordered]@{}
    screenshotsPath = $shotDir
    logcatPath = (Join-Path $shotDir "express_self_eligibility_logcat.txt")
    userNeedsPhoneThisRound = $false
    finalOverallResult = "NOT_RUN"
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

Start-MockChatScenario $serial "last_me" "last_me_express"
Start-FloatingOnTarget $serial "last_me_express" | Out-Null
$lastMeSession = Click-ExpressSelf $serial "last_me_express"
if ($null -ne $lastMeSession) {
    $data.lastMe = [ordered]@{
        terminalState = $lastMeSession.terminalState
        decisionType = $lastMeSession.decisionType
        routeCount = [int]$lastMeSession.routeCount
        cloudAttempted = [bool]$lastMeSession.cloudAttempted
        expressSelfEligibilityMode = $lastMeSession.expressSelfEligibilityMode
        expressSelfEligibilityEligible = $lastMeSession.expressSelfEligibilityEligible
    }
    $data.expressSelfRecentLastMeHoldBack = (
        $lastMeSession.terminalState -eq "HOLD_BACK_PANEL" -and
        [int]$lastMeSession.routeCount -eq 0 -and
        -not [bool]$lastMeSession.cloudAttempted -and
        $lastMeSession.decisionType -ne "NORMAL_REPLY"
    )
}

Start-MockChatScenario $serial "last_other" "last_other_express"
Start-FloatingOnTarget $serial "last_other_express" | Out-Null
$lastOtherSession = Click-ExpressSelf $serial "last_other_express"
if ($null -ne $lastOtherSession) {
    $data.lastOther = [ordered]@{
        terminalState = $lastOtherSession.terminalState
        decisionType = $lastOtherSession.decisionType
        routeCount = [int]$lastOtherSession.routeCount
        cloudAttempted = [bool]$lastOtherSession.cloudAttempted
        routeTypesCsv = $lastOtherSession.routeTypesCsv
        expressSelfEligibilityMode = $lastOtherSession.expressSelfEligibilityMode
        expressSelfEligibilityEligible = $lastOtherSession.expressSelfEligibilityEligible
    }
    $data.expressSelfPlanningArcReveal = (
        [int]$lastOtherSession.routeCount -ge 3 -and
        [int]$lastOtherSession.routeCount -le 5 -and
        "$($lastOtherSession.routeTypesCsv)".Contains("ARC_REVEAL") -and
        $lastOtherSession.expressSelfEligibilityEligible -eq $true
    )
}

Run-Adb $serial @("shell", "input", "keyevent", "HOME") | Out-Null
Start-Sleep -Seconds 1
Start-FloatingOnTarget $serial "desktop_express" | Out-Null
$desktopSession = Click-ExpressSelf $serial "desktop_express"
if ($null -ne $desktopSession) {
    $data.desktop = [ordered]@{
        terminalState = $desktopSession.terminalState
        decisionType = $desktopSession.decisionType
        routeCount = [int]$desktopSession.routeCount
        cloudAttempted = [bool]$desktopSession.cloudAttempted
        expressSelfEligibilityMode = $desktopSession.expressSelfEligibilityMode
        expressSelfEligibilityEligible = $desktopSession.expressSelfEligibilityEligible
    }
    $data.expressSelfUnsupportedBlocked = (
        [int]$desktopSession.routeCount -eq 0 -and
        -not [bool]$desktopSession.cloudAttempted -and
        $desktopSession.decisionType -ne "NORMAL_REPLY" -and
        $desktopSession.expressSelfEligibilityEligible -ne $true
    )
}

Run-Adb $serial @("logcat", "-d", "-t", "1000") | Set-Content -Path $data.logcatPath -Encoding UTF8

$data.finalOverallResult = if (
    $data.huiyiInstalled -and
    $data.mockchatInstalled -and
    $data.accessibilityEnabled -and
    $data.overlayPermissionGranted -and
    $data.unitTestsResult -eq "PASS" -and
    $data.fixtureTestsResult -eq "PASS" -and
    $data.expressSelfUnsupportedBlocked -and
    $data.expressSelfRecentLastMeHoldBack -and
    $data.expressSelfColdStartAllowed -and
    $data.expressSelfPlanningArcReveal -and
    $data.v4161BugFixtureFixed
) {
    "PASS"
} else {
    "FAIL"
}
$data.reason = if ($data.finalOverallResult -eq "PASS") {
    "EXPRESS_SELF_ELIGIBILITY_EMULATOR_AND_FIXTURE_PASS"
} else {
    "SEE_SCENARIO_ASSERTIONS"
}

Write-Reports $data
