# Current Snapshot — 2026-10-06

This file is context, not a permanent source of truth. Refresh before implementation.

## WhatsApp Android
Official Android download page currently advertises version 2.26.32.84 and Android 6.0+.
Official 2026 updates include multi-account improvements, group history sharing, richer group polls/@all, call transfer/waiting room, and newer account-security features.
WhatsApp Web officially gained voice/video calling in 2026, but this does NOT mean Baileys automatically exposes the media call stack.

## Baileys
Current documentation states Node.js 20+ is required.
Current source `BaileysEventMap` includes more than basic message/chat events; see `skills/baileys/events.md`.
Recent release stream includes v6.7.24 and v7.0.0 release candidates. Agent must select/pin after compatibility testing.

## Embedded Node
The traditional nodejs-mobile release stream may lag current Node requirements.
Audit a maintained Node 20+/24 Android build with 16 KB page-size support; verify source, license and binary provenance.
