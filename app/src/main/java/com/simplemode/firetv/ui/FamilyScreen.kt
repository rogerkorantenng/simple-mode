package com.simplemode.firetv.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.simplemode.firetv.accessibility.AccessibilityNeeds
import com.simplemode.firetv.family.QueryLogEntry
import com.simplemode.firetv.family.WatchEntry
import com.simplemode.firetv.family.formatRelativeTime
import com.simplemode.firetv.ui.theme.Cream
import com.simplemode.firetv.ui.theme.CreamMuted
import com.simplemode.firetv.Viewer

/**
 * The "settings surface that matters" this build's own engineering brief asked for: not a grid
 * of switches, but the caregiver's honest view into whether the AI feature is
 * working and what the person has been doing, plus the door into the
 * PIN-gated catalogue editor. Every value here is read, not asserted -- an
 * empty section says so rather than inventing content.
 *
 * It was three things at once before and all three were visible in one glance.
 * The two actions at the foot were `TileCard`s at full width, and `TileCard`
 * sizes its glyph and its label band from a tile about 255 dp across: stretched
 * to the full 844 dp they became two 170 dp slabs with a lock hanging in space
 * above a label at the far left. The screen also scrolled, and the editor
 * claimed initial focus, so it opened having already scrolled its own title off
 * the top. And the section headings took `titleLarge`, the same step as a
 * screen title, so nothing on the screen was clearly the name of the screen.
 *
 * Now: one header band with the way out floating on it, the same as every other
 * screen in the app; three readouts at the heading step; one door, at the foot,
 * as the full-width control [FocusableRow] exists to be. Nothing scrolls,
 * because a screen that scrolls on a television is a screen with a part you
 * have to know is there.
 */
@Composable
fun FamilyScreen(
    needs: AccessibilityNeeds,
    queries: List<QueryLogEntry>,
    watched: List<WatchEntry>,
    now: Long,
    onEditCatalogue: () -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val home = remember { FocusRequester() }

    Column(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth()) {
            ScreenHeader(title = "For the Family")
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 58.dp)
                    .width(168.dp),
            ) {
                FocusableRow(
                    label = "Home",
                    contentDescription = "Back to the Simple Mode home screen.",
                    focusRequester = home,
                    onClick = onHome,
                )
            }
        }
        // The readouts are bounded, not free to grow. Unbounded, a household
        // with three watched items pushed the editor off the bottom of the
        // television -- the one control on the screen, gone, with nothing to
        // say it was there.
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 58.dp)
                .padding(top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Two sentences rather than two fields joined by a middle dot, and
            // the same wording the home screen uses, so the family reads the
            // same claim in both places. On one line, because this screen holds
            // exactly one line a section -- see [andMore].
            FamilySection(title = "What Simple Mode sees right now") {
                Line(
                    (if (needs.captionsEnabled) "Captions are on." else "Captions are off.") + " " +
                        when (needs.audioDescriptionsEnabled) {
                            true -> "Audio description is on."
                            false -> "Audio description is off."
                            null -> "This TV does not report audio description."
                        },
                )
            }

            FamilySection(title = "What ${Viewer.NAME} has asked Tell Me") {
                val latest = queries.firstOrNull()
                if (latest == null) {
                    Line("No questions asked yet.", muted = true)
                } else {
                    Line(
                        "\"${latest.utterance}\" -> ${latest.matchedTitle ?: "no match"}, " +
                            formatRelativeTime(now, latest.timestampMillis) + andMore(queries.size),
                    )
                }
            }

            FamilySection(title = "What ${Viewer.NAME} has watched") {
                val latest = watched.firstOrNull()
                if (latest == null) {
                    Line("Nothing watched yet.", muted = true)
                } else {
                    Line(
                        "${latest.label}, " + formatRelativeTime(now, latest.timestampMillis) +
                            andMore(watched.size),
                    )
                }
            }
        }
        FocusableRow(
            label = "Edit what ${Viewer.NAME} sees",
            contentDescription = "Change ${Viewer.POSSESSIVE} home screen tiles. Protected by the family PIN.",
            icon = TileIcon.Lock,
            prominent = true,
            initialFocus = true,
            modifier = Modifier.padding(horizontal = 58.dp).padding(bottom = 30.dp),
            onClick = onEditCatalogue,
        )
    }
}

@Composable
private fun Line(text: String, muted: Boolean = false) {
    Text(
        text = text,
        color = if (muted) CreamMuted else Cream,
        style = MaterialTheme.typography.bodyLarge,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/**
 * The most recent entry and a count of the rest, on one line.
 *
 * One line a section, because three sections and a door is all a 540 dp canvas
 * holds. At the four-size scale a heading is 48 dp and a line is 38, so three of
 * each plus the gaps is 304 of the 310 dp under the header band. A second line
 * anywhere clips the section below it -- which is how the "What Norah has
 * watched" heading briefly ended up with nothing under it, and a heading with
 * nothing under it is the exact defect the set review calls "content that
 * failed to arrive".
 */
private fun andMore(total: Int): String = if (total <= 1) "" else ", and ${total - 1} more"
