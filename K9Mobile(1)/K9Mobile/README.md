# K-9 Mobile — Android port

Ported from `jOethebuilder/K-9` (ESP32 firmware). Native Kotlin + Jetpack
Compose. iOS is ruled out: Core NFC blocks third-party Mifare Classic access,
which QIDI needs.

This is the **full rebuild** — proper per-protocol multi-screen flow matching
the firmware's actual screen graph, not the flattened single-screen-per-
protocol first pass. If you're comparing against an earlier zip, this
replaces it entirely.

## Screen graph (matches firmware's Screen enum)

- **Main menu** — 4 buttons: QIDI, OPENSPOOL U1, ANYCUBIC, SETTINGS
- **QIDI** — sub-menu (auto-scan) → entry (manufacturer/material/color) →
  material picker, color picker
- **OpenSpool U1** — sub-menu (auto-scan) → entry (manufacturer/material/
  subtype/color, SAVE/SEND/READ) → manufacturer picker, material picker,
  subtype picker (PLA/PETG only), color picker, slot picker (SEND)
- **Anycubic ACE** — sub-menu (auto-scan) → entry (material/size/color,
  SAVE/READ) → material picker, color picker, custom color (R/G/B sliders)
- **Settings** — WiFi (opens system settings, see below), U1 Connection,
  NFC Status, App Info, Factory Reset

Dropped entirely (no phone equivalent): screensaver, backlight, touch
calibration, splash animation.

**WiFi**: firmware's on-screen SSID/password keyboard isn't rebuilt — a
phone already manages its own WiFi at the OS level. The Settings screen's
WIFI button opens Android's system WiFi settings instead. U1 Connection
(Moonraker host) is the one setting that's actually app-specific, and that
got its own real screen with save/test.

## Shared NFC architecture

- **`NfcSessionManager`** — wraps `enableReaderMode()`, replaces firmware's
  polling `loop()`. Delivers every detected tag; routing by protocol AND
  current screen happens in `MainActivity`.
- **`TagPresencePoller`** — since Android's reader mode has no "tag removed"
  event, presence is checked by repeatedly probing an `NfcA` connect/close
  on the retained `Tag` object (~150ms interval, 30s safety cap — same
  numbers as firmware's hold-loop). Drives both sub-menu auto-clear
  (`qidiTagPresent`-style) and entry-screen auto-revert-after-READ.
- **`NfcFlowState`** — arm-then-tap coordinator for SAVE and READ buttons.
  Firmware's `waitForTag()` blocks the whole device; a phone can't block the
  UI thread, so tapping SAVE/READ arms an action, and the next NFC tap
  (anywhere) executes it and reports back. Takes priority over sub-menu
  auto-scan in `MainActivity.handleDetection()`.
- **Per-screen routing** (`MainActivity.handleDetection`): reader-mode's
  `FLAG_READER_NFC_A/B` can't distinguish Mifare Classic from NTAG21x at the
  radio level (both are NFC-A), so the "QIDI screen only reacts to Mifare
  Classic, OpenSpool/Anycubic screens only react to NTAG" split from
  firmware happens here in software, keyed off the current nav route.

## Direct ports (unchanged from the original scaffold)
- `protocol/` — all lookup tables, `GetSku()`, `nearestColorName()`,
  subtype rules.
- `network/MoonrakerClient.kt` — U1 test/send, same endpoints and macro.
- `nfc/QidiTagIO.kt`, `nfc/AceTagIO.kt`, `nfc/OpenSpoolTagIO.kt` — same
  page/block layouts as firmware, Android tech classes instead of PN532
  I2C calls.

## Untested / needs a real device + real tags
- `AceTagIO` — SKU write at pages 5-9 not hardware-verified.
- `OpenSpoolTagIO` — uses Android's built-in `Ndef` API rather than firmware's
  hand-built TLV framing. Worth confirming a tag written by this app reads
  back correctly on the ESP32 firmware and vice versa.
- `TagPresencePoller`'s NfcA-probe approach for "is the tag still there" is
  a reasonable guess at Android's NFC behavior but hasn't been confirmed
  against real hardware — if sub-menu screens don't auto-clear when you lift
  a tag, or entry screens don't revert from the read view, this is the
  first place to check.
- The U1 OpenRFID/External mode SEND conflict (`003-0522-0000-0000`) is a
  printer-firmware-mode issue, unresolved same as firmware notes.
- `GetSku()` table — still unverified against a live Anycubic source.

## Building without Android Studio

`.github/workflows/build-apk.yml` builds a debug APK in GitHub's cloud on
every push to `main` — see the Actions tab after pushing. Download the
`k9-mobile-debug-apk` artifact, unzip it, sideload `app-debug.apk` onto
your phone.
