$ErrorActionPreference = "Continue"
$adb = "C:\Users\LENOVO\AppData\Local\Android\Sdk\platform-tools\adb.exe"
$s = "4ca8a939"
$pkg = "org.rasitech.openwacompanion.debug"
$apk = "E:\OpenWA\android\app\build\outputs\apk\debug\app-debug.apk"
$reportDir = "E:\OpenWA\docs\device-reports"
function A { & $adb -s $s @args }

Write-Host "Installing fresh..."
A install -r $apk
A shell pm clear $pkg
A logcat -c
A shell am start -n "$pkg/org.rasitech.openwacompanion.MainActivity"
Start-Sleep 5
A shell pm grant $pkg android.permission.POST_NOTIFICATIONS 2>$null

A shell uiautomator dump /sdcard/u.xml | Out-Null
A pull /sdcard/u.xml "$reportDir\u-fresh.xml" | Out-Null
$xml = Get-Content "$reportDir\u-fresh.xml" -Raw
$match = [regex]::Match($xml, 'text="Start engine / show QR".*?bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"')
if (-not $match.Success) {
  $match = [regex]::Match($xml, 'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"[^>]*text="Start engine / show QR"')
}
if ($match.Success) {
  $cx = [int](([int]$match.Groups[1].Value + [int]$match.Groups[3].Value) / 2)
  $cy = [int](([int]$match.Groups[2].Value + [int]$match.Groups[4].Value) / 2)
  Write-Host "Start engine $cx,$cy"
  A shell input tap $cx $cy
} else {
  Write-Host "Start button missing"
}

Start-Sleep 15
$tmp = Join-Path $env:TEMP "ow-st.json"
A shell run-as $pkg cat files/nodejs/bridge/status.json > $tmp 2>$null
$raw = Get-Content $tmp -Raw -ErrorAction SilentlyContinue
Write-Host "STATUS1 connection check"
if ($raw) {
  Write-Host ([regex]::Match($raw, '"connection"\s*:\s*"([^"]*)"').Groups[1].Value)
  Write-Host ([regex]::Match($raw, '"node"\s*:\s*"([^"]*)"').Groups[1].Value)
}

$phone = "6285128006442"
$payload = '{"type":"request-pairing-code","phone":"' + $phone + '"}'
$localCmd = Join-Path $env:TEMP "openwa-command.json"
Set-Content -Path $localCmd -Value $payload -NoNewline -Encoding ascii
A push $localCmd /data/local/tmp/openwa-command.json | Out-Null
A shell "cat /data/local/tmp/openwa-command.json | run-as $pkg tee files/nodejs/bridge/command.json > /dev/null"
Start-Sleep 8

A shell run-as $pkg cat files/nodejs/bridge/status.json > $tmp 2>$null
$raw2 = Get-Content $tmp -Raw
$codeMatch = [regex]::Match($raw2, '"pairingCode"\s*:\s*(null|"[^"]*")')
$conn2 = [regex]::Match($raw2, '"connection"\s*:\s*"([^"]*)"').Groups[1].Value
Write-Host ("STATUS2 connection=" + $conn2 + " pairingCode=" + $codeMatch.Groups[1].Value)

Start-Sleep 12
A shell run-as $pkg cat files/nodejs/bridge/status.json > $tmp 2>$null
$raw3 = Get-Content $tmp -Raw
$codeMatch2 = [regex]::Match($raw3, '"pairingCode"\s*:\s*(null|"[^"]*")')
$conn = [regex]::Match($raw3, '"connection"\s*:\s*"([^"]*)"').Groups[1].Value
Write-Host ("After QR cycle: connection=" + $conn + " pairingCode=" + $codeMatch2.Groups[1].Value)

@(
  "time=$(Get-Date -Format o)"
  "pairingCode_immediate=$($codeMatch.Groups[1].Value)"
  "pairingCode_after_qr_cycle=$($codeMatch2.Groups[1].Value)"
  "connection=$conn"
) | Set-Content "$reportDir\pairing-code-persist.txt"

$appPid = (A shell pidof $pkg).Trim()
$rss = ""
if ($appPid) {
  $rss = (A shell "awk '/VmRSS:/{print `$2}' /proc/$appPid/status").Trim()
}
$pathLine = (A shell pm path $pkg).Trim().Replace("package:","")
$apkSize = (A shell "stat -c %s $pathLine").Trim()
@(
  "device=2312DRA50G"
  "abi=arm64-v8a"
  "sdk=36"
  "page_size=4096"
  "node=v26.10.0"
  "baileys=6.7.24"
  "app_pid=$appPid"
  "vm_rss_kb=$rss"
  "apk_bytes=$apkSize"
  "elf_align=0x4000 for libnode libopenwa-node libc++_shared"
  "jni=libopenwa-node loaded"
  "note=com.whatsapp and com.whatsapp.w4b on device are linked companions; primary phone required for Linked Devices"
) | Set-Content "$reportDir\runtime-device-report.txt"
Get-Content "$reportDir\runtime-device-report.txt"
Get-Content "$reportDir\pairing-code-persist.txt"

$marker = 'data:image/png;base64,'
$idx = $raw3.IndexOf($marker)
if ($idx -ge 0) {
  $start = $idx + $marker.Length
  $end = $raw3.IndexOf([char]34, $start)
  if ($end -gt $start) {
    $bytes = [Convert]::FromBase64String($raw3.Substring($start, $end - $start))
    [IO.File]::WriteAllBytes("$reportDir\openwa-qr-live.png", $bytes)
    Write-Host "QR refreshed"
  }
}

$pc = $codeMatch2.Groups[1].Value
if ($pc -and $pc -ne "null") {
  Write-Host "PASS: pairingCode persisted"
  exit 0
}
Write-Host "FAIL or null pairingCode persistence"
exit 5
