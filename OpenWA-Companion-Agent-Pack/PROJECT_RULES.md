# Project Rules

## Product
- App name: OpenWA Companion
- Subtitle: Unofficial open-source companion client
- Tidak berafiliasi dengan WhatsApp LLC atau Meta Platforms.
- Gunakan identitas visual sendiri.

## Architecture rule
Android UI tidak boleh bergantung pada bentuk payload Baileys mentah.
Gunakan:
`Baileys -> Adapter/Normalizer -> Domain Events -> Local Repository -> ViewModel -> Compose UI`

Ini membuat upgrade Baileys tidak memaksa rewrite UI.

## Default build assumptions
- Android native, Kotlin, Jetpack Compose.
- `arm64-v8a` wajib untuk production.
- `x86_64` boleh untuk emulator/debug.
- Minimum Android default: API 26 kecuali hasil compatibility test membenarkan target berbeda.
- targetSdk/compileSdk: latest stable yang tersedia saat build.
- NDK: versi yang memenuhi requirement 16 KB page alignment.
- Multi-account architecture disiapkan dari awal; implementasi account switching harus memakai session storage terpisah.
- Light + dark theme, tetapi light theme adalah default.
- Phone, tablet, landscape, foldable responsive layout.
- Bahasa awal: English + Bahasa Indonesia.
- No external analytics by default.
- Crash reporting hanya opt-in dan tidak boleh membawa message/contact/auth data.

## UI identity
- Familiar seperti WhatsApp terbaru, tetapi jangan menyalin branding proprietary.
- White/light clean theme, typography profesional, border/shadow minimal.
- Tidak memakai dark SaaS/glassmorphism.
- Tidak memakai gradient biru-ungu.
- Tidak membuat card berlebihan.

## Release behavior
Semua fitur yang belum benar-benar didukung upstream harus:
- disembunyikan, atau
- ditampilkan dengan label `Not supported by current engine`.
Jangan pernah mensimulasikan keberhasilan.
