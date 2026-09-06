package com.joethebuilder.k9.ui.screens.settings

import com.joethebuilder.k9.protocol.HelpTopic

/**
 * Holds the tapped-on help topic between the list screen and the detail
 * screen, same small-singleton pattern as WriteArmState/ActiveProtocol —
 * avoids passing a topic ID through nav arguments for a one-item handoff.
 */
object HelpSelection {
    var current: HelpTopic? = null
}
