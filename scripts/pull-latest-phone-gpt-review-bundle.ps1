param(
    [string]$PackageName = "com.huiyi.v4"
)

$ErrorActionPreference = "Stop"
$repo = (Resolve-Path ".").Path
$outDir = Join-Path $repo "outputs\from_phone"
$unpacked = Join-Path $outDir "unpacked"
New-Item -ItemType Directory -Force -Path $outDir, $unpacked | Out-Null

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

function Pull-FromDownloads {
    $listing = & $adb shell "ls -t /sdcard/Download/Huiyi/huiyi-phone-gpt-review-v*.zip 2>/dev/null | head -n 1"
    $remote = ($listing | Select-Object -First 1).Trim()
    if ([string]::IsNullOrWhiteSpace($remote)) { return $null }
    $dest = Join-Path $outDir "huiyi-phone-gpt-review-latest.zip"
    & $adb pull $remote $dest | Out-Host
    if (Test-Path $dest) { return $dest }
    return $null
}

function Pull-FromRunAs {
    $listing = & $adb shell "run-as $PackageName sh -c 'ls -t files/exports/gpt_review_bundles/huiyi-phone-gpt-review-v*.zip 2>/dev/null | head -n 1'"
    $remote = ($listing | Select-Object -First 1).Trim()
    if ([string]::IsNullOrWhiteSpace($remote)) { return $null }
    $dest = Join-Path $outDir "huiyi-phone-gpt-review-latest.zip"
    & $adb exec-out "run-as $PackageName cat $remote" | Set-Content -LiteralPath $dest -Encoding Byte
    if (Test-Path $dest -PathType Leaf) { return $dest }
    return $null
}

$zip = Pull-FromDownloads
if (-not $zip) { $zip = Pull-FromRunAs }

if (-not $zip) {
    throw "No phone GPT review bundle found. Tap 导出 GPT 验收总包 on the phone first. No PASS is inferred."
}

if (Test-Path $unpacked) { Remove-Item -LiteralPath $unpacked -Recurse -Force }
New-Item -ItemType Directory -Force -Path $unpacked | Out-Null
Expand-Archive -LiteralPath $zip -DestinationPath $unpacked -Force

Write-Host "phoneBundle=$zip"
Write-Host "unpacked=$unpacked"
