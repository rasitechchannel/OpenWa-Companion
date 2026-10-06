# Skill: Security & Privacy

## Credential storage
- Auth credentials/key material must never remain as casual plaintext export files in shared storage.
- Protect wrapping keys with Android Keystore.
- Use app-private storage.
- Disable/limit Android backup for sensitive credential paths.
- Redact secrets from logs/crash reports.

## Privacy defaults
- no external backend
- no chat analytics
- no third-party AI
- no upload of media/message/contact/session
- no advertising SDK by default
- no remote debugging in release

## App lock
Support optional biometric/device credential lock.

## Logs
Release logs must not contain:
- full phone numbers when avoidable
- message text
- auth keys
- media URLs/keys
- pairing secrets

## Threat checks
Test:
- rooted device disclosure (do not block by default)
- backup restore edge cases
- session file theft resistance
- intent/exported component exposure
- WebView absent unless strictly needed
- network security config
