param(
    [ValidateSet("USER_ASSERTED_LAST_ME", "USER_ASSERTED_LAST_OTHER")]
    [string]$TestIntent = "USER_ASSERTED_LAST_ME",
    [string]$PackageName = "com.bajiao.im.liaoqi"
)

$ErrorActionPreference = "Stop"
$repo = Split-Path -Parent $PSScriptRoot
$adb = "adb"

Write-Host "Huiyi real-device last-speaker smoke"
Write-Host "TestIntent: $TestIntent"
Write-Host "Target package: $PackageName"
Write-Host ""

$devices = & $adb devices | Select-Object -Skip 1 | Where-Object { $_ -match "`tdevice$" }
if (-not $devices) {
    Write-Host "No physical Android device detected."
    Write-Host "Expected report result: NOT_TESTED"
    exit 0
}

Write-Host "Before running:"
Write-Host "1. Install the newest APK from outputs or LAN update."
Write-Host "2. Enable Huiyi Accessibility."
Write-Host "3. Enable floating window permission."
Write-Host "4. Open $PackageName on a real chat screen."
Write-Host ""

if ($TestIntent -eq "USER_ASSERTED_LAST_ME") {
    Write-Host "Last ME steps:"
    Write-Host "1. Send one short message yourself."
    Write-Host "2. Wait 1 second."
    Write-Host "3. Tap Huiyi floating bubble -> next sentence."
    Write-Host "4. In Huiyi developer page, tap export last ME acceptance bundle or export GPT review bundle."
    Write-Host "Expected: WAIT, routeCount=0, wait panel shown, no MainActivity."
} else {
    Write-Host "Last OTHER steps:"
    Write-Host "1. Open a chat where the last effective visible message is from the other person."
    Write-Host "2. Wait 1 second."
    Write-Host "3. Tap Huiyi floating bubble -> next sentence."
    Write-Host "4. In Huiyi developer page, tap export last OTHER acceptance bundle or export GPT review bundle."
    Write-Host "Expected: NORMAL_REPLY with 5 routes or CONTEXT_REQUIRED, no MainActivity."
}

Write-Host ""
Write-Host "After export, pull phone review files if needed:"
Write-Host "  .\scripts\pull-latest-phone-gpt-review-bundle.ps1"
