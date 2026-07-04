param(
    [int]$Port = 8787,
    [string]$Root = ""
)

if ([string]::IsNullOrWhiteSpace($Root)) {
    $Root = Join-Path (Split-Path $PSScriptRoot -Parent) "outputs\update_server"
}

$Root = [System.IO.Path]::GetFullPath($Root)
if (-not (Test-Path -LiteralPath $Root)) {
    throw "Update server root not found: $Root"
}

function Write-Response($stream, [int]$status, [string]$contentType, [byte[]]$body) {
    $statusText = if ($status -eq 200) { "OK" } elseif ($status -eq 403) { "Forbidden" } else { "Not Found" }
    $header = "HTTP/1.1 $status $statusText`r`nContent-Type: $contentType`r`nContent-Length: $($body.Length)`r`nConnection: close`r`nCache-Control: no-store, no-cache, must-revalidate`r`nPragma: no-cache`r`nAccess-Control-Allow-Origin: *`r`n`r`n"
    $headerBytes = [System.Text.Encoding]::ASCII.GetBytes($header)
    $stream.Write($headerBytes, 0, $headerBytes.Length)
    $stream.Write($body, 0, $body.Length)
}

$listener = [System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Any, $Port)
$listener.Start()
Write-Host "Huiyi LAN update server"
Write-Host "Root: $Root"
Write-Host "Port: $Port"
Write-Host "Phone URL: http://<电脑局域网IP>:$Port/latest.json"
Write-Host "Press Ctrl+C to stop."

try {
    while ($true) {
        $client = $listener.AcceptTcpClient()
        try {
            $stream = $client.GetStream()
            $reader = [System.IO.StreamReader]::new($stream)
            $requestLine = $reader.ReadLine()
            if ([string]::IsNullOrWhiteSpace($requestLine)) {
                $client.Close()
                continue
            }
            $parts = $requestLine.Split(" ")
            $path = if ($parts.Length -ge 2) { $parts[1] } else { "/" }
            while (-not [string]::IsNullOrEmpty($reader.ReadLine())) {}

            $relative = [Uri]::UnescapeDataString($path.TrimStart("/"))
            if ([string]::IsNullOrWhiteSpace($relative)) { $relative = "latest.json" }
            $candidate = [System.IO.Path]::GetFullPath((Join-Path $Root $relative))
            if (-not $candidate.StartsWith($Root, [System.StringComparison]::OrdinalIgnoreCase)) {
                $body = [System.Text.Encoding]::UTF8.GetBytes("Forbidden")
                Write-Response $stream 403 "text/plain; charset=utf-8" $body
                continue
            }
            if (-not (Test-Path -LiteralPath $candidate -PathType Leaf)) {
                $body = [System.Text.Encoding]::UTF8.GetBytes("Not found")
                Write-Response $stream 404 "text/plain; charset=utf-8" $body
                continue
            }

            $bytes = [System.IO.File]::ReadAllBytes($candidate)
            $ext = [System.IO.Path]::GetExtension($candidate).ToLowerInvariant()
            $mime = switch ($ext) {
                ".json" { "application/json; charset=utf-8" }
                ".apk" { "application/vnd.android.package-archive" }
                default { "application/octet-stream" }
            }
            Write-Response $stream 200 $mime $bytes
        } finally {
            $client.Close()
        }
    }
} finally {
    $listener.Stop()
}
