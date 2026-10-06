$ErrorActionPreference = "Continue"
$adb = "C:\Users\LENOVO\AppData\Local\Android\Sdk\platform-tools\adb.exe"
$s = "4ca8a939"
$pkg = "org.rasitech.openwacompanion.debug"
$reportDir = "E:\OpenWA\docs\device-reports"
$out = Join-Path $reportDir "e2e-results.txt"
$utf8 = New-Object System.Text.UTF8Encoding $false
$results = New-Object System.Collections.Generic.List[string]
function A { & $adb -s $s @args }
function L([string]$t) { $results.Add($t); Write-Host $t }

function Sync-Status {
  $raw = & $adb -s $s exec-out run-as $pkg cat files/nodejs/bridge/status.json 2>$null
  if (-not $raw) { return "" }
  if ($raw -is [array]) { $raw = $raw -join "`n" }
  $raw = [string]$raw
  [IO.File]::WriteAllText((Join-Path $reportDir "status-live.json"), $raw, $utf8)
  return $raw
}
function Field([string]$raw, [string]$key) {
  $m = [regex]::Match($raw, [string]::Format('"{0}"\s*:\s*"([^"]*)"', $key))
  if ($m.Success) { return $m.Groups[1].Value }
  return ""
}
function Write-Cmd([string]$json) {
  $p = Join-Path $env:TEMP "openwa-command.json"
  [IO.File]::WriteAllText($p, $json)
  A push $p /data/local/tmp/openwa-command.json | Out-Null
  A shell "cat /data/local/tmp/openwa-command.json | run-as $pkg tee files/nodejs/bridge/command.json > /dev/null"
}
function Db-Query([string]$sql) {
  $tmpDb = "/data/local/tmp/openwa-e2e.db"
  A shell "run-as $pkg cp databases/openwa.db $tmpDb 2>/dev/null; run-as $pkg cp databases/openwa.db $tmpDb"
  # Prefer extracting via run-as cat to host
  $local = Join-Path $env:TEMP "openwa-e2e.db"
  A exec-out run-as $pkg cat databases/openwa.db > $local 2>$null
  if (-not (Test-Path $local) -or (Get-Item $local).Length -lt 100) {
    return "DB_UNAVAILABLE"
  }
  $sqlite = @(
    "C:\Users\LENOVO\AppData\Local\Android\Sdk\platform-tools\sqlite3.exe",
    "sqlite3"
  ) | Where-Object { $_ -eq "sqlite3" -or (Test-Path $_) } | Select-Object -First 1
  if (-not $sqlite) {
    # fallback: use adb shell sqlite3 if present
    return (A shell "run-as $pkg sh -c `"sqlite3 databases/openwa.db \`"$sql\`"`" 2>$null)
  }
  return (& $sqlite $local $sql 2>$null)
}
function Count-Events([string]$type) {
  $raw = A shell "run-as $pkg sh -c `"grep -c eventType.:.$type files/nodejs/bridge/events.ndjson 2>/dev/null || true`""
  return ($raw | Out-String).Trim()
}
function Wait-Conn([string]$want, [int]$sec = 60) {
  $deadline = (Get-Date).AddSeconds($sec)
  while ((Get-Date) -lt $deadline) {
    $raw = Sync-Status
    if ((Field $raw "connection") -eq $want) { return $raw }
    Start-Sleep 3
  }
  return (Sync-Status)
}

L "=== OpenWA E2E after pairing ==="
L ("time=" + (Get-Date -Format o))
$raw = Sync-Status
$conn = Field $raw "connection"
$me = Field $raw "me"
L "connection=$conn me=$me node=$(Field $raw 'node')"
if ($conn -ne "open") {
  L "ABORT: not paired (connection=$conn)"
  $results | Set-Content $out
  exit 3
}
L "PAIRING=DONE"

# --- Session persistence ---
A shell am force-stop $pkg
Start-Sleep 2
A shell am start -n "$pkg/org.rasitech.openwacompanion.MainActivity" | Out-Null
Start-Sleep 8
# Engine may need FGS restart - tap start if shown, else wait for auto
$raw = Wait-Conn "open" 90
L ("after_force_stop_reopen=" + (Field $raw "connection"))

# Kill process
$appPid = (A shell pidof $pkg).Trim()
if ($appPid) { A shell "kill -9 $appPid" 2>$null; Start-Sleep 2 }
A shell am start -n "$pkg/org.rasitech.openwacompanion.MainActivity" | Out-Null
Start-Sleep 10
$raw = Wait-Conn "open" 90
L ("after_kill_reopen=" + (Field $raw "connection"))

# Restart via logout? No - restart engine only by force-stop node path: clear not needed
# Soft: toggle airplane
A shell settings put global airplane_mode_on 1 2>$null
A shell am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true 2>$null | Out-Null
Start-Sleep 5
A shell settings put global airplane_mode_on 0 2>$null
A shell am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false 2>$null | Out-Null
Start-Sleep 15
$raw = Wait-Conn "open" 120
L ("after_airplane=" + (Field $raw "connection"))

# Background / screen
A shell input keyevent 3
Start-Sleep 8
A shell am start -n "$pkg/org.rasitech.openwacompanion.MainActivity" | Out-Null
Start-Sleep 5
$raw = Sync-Status
L ("after_background=" + (Field $raw "connection"))

# Reboot (optional long) - mark and do if env OPENWA_REBOOT=1
if ($env:OPENWA_REBOOT -eq "1") {
  L "rebooting device..."
  A reboot
  A wait-for-device
  Start-Sleep 40
  A shell am start -n "$pkg/org.rasitech.openwacompanion.MainActivity" | Out-Null
  Start-Sleep 15
  $raw = Wait-Conn "open" 180
  L ("after_reboot=" + (Field $raw "connection"))
} else {
  L "after_reboot=SKIPPED (set OPENWA_REBOOT=1 to enable)"
}

# --- History / Room ---
Start-Sleep 20
$chatCount = Db-Query "SELECT COUNT(*) FROM chats;"
$msgCount = Db-Query "SELECT COUNT(*) FROM messages;"
$contactCount = Db-Query "SELECT COUNT(*) FROM contacts;"
$groupCount = Db-Query "SELECT COUNT(*) FROM groups;"
L "room_chats=$chatCount"
L "room_messages=$msgCount"
L "room_contacts=$contactCount"
L "room_groups=$groupCount"
L ("events_history=" + (Count-Events "history.set"))
L ("events_chats_upsert=" + (Count-Events "chats.upsert"))
L ("events_messages_upsert=" + (Count-Events "messages.upsert"))

# Dedup: duplicate message ids
$dup = Db-Query "SELECT messageId, COUNT(*) c FROM messages GROUP BY accountId, messageId, chatId HAVING c>1 LIMIT 5;"
L "duplicate_messages=$dup"

# --- Messaging ---
# Pick a 1:1 chat jid if available
$jid = Db-Query "SELECT id FROM chats WHERE id LIKE '%@s.whatsapp.net' ORDER BY lastMessageAt DESC LIMIT 1;"
$jid = ($jid | Out-String).Trim()
if (-not $jid -or $jid -eq "DB_UNAVAILABLE") {
  # try from events
  $jid = ""
}
L "test_jid=$jid"
$probe = "OpenWA-E2E " + (Get-Date -Format "HHmmss")
if ($jid) {
  Write-Cmd ('{"type":"send-text","jid":"' + $jid + '","text":"' + $probe + '"}')
  Start-Sleep 8
  $sent = Db-Query ("SELECT COUNT(*) FROM messages WHERE text LIKE '%" + $probe + "%';")
  L "send_text_count=$sent"
} else {
  L "send_text=SKIPPED_NO_JID"
}

# Reactions / receipts event counts
L ("events_reaction=" + (Count-Events "messages.reaction"))
L ("events_receipt=" + (Count-Events "message-receipt.update"))
L ("events_presence=" + (Count-Events "presence.update"))
L ("events_call=" + (Count-Events "call"))
L ("events_groups=" + (Count-Events "groups.upsert"))
L ("events_group_participants=" + (Count-Events "group-participants.update"))

# Media
$mediaCount = Db-Query "SELECT COUNT(*) FROM media;"
$mediaPaths = Db-Query "SELECT localPath FROM media WHERE localPath IS NOT NULL LIMIT 5;"
L "media_rows=$mediaCount"
L "media_paths=$mediaPaths"
# Ensure no giant base64 in recent events (sample size)
$giant = A shell "run-as $pkg sh -c `"grep -c data:image/png;base64 files/nodejs/bridge/events.ndjson 2>/dev/null || echo 0`""
L "events_inline_base64_hits=$giant"

# Notifications channels still present
$n = A shell dumpsys notification | Select-String -Pattern "openwa_privacy|openwa_sync" | Measure-Object
L "notification_channels_hits=$($n.Count)"

# Sensitive logs
$sens = A logcat -d -t 500 | Select-String -Pattern "noiseKey|signedIdentityKey|advSecretKey|privateKey"
L "sensitive_log_hits=$($sens.Count)"

# Memory
$appPid = (A shell pidof $pkg).Trim()
if ($appPid) {
  $rss = (A shell "awk '/VmRSS:/{print `$2}' /proc/$appPid/status").Trim()
  L "vm_rss_kb=$rss"
}
$mem = A shell dumpsys meminfo $pkg | Select-String "TOTAL PSS|TOTAL RSS" | Select-Object -First 2
$mem | ForEach-Object { L ("mem: " + $_.Line.Trim()) }

$results | Set-Content $out -Encoding utf8
Write-Host "Wrote $out"
# Exit 0 if still open
$raw = Sync-Status
if ((Field $raw "connection") -eq "open") { exit 0 } else { exit 4 }
