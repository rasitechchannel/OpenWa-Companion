# Skill: Background Sync

- Connection ownership must be explicit.
- Avoid duplicate sockets from Activity + Service.
- Service exposes state through repository/Flow.
- Reconnect on transient failure.
- Stop reconnect on logout/revoked credential.
- Handle airplane mode, Wi-Fi/cellular switch, doze, process death, reboot.
- Boot receiver only if user enabled persistent sync and platform policy permits.
- Foreground notification must be honest and user-controllable.
- Battery optimization guidance must not pressure user to disable protections unnecessarily.
