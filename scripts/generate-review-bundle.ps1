param(
    [string]$TaskName = "Review Freshness + Real Device Smoke Test",
    [string[]]$CurrentReport = @()
)

$ErrorActionPreference = "Stop"
$repo = (Resolve-Path ".").Path
$pythonCandidates = @(
    (Join-Path $env:USERPROFILE ".cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe"),
    "python.exe",
    "python"
)

$python = $null
foreach ($candidate in $pythonCandidates) {
    try {
        $cmd = Get-Command $candidate -ErrorAction Stop
        $python = $cmd.Source
        break
    } catch {
        if (Test-Path $candidate) {
            $python = $candidate
            break
        }
    }
}

if (-not $python) {
    throw "Python runtime not found. Codex bundled Python should exist under .cache\codex-runtimes."
}

$argsList = @((Join-Path $repo "scripts\generate_review_bundle.py"), "--task-name", $TaskName)
foreach ($report in $CurrentReport) {
    $argsList += "--current-report"
    $argsList += $report
}

& $python @argsList
