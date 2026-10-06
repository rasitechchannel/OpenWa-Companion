# Architecture

## Runtime
APK
- Android UI: Kotlin + Jetpack Compose
- App process / service layer
- JNI/native bridge
- Embedded Node.js runtime (`libnode.so`)
- Baileys
- Room/SQLite
- Android Keystore
- Foreground sync service
- Android notification integration

## Data flow
WhatsApp network
↕
Baileys
↕
Baileys Adapter
↕
Typed Event Bus / Command Bus
↕
Native Bridge
↕
Repository
↕
Room + Media Cache
↕
ViewModel
↕
Compose UI

## No external developer server
Tidak ada:
- VPS relay
- REST backend milik developer
- remote session storage
- cloud message database

## Process lifecycle
- Engine start idempotent.
- Session unlock dilakukan saat aplikasi/service membutuhkannya.
- Reconnect memakai exponential backoff + jitter.
- Logged-out state tidak melakukan reconnect loop.
- Foreground service dipakai jika diperlukan untuk ongoing sync sesuai policy Android.
- State engine dapat direcover setelah process death.

## Bridge contract
Semua command harus memiliki:
- requestId
- accountId
- commandType
- payload version
- success/error response
- timeout
- cancellation jika relevan

Semua event harus memiliki:
- eventId
- accountId
- eventType
- timestamp
- schemaVersion
- normalizedPayload
- optional raw metadata for diagnostics (redacted)

Tidak boleh mengekspos auth keys ke Compose layer.
