param(
    [string]$ApkPath = "outputs\huiyi-v4.1.10-debug.apk"
)

$ErrorActionPreference = "Stop"
$repo = (Resolve-Path ".").Path
$apk = Join-Path $repo $ApkPath

Write-Host "Huiyi v4.1.10 real-device scenario truth smoke"
Write-Host "APK: $apk"

if (-not (Test-Path $apk)) {
    throw "APK not found. Run .\gradlew.bat assembleDebug first, then copy the APK to outputs."
}

$adbCandidates = @(
    (Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe"),
    "adb.exe",
    "adb"
)

$adb = $null
foreach ($candidate in $adbCandidates) {
    try {
        $cmd = Get-Command $candidate -ErrorAction Stop
        $adb = $cmd.Source
        break
    } catch {
        if (Test-Path $candidate) {
            $adb = $candidate
            break
        }
    }
}

if (-not $adb) {
    throw "adb not found. Android SDK should be at $env:LOCALAPPDATA\Android\Sdk."
}

& $adb devices
Write-Host ""
Write-Host "Manual phone smoke steps:"
Write-Host "1. Install the APK if needed: adb install -r `"$apk`""
Write-Host "2. On the phone, enable Huiyi accessibility service and floating-window permission."
Write-Host "3. Open com.bajiao.im.liaoqi on a real chat page."
Write-Host "4. Wait 2 seconds so the pre-analysis chat snapshot is stable."
Write-Host "5. Tap Huiyi 下一句."
Write-Host "6. Export 导出真机验收包 from Huiyi developer settings."
Write-Host "7. Check outputs/review/huiyi-v4-review-for-gpt.md and outputs/gpt_review_inbox/."
Write-Host ""
Write-Host "PASS means product behavior matches actualLastSpeaker. If scenarioName conflicts with the actual screen, expect CONTROLLED_PASS_WITH_SCENARIO_MISMATCH, not parser FAIL."
