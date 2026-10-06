# Skill: Kotlin ↔ Node Bridge

Preferred: JNI/native bridge dengan command/event protocol versioned.

## Requirements
- bounded queue
- thread-safe
- backpressure
- JSON/CBOR/typed serialization yang jelas
- large media tidak boleh melewati bridge sebagai giant Base64 blob
- file descriptor/path app-private lebih disukai untuk payload besar
- all errors mapped into typed error codes
- redact secrets from logs

## Commands baseline
- ENGINE_START
- ENGINE_STOP
- ACCOUNT_CREATE
- ACCOUNT_SWITCH
- PAIR_QR_START
- PAIR_CODE_REQUEST
- LOGOUT
- SEND_MESSAGE
- MESSAGE_ACTION
- CHAT_ACTION
- GROUP_ACTION
- NEWSLETTER_ACTION
- PRESENCE_ACTION
- DOWNLOAD_MEDIA
- FETCH_HISTORY
- PRIVACY_ACTION
- SETTINGS_ACTION

Agent wajib generate command list dari actual socket capabilities.
