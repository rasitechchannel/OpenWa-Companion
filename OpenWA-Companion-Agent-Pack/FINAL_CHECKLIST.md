# Final Checklist

## Runtime
- [ ] Embedded Node meets pinned Baileys requirement.
- [ ] arm64-v8a works.
- [ ] 16 KB page-size compatibility verified.
- [ ] APK contains no downloaded runtime executable dependency.

## Real connectivity
- [ ] QR pairing real.
- [ ] Pairing code real where supported.
- [ ] Session persists.
- [ ] Send/receive real messages.
- [ ] History sync real.
- [ ] Reconnect real.
- [ ] Logout/revocation handled.

## Coverage
- [ ] No TODO in BAILEYS_COVERAGE_MATRIX.
- [ ] Exported socket methods audited.
- [ ] Message/proto types audited.
- [ ] Unknown/new event path exists.

## UI
- [ ] UI refreshed against latest WhatsApp Android before release.
- [ ] OpenWA branding used.
- [ ] No WhatsApp proprietary logo/assets.
- [ ] No fake buttons.
- [ ] Multi-account works.
- [ ] Phone/tablet/foldable checked.
- [ ] Accessibility checked.

## Calls
- [ ] Call event support truthfully represented.
- [ ] Live call marked unsupported unless real media provider passed tests.
- [ ] No fake voice/video call success state.

## Privacy/security
- [ ] No external message backend.
- [ ] Credentials protected by Keystore-based design.
- [ ] Sensitive backup excluded.
- [ ] Release logs redacted.
- [ ] No sensitive analytics.
- [ ] App lock tested.

## Open source
- [ ] THIRD_PARTY_NOTICES complete.
- [ ] All licenses included.
- [ ] Unknown-license code removed.
- [ ] About > Open Source Components complete.
- [ ] Unofficial/non-affiliation disclaimer visible.

## Release
- [ ] Physical-device test matrix passed.
- [ ] Performance/battery report produced.
- [ ] SBOM produced.
- [ ] Release artifact signed.
