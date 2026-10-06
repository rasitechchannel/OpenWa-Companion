$ErrorActionPreference = "Continue"
$adb = "C:\Users\LENOVO\AppData\Local\Android\Sdk\platform-tools\adb.exe"
$s = "4ca8a939"
$pkg = "org.rasitech.openwacompanion.debug"
$reportDir = "E:\OpenWA\docs\device-reports"
function A { & $adb -s $s @args }

function Get-StatusField([string]$raw, [string]$key) {
  $pat = [string]::Format('"{0}"\s*:\s*"([^"]*)"', $key)
  $m = [regex]::Match($raw, $pat)
  if ($m.Success) { return $m.Groups[1].Value }
  return ""
}

A shell am start -n "$pkg/org.rasitech.openwacompanion.MainActivity" | Out-Null
Start-Sleep 3
$tmpCheck = Join-Path $env:TEMP "ow-st-check.json"
A shell run-as $pkg cat files/nodejs/bridge/status.json > $tmpCheck 2>$null
$checkRaw = ""
if (Test-Path $tmpCheck) { $checkRaw = Get-Content $tmpCheck -Raw -ErrorAction SilentlyContinue }
if (-not $checkRaw -or $checkRaw -notmatch "connection") {
  A shell uiautomator dump /sdcard/u.xml | Out-Null
  A pull /sdcard/u.xml "$reportDir\u-repoll.xml" | Out-Null
  $xml = Get-Content "$reportDir\u-repoll.xml" -Raw
  $match = [regex]::Match($xml, 'text="Start engine / show QR".*?bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"')
  if (-not $match.Success) {
    $match = [regex]::Match($xml, 'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"[^>]*text="Start engine / show QR"')
  }
  if ($match.Success) {
    $cx = [int](([int]$match.Groups[1].Value + [int]$match.Groups[3].Value) / 2)
    $cy = [int](([int]$match.Groups[2].Value + [int]$match.Groups[4].Value) / 2)
    A shell input tap $cx $cy
    Start-Sleep 10
  }
}

Write-Host "=== LONG POLL 12 min ==="
$deadline = (Get-Date).AddMinutes(12)
$paired = $false
$dq = [char]34
$marker = 'data:image/png;base64,'
while ((Get-Date) -lt $deadline) {
  $tmp = Join-Path $env:TEMP "ow-status-long.json"
  A shell run-as $pkg cat files/nodejs/bridge/status.json > $tmp 2>$null
  if (Test-Path $tmp) {
    $raw = Get-Content $tmp -Raw -ErrorAction SilentlyContinue
    if ($raw) {
      $conn = Get-StatusField $raw "connection"
      $node = Get-StatusField $raw "node"
      Write-Host ("{0:T} connection={1} node={2}" -f (Get-Date), $conn, $node)
      $idx = $raw.IndexOf($marker)
      if ($idx -ge 0) {
        $start = $idx + $marker.Length
        $end = $raw.IndexOf($dq, $start)
        if ($end -gt $start) {
          $b64 = $raw.Substring($start, $end - $start)
          try {
            $bytes = [Convert]::FromBase64String($b64)
            [IO.File]::WriteAllBytes((Join-Path $reportDir "openwa-qr-live.png"), $bytes)
          } catch {}
        }
      }
      if ($conn -eq "open") {
        Copy-Item $tmp (Join-Path $reportDir "status-open.json") -Force
        $paired = $true
        break
      }
    }
  }
  Start-Sleep -Seconds 5
}
Set-Content -Path (Join-Path $reportDir "pair-result.txt") -Value ("paired=$paired time=$(Get-Date -Format o)")
if ($paired) { Write-Host "SUCCESS: paired"; exit 0 } else { Write-Host "TIMEOUT"; exit 4 }
