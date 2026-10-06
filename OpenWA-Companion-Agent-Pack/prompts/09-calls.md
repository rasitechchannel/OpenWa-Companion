# Batch 09 — Calls

Implement call-event/history UI from real Baileys events.

Important:
- incoming/outgoing/status data: implement if exposed
- reject/accept/terminate: only if actual tested method exists
- live voice/video media: mark unsupported unless a stable audited open-source provider proves real media transport
- no fake ringing/success state

Create a `CallProvider` interface so future media engine can plug in without rewriting UI.
