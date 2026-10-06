# Skill: Testing

## Mandatory physical-device tests
- fresh install
- QR pairing
- pairing code
- first history sync
- send/receive text
- send/receive each supported message type
- reaction
- edit/delete
- receipts
- presence
- group changes
- join requests
- channel/newsletter updates
- media download/upload
- airplane mode
- network switch
- force stop / reopen
- reboot
- remote logout
- session restore
- multiple accounts
- low storage
- large history
- large group
- 16 KB page-size device/emulator
- Android latest stable

## Calls
Call event tests are separate from media-call tests.
Do not mark voice/video calling pass merely because `call` event arrives.

## Performance
Measure:
- startup time
- Node runtime RSS
- sync peak memory
- DB write throughput
- battery drain
- APK/install size
