# UI Visual Comparison — OpenWA vs WhatsApp Android

Generated: 2026-10-06

## Reference capture

WhatsApp Android (linked-device build on Xiaomi 2312DRA50G, dark mode):

| Screen | File |
|---|---|
| Chats home | `docs/ui-refs/whatsapp/01-chats-home.png` |
| Updates | `docs/ui-refs/whatsapp/02-updates.png` |
| Calls | `docs/ui-refs/whatsapp/04-calls.png` |
| Conversation | `docs/ui-refs/whatsapp/05-conversation.png` |
| Settings | `docs/ui-refs/whatsapp/06-settings.png` |

OpenWA screenshots blocked this pass by device keyguard (`mDreamingLockscreen=true`). Re-capture after unlock into `docs/ui-refs/openwa/`.

## Implementation pass (this session)

Applied WhatsApp-aligned structure (not Material sample):

| Area | Change |
|---|---|
| Color tokens | WA dark `#0B141A` / `#1F2C34` / `#005C4B` / `#00A884`; light `#008069` / `#D9FDD3` |
| Typography | 17sp chat titles, 14sp preview, 12sp timestamps |
| Chat list | 49dp avatar, ~72dp rows, unread green pill, time placement |
| Home | Title "OpenWA", search pill, overflow menu, FAB new-chat, bottom tabs with selected pill |
| Conversation | WA bubble colors, composer capsule + circular send/mic, muted call icons (no fake live A/V) |
| Onboarding | Welcome → Agree and continue → Link with QR / phone code (real pair only) |
| Settings | Profile header + WA-style rows; Diagnostics debug-only |
| Branding | OpenWA Companion + unofficial disclaimer; no WA logo assets |

## Remaining gaps to 99% (honest)

1. Device unlock required to screenshot/compare OpenWA side-by-side.
2. Chat wallpaper doodle pattern not shipped (avoid proprietary WA asset).
3. Attachment sheet / media viewer / selection mode / reply swipe not fully built.
4. Status viewer & channel composer gated on real Baileys data.
5. Iconography still Material Outlined set — swap to custom WA-weight icons later without copying Meta assets.
6. Functional pairing still required before conversation density can be validated with real bubbles.

## Status

**UI/UX: PARTIAL** — major structural parity pass landed; not DONE until unlock + visual compare loop closes remaining density/icon/sheet gaps.
