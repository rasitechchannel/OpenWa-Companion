# Skill: Android Native

- Kotlin
- Jetpack Compose
- Coroutines/Flow
- Room
- WorkManager hanya untuk pekerjaan yang cocok
- Foreground service untuk continuous connection bila diperlukan
- Notification channels
- Android Keystore
- scoped storage
- adaptive layouts

## Multi-account
Setiap account:
- unique accountId
- isolated auth state
- isolated session key namespace
- isolated engine connection
- isolated DB ownership metadata
Jangan mencampur credential antar account.

## Battery
Ukur:
- idle connected
- reconnect loop
- history sync
- large media
- foreground/background
Jangan membuat wakelock permanen.
