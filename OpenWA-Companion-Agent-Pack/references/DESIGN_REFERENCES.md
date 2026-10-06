# Design References — OpenWA Companion

Refreshed 2026-10-06. References only; no proprietary WhatsApp assets.

## Product identity
- Brand: **OpenWA Companion** / Dual Chat Link motif (pack `assets/`)
- Tokens: `OpenWA-Companion-Agent-Pack/assets/design_tokens.json`
- Accent `#176B5B`, light default, minimal borders, no glassmorphism, no purple gradients

## UI structure references (license-compatible)
1. [GetStream/whatsApp-clone-compose](https://github.com/GetStream/whatsApp-clone-compose) — Apache-2.0, Compose patterns
2. [mitchelkenn00/WhatsApp-UI-clone](https://github.com/mitchelkenn00/WhatsApp-UI-clone) — MIT, Compose screens
3. [yuvakrishnayk/whatsapp_ui](https://github.com/yuvakrishnayk/whatsapp_ui) — MIT, Flutter visual reference only

## Rules
- Do not copy WhatsApp/Meta trademarks or logo assets
- Do not pull Stream backend/video SDKs into production merely because a clone sample uses them
- Re-audit official WhatsApp Android UI before release polish (batch 06+)
- Every control must map to real capability state: `implemented` | `captured-only` | `unsupported-upstream` | `not-applicable`
