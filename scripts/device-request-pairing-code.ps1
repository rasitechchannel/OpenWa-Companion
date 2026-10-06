$ErrorActionPreference = "Continue"
$adb = "C:\Users\LENOVO\AppData\Local\Android\Sdk\platform-tools\adb.exe"
$s = "4ca8a939"
$pkg = "org.rasitech.openwacompanion.debug"
$phone = "6285128006442"
$localCmd = Join-Path $env:TEMP "openwa-command.json"
$payload = '{"type":"request-pairing-code","phone":"' + $phone + '"}'
Set-Content -Path $localCmd -Value $payload -NoNewline -Encoding ascii
& $adb -s $s push $localCmd /data/local/tmp/openwa-command.json
# copy into app sandbox
& $adb -s $s shell "run-as $pkg cp /data/local/tmp/openwa-command.json files/nodejs/bridge/command.json"
# fallback: cat via run-as
& $adb -s $s shell "cat /data/local/tmp/openwa-command.json | run-as $pkg tee files/nodejs/bridge/command.json > /dev/null"
Start-Sleep 10
& $adb -s $s shell "run-as $pkg cat files/nodejs/bridge/command.json" 2>&1
Write-Host "==== STATUS ===="
& $adb -s $s shell "run-as $pkg cat files/nodejs/bridge/status.json" 2>$null | Select-String -Pattern "connection|pairingCode|lastError|node"
Write-Host "==== EVENTS ===="
& $adb -s $s shell "run-as $pkg tail -c 2000 files/nodejs/bridge/events.ndjson" 2>$null
