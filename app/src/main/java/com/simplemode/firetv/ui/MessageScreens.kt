package com.simplemode.firetv.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * The three screens that are one short message and the one way back, and the
 * words each of them says.
 *
 * How that shape is laid out lives next door in [MessageScreen]. What a screen
 * tells somebody and how it is arranged are two arguments, and they were being
 * had in one file.
 */

@Composable
fun NowPlayingScreen(label: String, onHome: () -> Unit, modifier: Modifier = Modifier) {
    MessageScreen(
        title = "Now Playing",
        body = label,
        // Three lines, not five. At the four-size scale a five-line paragraph
        // plus a 66sp channel name pushed the one control on the screen off the
        // bottom of the television.
        detail = "Nothing is really playing here. The recovery button in the " +
            "corner brings Simple Mode back.",
        onHome = onHome,
        modifier = modifier,
    )
}

@Composable
fun CallForHelpScreen(onHome: () -> Unit, modifier: Modifier = Modifier) {
    MessageScreen(
        title = "Call for Help",
        detail = "This TV was set up by a family member. A real build would show " +
            "their name and a one-button call placed over Wi-Fi, configured from " +
            "their phone. That remote setup is not built in this version; see SPEC.md.",
        onHome = onHome,
        modifier = modifier,
    )
}

/**
 * What she sees when she presses something this television cannot open.
 *
 * The title used to be "Not Set Up Yet" over "This TV does not have a settings
 * screen for that." an internal review of this batch's three Fire TV apps together: "The title blames the
 * setup; the sentence blames the TV. Neither is what happened, and a person who
 * cannot work a television has just been told she failed at something."
 *
 * What actually happened is that the app looked for something and it is not on
 * this set. So the title says that, in sentence case, without naming a culprit,
 * and the sentence under it is the one the caller passed -- a plain statement of
 * which thing is missing. Nothing apologises and nothing is anybody's fault.
 */
@Composable
fun NotAvailableScreen(message: String, onHome: () -> Unit, modifier: Modifier = Modifier) {
    MessageScreen(
        title = "Nothing to open",
        detail = message,
        onHome = onHome,
        modifier = modifier,
    )
}
