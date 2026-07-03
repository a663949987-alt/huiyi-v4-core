param(
    [string]$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path,
    [string]$AdbPath = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
)

$ErrorActionPreference = "Stop"

$report = [ordered]@{
    taskName = "relay_cloud_smoke_before_user_phone"
    versionName = "4.1.28"
    versionCode = 447
    generatedAt = (Get-Date).ToString("o")
    emulatorCloudSmokeResult = "NOT_RUN_NO_EMULATOR"
    emulatorDetected = $false
    emulatorSerial = $null
    huiyiInstalled = $false
    mockchatInstalled = $false
    accessibilityEnabled = $false
    overlayPermissionGranted = $false
    cloudAttempted = $false
    cloudSuccess = $false
    decisionSource = "NOT_RUN"
    routeCount = 0
    reason = "NO_RUNNING_EMULATOR"
}

if (Test-Path -LiteralPath $AdbPath) {
    $lines = & $AdbPath devices
    $serial = $lines | Where-Object { $_ -match '^emulator-\d+\s+device' } | ForEach-Object { ($_ -split '\s+')[0] } | Select-Object -First 1
    if ($serial) {
        $report.emulatorDetected = $true
        $report.emulatorSerial = $serial
        $report.emulatorCloudSmokeResult = "FAIL"
        $report.reason = "EMULATOR_PRESENT_BUT_AUTOMATED_CLOUD_UI_FLOW_NOT_RUN"
    }
} else {
    $report.reason = "ADB_NOT_FOUND"
}

$outDir = Join-Path $ProjectRoot "outputs/gpt_review_inbox"
New-Item -ItemType Directory -Force -Path $outDir | Out-Null
$jsonPath = Join-Path $outDir "emulator-cloud-smoke-report.json"
$mdPath = Join-Path $outDir "emulator-cloud-smoke-report-for-gpt.md"
$report | ConvertTo-Json -Depth 10 | Set-Content -LiteralPath $jsonPath -Encoding UTF8

$markdown = @"
# Emulator Cloud Smoke Report

- taskName: $($report.taskName)
- versionName: $($report.versionName)
- versionCode: $($report.versionCode)
- generatedAt: $($report.generatedAt)
- emulatorCloudSmokeResult: $($report.emulatorCloudSmokeResult)
- emulatorDetected: $($report.emulatorDetected)
- emulatorSerial: $($report.emulatorSerial)
- huiyiInstalled: $($report.huiyiInstalled)
- mockchatInstalled: $($report.mockchatInstalled)
- accessibilityEnabled: $($report.accessibilityEnabled)
- overlayPermissionGranted: $($report.overlayPermissionGranted)
- cloudAttempted: $($report.cloudAttempted)
- cloudSuccess: $($report.cloudSuccess)
- decisionSource: $($report.decisionSource)
- routeCount: $($report.routeCount)
- reason: $($report.reason)
"@
Set-Content -LiteralPath $mdPath -Value $markdown -Encoding UTF8

Write-Host "emulatorCloudSmokeResult=$($report.emulatorCloudSmokeResult)"
Write-Host "report=$mdPath"
Write-Host "json=$jsonPath"
if ($report.emulatorCloudSmokeResult -eq "FAIL") { exit 2 }
