# Skill: Embedded Node.js

Baileys saat snapshot ini memerlukan Node.js >= 20. Jangan memilih runtime mobile lama hanya karena integrasinya mudah.

## Selection gate
Runtime wajib:
- Node >= requirement Baileys pinned
- Android arm64-v8a
- 16 KB page-size compatible
- TLS/crypto/WebSocket behavior compatible
- npm dependency loading compatible
- lifecycle start/stop/restart teruji
- license acceptable
- reproducible source/build path tersedia

## Candidate strategy
Audit maintained Node-mobile builds/forks yang menyediakan Node 20+ / 24 dan 16 KB alignment. Jangan trust binary tanpa source/build provenance.

## Packaging
- `libnode.so` untuk ABI production
- JS bundle/dependencies dipaketkan sebagai app-private resource
- Extract/copy ke internal files dir jika runtime memerlukan writable module tree
- Verify checksum/version on startup
- Tidak download runtime executable dari internet

## Native addons
Hindari native npm addons bila pure-JS/Web APIs cukup. Jika perlu native addon:
- build khusus mobile ABI
- N-API compatible
- 16 KB aligned
- license audit
