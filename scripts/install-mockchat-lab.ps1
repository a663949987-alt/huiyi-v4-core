param(
    [string]$Scenario = "last_other"
)

$repo = Split-Path $PSScriptRoot -Parent
$adb = Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe"
$huiyiApk = Join-Path $repo "outputs\huiyi-v4.1.3-debug.apk"
$mockApk = Join-Path $repo "outputs\mockchat-debug.apk"

if (-not (Test-Path -LiteralPath $adb)) {
    throw "adb not found: $adb"
}
if (-not (Test-Path -LiteralPath $huiyiApk)) {
    throw "Huiyi APK not found: $huiyiApk"
}
if (-not (Test-Path -LiteralPath $mockApk)) {
    throw "MockChat APK not found: $mockApk"
}

& $adb install -r $huiyiApk
if ($LASTEXITCODE -ne 0) { throw "Install Huiyi failed" }

& $adb install -r $mockApk
if ($LASTEXITCODE -ne 0) { throw "Install MockChat failed" }

& $adb shell am start -n com.huiyi.mockchat/.MainActivity --es scenario $Scenario
if ($LASTEXITCODE -ne 0) { throw "Open MockChat scenario failed" }

Write-Host "Installed Huiyi and MockChatLab."
Write-Host "Opened scenario: $Scenario"
Write-Host "Now enable Huiyi accessibility service and overlay permission on the device."
