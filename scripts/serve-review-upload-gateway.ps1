param(
    [int]$Port = 8791
)

$repo = Split-Path $PSScriptRoot -Parent
$bundledPython = Join-Path $env:USERPROFILE ".cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe"
$python = if (Get-Command python -ErrorAction SilentlyContinue) {
    "python"
} elseif (Test-Path -LiteralPath $bundledPython) {
    $bundledPython
} else {
    "py"
}
$script = Join-Path $PSScriptRoot "review_upload_gateway.py"

Push-Location $repo
try {
    & $python $script --repo $repo --host "0.0.0.0" --port $Port
} finally {
    Pop-Location
}
