# Batch 02 — Embedded Node POC

Goal: prove Node >= Baileys requirement runs inside APK.

Tasks:
- integrate selected `libnode.so`
- run a JS entrypoint
- report Node version to Kotlin through bridge
- verify crypto/TLS/WebSocket primitives needed by Baileys
- verify process shutdown/restart
- measure APK size and RSS
- verify 16 KB page compatibility

Do not continue if Node runtime is below Baileys requirement.
