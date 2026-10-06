$ErrorActionPreference = "Continue"
$adb = "C:\Users\LENOVO\AppData\Local\Android\Sdk\platform-tools\adb.exe"
$s = "4ca8a939"
$pkg = "org.rasitech.openwacompanion.debug"
$reportDir = "E:\OpenWA\docs\device-reports"
$utf8 = New-Object System.Text.UTF8Encoding $false

function Get-JsonString([string]$raw, [string]$key) {
  $pat = '"' + $key + '"\s*:\s*"([^"]*)"'
  $m = [regex]::Match($raw, $pat)
  if ($m.Success) { return $m.Groups[1].Value }
  return ""
}

Write-Host "=== Poll open 20m ==="
Write-Host "PRIMARY: enter pairing code from CURRENT_PAIRING_CODE.txt or scan QR at http://127.0.0.1:8765/qr-scan.html"
$deadline = (Get-Date).AddMinutes(20)
while ((Get-Date) -lt $deadline) {
  $raw = & $adb -s $s exec-out run-as $pkg cat files/nodejs/bridge/status.json 2>$null
  if ($raw) {
    if ($raw -is [array]) { $raw = $raw -join "`n" }
    $raw = [string]$raw
    [IO.File]::WriteAllText((Join-Path $reportDir "status-live.json"), $raw, $utf8)
    $conn = Get-JsonString $raw "connection"
    $pc = Get-JsonString $raw "pairingCode"
    if (-not $pc) {
      $nullPc = [regex]::Match($raw, '"pairingCode"\s*:\s*null')
      if ($nullPc.Success) { $pc = "null" }
    }
    Write-Host ("{0:T} connection={1} pairingCode={2}" -f (Get-Date), $conn, $pc)
    if ($conn -eq "open") {
      Copy-Item (Join-Path $reportDir "status-live.json") (Join-Path $reportDir "status-open.json") -Force
      Write-Host "SUCCESS: connection=open"
      exit 0
    }
  }
  Start-Sleep -Seconds 5
}
Write-Host "TIMEOUT"
exit 4
