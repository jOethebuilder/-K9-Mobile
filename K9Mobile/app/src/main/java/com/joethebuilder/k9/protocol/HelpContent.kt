package com.joethebuilder.k9.protocol

/**
 * Static help text shown in Settings > Help. Kept basic and step-by-step
 * on purpose — a tester following these shouldn't need any background
 * on the project, just what to tap and what to enter, in order.
 */
data class HelpTopic(val id: String, val title: String, val body: String)

object HelpContent {
    val topics: List<HelpTopic> = listOf(
        HelpTopic(
            id = "getting_started",
            title = "Getting Started",
            body = """
                1. From the main screen, pick the menu item that matches your tag or printer: QIDI, OPENSPOOL U1, ANYCUBIC, or BAMBU OPENSPOOL.

                2. To READ a tag: just hold it to the back of your phone while you're on that screen. It reads automatically — no button needed.

                3. To WRITE a tag: tap WRITE, fill in the form, then hold a blank tag to your phone.

                Your phone's NFC antenna is usually near the top-back of the phone, but it depends on the model. If nothing happens, slide the tag slowly around the back of the phone until it reads.
            """.trimIndent()
        ),
        HelpTopic(
            id = "qidi",
            title = "QIDI",
            body = """
                1. Tap QIDI on the main screen.
                2. Hold a tag to your phone — it reads automatically.
                3. To write a tag instead: tap WRITE, choose Manufacturer, Material, and Color, then hold a blank tag to your phone.

                Testing your QIDI connection:
                1. Go to Settings > QIDI CONNECTION.
                2. Enter your printer's IP address.
                3. Tap TEST CONNECTION.
                4. "CONNECTED" means it worked. "CONNECTION FAILED" means check the IP address and make sure your phone and printer are on the same WiFi network.

                Note: QIDI tags don't store temperature info. That's normal.
            """.trimIndent()
        ),
        HelpTopic(
            id = "openspool_u1",
            title = "OpenSpool U1",
            body = """
                1. Tap OPENSPOOL U1 on the main screen.
                2. Hold a tag to your phone to read it, or tap WRITE to fill in a new one.
                3. To send the current settings straight to your printer (no tag needed for this step): tap SEND.

                Testing your U1 connection:
                1. Go to Settings > U1 CONNECTION.
                2. Enter your printer's IP address.
                3. Tap TEST CONNECTION.
                4. "CONNECTED" means it worked. "CONNECTION FAILED" means check the IP address and make sure your phone and printer are on the same WiFi network.
            """.trimIndent()
        ),
        HelpTopic(
            id = "anycubic",
            title = "Anycubic ACE",
            body = """
                1. Tap ANYCUBIC on the main screen.
                2. Hold a tag to your phone to read it, or tap WRITE to fill in a new one.
                3. For an exact color instead of a preset: on the color screen, tap CUSTOM.

                This one hasn't been tested much on real hardware yet — if anything looks wrong after writing a tag, note exactly what you did and what happened.
            """.trimIndent()
        ),
        HelpTopic(
            id = "bambu",
            title = "Bambu OpenSpool",
            body = """
                On the printer, before doing anything in the app:
                1. Open the printer's network/WLAN settings on its screen.
                2. Turn ON "LAN Mode" and "Developer Mode." Do NOT turn on "LAN Only Mode" — leave that off.
                3. Still in network settings, note down three things:
                   - IP address
                   - Serial Number (may also be on a sticker on the printer, or in Bambu Studio/Handy under printer info)
                   - Access Code (under WLAN > Access Code)

                In the app:
                1. Go to Settings > BAMBU CONNECTION.
                2. Enter the IP address in "Printer IP address."
                3. Enter the serial number in "Serial number."
                4. Enter the access code in "LAN access code."
                5. Tap TEST CONNECTION. "CONNECTED" means setup worked. "CONNECTION FAILED" means double-check all three fields and that your phone and printer are on the same WiFi network.

                Using it:
                1. Tap BAMBU OPENSPOOL on the main screen.
                2. Hold a tag to your phone to read it, or tap WRITE to fill in a new one.
                3. Tap SEND to push the current settings to the printer over WiFi (into the external spool slot).

                This has not been tested against a real Bambu printer yet. If SEND doesn't work, screenshot whatever message shows up and send it back.
            """.trimIndent()
        ),
        HelpTopic(
            id = "spoolman",
            title = "Spoolman",
            body = """
                Shows filament spool info from Spoolman, if you have it set up separately.

                Still a work in progress — if something looks off, note what you expected versus what actually showed up.
            """.trimIndent()
        )
    )
}
