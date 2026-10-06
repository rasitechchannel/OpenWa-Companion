# Feature Scope

Implementasikan semua capability yang tersedia secara nyata dari versi Baileys yang dipin.

## Core
- QR pairing
- pairing code bila upstream mendukung
- reconnect
- logout/unlink
- multi-account/session separation
- full/partial history sync
- chat list
- chat details
- contacts
- groups
- status/updates sejauh upstream mendukung
- newsletters/channels sejauh upstream mendukung
- privacy/settings state sejauh upstream mendukung
- blocklist
- labels
- presence
- receipts
- reactions
- polls
- disappearing/ephemeral settings
- media upload/download
- message search lokal
- archive/pin/mute/star jika upstream/data model mendukung
- chat lock state jika tersedia
- LID mapping jika tersedia

## Message types
Agent wajib mengaudit message schema/proto dari Baileys version yang dipin. Jangan hanya mengimplementasikan daftar ini:
- text
- extended text
- image
- video
- audio
- voice note
- document
- sticker
- GIF/video GIF semantics
- contact
- location/live location bila exposed
- reaction
- poll
- reply/quoted
- mention
- edit
- delete/revoke
- ephemeral/disappearing
- view-once jika upstream mengizinkan handling
- protocol/system message yang relevan
- group/admin system events
- newsletter/channel messages

## Calls
- Render call history/event bila Baileys mengeksposnya.
- Incoming call event, type, status, participant metadata: implement bila exposed.
- Reject/terminate only if real upstream capability exists.
- Voice/video media transport: **jangan fake**.
- Jika versi/fork yang diaudit menyediakan full media call yang legal, stable, open-source dan kompatibel, buat provider terpisah dan test nyata.
