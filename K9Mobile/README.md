# K-9 Mobile — Android port scaffold

Ported from `jOethebuilder/K-9` (ESP32 firmware). Native Kotlin + Jetpack Compose,
per the platform decision: iOS is ruled out because Core NFC blocks third-party
Mifare Classic access, which QIDI needs.

## What's a direct port (should just work)
- `protocol/` — QidiData, OpenSpoolData, AceData: all lookup tables, `GetSku()`,
  `nearestColorName()`, subtype rules. Copied logic, not rewritten.
- `network/MoonrakerClient.kt` — `u1TestConnection` / `u1SendFilamentConfig`,
  same endpoints, same G-code macro, same OpenRFID/External mode caveat.

## What's rebuilt on Android APIs (same job, different mechanism)
- `nfc/QidiTagIO.kt` — `MifareClassic.authenticateSectorWithKeyA()` /
  `readBlock()` / `writeBlock()` instead of PN532 I2C calls. Same key (all-FF),
  same sector/block, same byte layout.
- `nfc/AceTagIO.kt` — `MifareUltralight.readPages()`/`writePage()` (NTAG21x is
  command-compatible with Ultralight) instead of `ntag2xx_ReadPage`/`WritePage`.
  Same page numbers, same layout, alpha still locked to 0xFF.
- `nfc/OpenSpoolTagIO.kt` — uses Android's `Ndef`/`NdefRecord` API instead of
  hand-built TLV framing, since Android already implements that layer. Same
  JSON payload shape (`protocol`, `brand`, `type`, `subtype`, `color_hex`, temps).
- `nfc/NfcSessionManager.kt` — `enableReaderMode()` push callback replaces the
  firmware's `loop()` polling + `tagPresent` booleans.

## New design decision made during the port (flagging per your process)
**Auto-detect protocol on tag tap** instead of "pick protocol screen first,
then tap." Tap a tag anywhere in the app -> it's identified (Mifare Classic ->
QIDI; NTAG21x -> ACE header check, then OpenSpool NDEF check) and routes to
the matching screen automatically. Manual navigation from the main menu is
still there as a fallback. Say the word if you'd rather force manual selection
first — it's a small change to `NfcSessionManager`/`MainActivity`.

## Real architecture problem this port had to solve
Firmware writes were synchronous — `waitForTag()` blocked until a tag showed
up, then wrote. A phone can't block the UI thread on a tap. So WRITE is now
two steps: tap WRITE (arms the write via `WriteArmState`), then tap the tag
(next NFC callback executes the armed write instead of routing/reading). Every
screen's WRITE button shows "Hold tag near phone…" while armed — that's the
mobile equivalent of the firmware's "Hold tag near reader..." footer message.

## Not ported (no phone equivalent)
Screensaver, backlight control, touch calibration, splash animation — all
hardware-specific, dropped entirely.

## UI simplification worth knowing about
The firmware's paged 3x3/4x3 grid pickers (`drawXMaterialPicker` etc.) exist
because a 320x240 touchscreen can't scroll. On a phone, `SelectableList` is
just a scrollable list — no pagination logic needed. Functionally equivalent,
less code.

## Untested / needs a real device + real tags before trusting it
- `AceTagIO` — `MifareUltralight.readPages()` behavior against real NTAG215
  ACE tags hasn't been hardware-verified in this port. Confirm the SKU write
  at pages 5-9 doesn't collide with anything Anycubic firmware reads before
  relying on it printer-side.
- `OpenSpoolTagIO` — using Android's built-in `Ndef`/`NdefFormatable` instead
  of your own TLV builder means it's relying on Android's NDEF stack instead
  of matching your firmware's exact byte-for-byte TLV output. Worth a
  byte-level comparison against a tag written by the ESP32 firmware to
  confirm cross-compatibility (K-9-written tag readable by phone, and
  vice versa).
- The U1 OpenRFID/External mode SEND conflict (`003-0522-0000-0000`) is a
  printer-firmware-mode issue, not something this port can fix — same
  decision-pending status as the firmware notes.
- `GetSku()` table — still unverified against a live Anycubic source, ported
  as-is with the same caveat.

## Not yet built
- Settings screen (U1 host entry — currently only read via `PrefsRepository`,
  no UI to write it yet)
- Manual protocol-selection entry screens beyond the picker lists shown here
  (e.g. an explicit "blank tag" state, matching firmware's `TAG_BLANK`)
- Any handling for `UNKNOWN_MIFARE_CLASSIC` / `UNKNOWN_NTAG` beyond a TODO
  comment in `MainActivity`

## Building the APK without Android Studio

`.github/workflows/build-apk.yml` builds a debug APK in GitHub's cloud on
every push — no SDK, no Android Studio, no local install at all.

1. Push this project to a GitHub repo (new repo, or a folder in an existing
   one — e.g. alongside `jOethebuilder/K-9`).
2. GitHub Actions runs automatically. Watch it under the repo's **Actions**
   tab — the build takes a few minutes.
3. When it finishes (green check), open that run and scroll to
   **Artifacts** at the bottom. Download `k9-mobile-debug-apk` — it's a zip
   containing `app-debug.apk`.
4. Get that APK onto your phone (email it to yourself, AirDrop-equivalent,
   Google Drive, USB file transfer — whatever's easiest) and tap it to
   install. Android will ask you to allow installs from that source once;
   confirm and it installs like any app.
5. You won't get live Logcat debug output this way (that needs `adb`, which
   does need something installed — see below), but you'll have a working
   app on your phone to actually tap tags with.

If a build fails, click into the failed step in the Actions log — Gradle
errors show up there same as they would in Android Studio's build output,
just read from a webpage instead of an IDE pane.

### If you want live debug logs later
`adb` (Android Debug Bridge) is the one piece you'd eventually want for
Serial.println-style debugging, and it does NOT require Android Studio —
it comes bundled in Android's much smaller "command line tools" package, or
via `brew install android-platform-tools` / equivalent package manager. Not
needed to get the app running now, only if NFC reads/writes misbehave and
you want to see what's happening in real time.

