param(
    [string]$ReleaseNotes = "Huiyi LAN test update package",
    [switch]$SkipBuild
)

$repo = Split-Path $PSScriptRoot -Parent
$appGradle = Join-Path $repo "app\build.gradle.kts"
$gradlew = Join-Path $repo "gradlew.bat"
$updateDir = Join-Path $repo "outputs\update_server"
$outputsDir = Join-Path $repo "outputs"

$gradleText = Get-Content -LiteralPath $appGradle -Raw
$versionName = [regex]::Match($gradleText, 'versionName\s*=\s*"([^"]+)"').Groups[1].Value
$versionCode = [regex]::Match($gradleText, 'versionCode\s*=\s*(\d+)').Groups[1].Value
if ([string]::IsNullOrWhiteSpace($versionName) -or [string]::IsNullOrWhiteSpace($versionCode)) {
    throw "Cannot read versionName/versionCode from app/build.gradle.kts"
}

if (-not $SkipBuild) {
    Push-Location $repo
    try {
        $env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"
        $env:Path = "$env:JAVA_HOME\bin;" + [System.Environment]::GetEnvironmentVariable("Path", "Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path", "User")
        & $gradlew assembleDebug --no-daemon
        if ($LASTEXITCODE -ne 0) { throw "assembleDebug failed" }
    } finally {
        Pop-Location
    }
}

New-Item -ItemType Directory -Force -Path $updateDir | Out-Null
New-Item -ItemType Directory -Force -Path $outputsDir | Out-Null

$sourceApk = Join-Path $repo "app\build\outputs\apk\debug\app-debug.apk"
if (-not (Test-Path -LiteralPath $sourceApk)) {
    throw "Debug APK not found: $sourceApk"
}

$apkName = "huiyi-v$versionName-debug.apk"
$updateApk = Join-Path $updateDir $apkName
$outputApk = Join-Path $outputsDir $apkName
Copy-Item -LiteralPath $sourceApk -Destination $updateApk -Force
Copy-Item -LiteralPath $sourceApk -Destination $outputApk -Force

$sha = (Get-FileHash -LiteralPath $updateApk -Algorithm SHA256).Hash
$createdAt = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()

$manifest = [ordered]@{
    versionName = $versionName
    versionCode = [int]$versionCode
    apkUrl = $apkName
    releaseNotes = $ReleaseNotes
    forceUpdate = $false
    sha256 = $sha
    createdAt = $createdAt
}

$json = $manifest | ConvertTo-Json -Depth 4
$latestPath = Join-Path $updateDir "latest.json"
Set-Content -LiteralPath $latestPath -Value $json -Encoding UTF8

Write-Host "LAN update package published"
Write-Host "Version: $versionName ($versionCode)"
Write-Host "APK: $updateApk"
Write-Host "latest.json: $latestPath"
Write-Host "SHA-256: $sha"
