package com.simplemode.firetv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.simplemode.firetv.ui.theme.Cream
import com.simplemode.firetv.ui.theme.FernTitle
import com.simplemode.firetv.ui.theme.Plinth
import com.simplemode.firetv.Viewer

/**
 * One row in the caregiver's catalogue editor: where a tile sits in the order,
 * what it is called, and the three things that can be done to it.
 *
 * **It used to be three unlabelled glyphs.** an internal review of this batch's three Fire TV apps together
 * is blunt about it: five rows each carrying a bare up arrow, a bare down arrow
 * and a cross, parked at the far right of a row fourteen hundred pixels wide,
 * "a two-dimensional glyph grid, in the app whose whole premise is that its
 * user cannot navigate one". The label and the control that acted on it were a
 * screen apart, and nothing on the screen said what any of the three did. Two
 * of those glyphs also meant nothing on their own: an up arrow next to a tile
 * name could as easily have meant "open" as "move".
 *
 * Three changes, all of them the same change:
 *
 * - **The glyphs are words.** "Move up", "Move down", "Remove". A caregiver
 *   reads them once and the screen is finished explaining itself.
 * - **The row is 760 dp, not the whole screen.** The controls now sit beside
 *   the name they act on rather than at the far end of an empty field.
 * - **The position is printed.** This list is the order the tiles appear on the
 *   home screen, so it really is a sequence, and a number is the one thing that
 *   makes "move up" mean something before you press it. It also replaces the
 *   fern bar that used to run down the leading edge of every row -- which was a
 *   decoration here, and is Profile Gate's genuine invention over there.
 *
 * The three controls are [FernCap]s -- the same component the keyboard's keys
 * are, for the same reason: a control that has no container at rest is not
 * legible as a control.
 *
 * A compact row, not another huge [TileCard]: this screen is read by the person
 * setting the television up, at a desk or on a sofa with the remote in hand,
 * not by the viewer three metres away. The ten-foot rule is about what *she*
 * sees.
 */
@Composable
fun CatalogRow(
    position: Int,
    title: String,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    firstAction: FocusRequester? = null,
) {
    Row(
        modifier = modifier
            .width(CATALOGUE_COLUMN)
            .height(ROW_HEIGHT)
            .clip(RoundedCornerShape(12.dp))
            .background(Plinth)
            // Every row is a focus group, or Compose's bidirectional search
            // resolves a sideways press by whichever control happens to be
            // geometrically nearest in the row above.
            .focusGroup()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ACTION_GAP),
    ) {
        Text(
            text = "$position",
            color = FernTitle,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(34.dp),
        )
        Text(
            text = title,
            color = Cream,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(end = 12.dp),
        )
        RowAction("Move up", "Move $title up to position ${position - 1}.", onMoveUp, firstAction)
        RowAction("Move down", "Move $title down to position ${position + 1}.", onMoveDown)
        RowAction("Remove", "Remove $title from ${Viewer.POSSESSIVE} home screen.", onRemove)
    }
}

/**
 * One of the three controls: a [FernCap] at a fixed width, so the three columns
 * line up all the way down the list and the screen reads as a table rather than
 * as five ragged rows. It is literally the same component the keyboard's keys
 * are, because a caregiver on a sofa is holding the same remote as everybody
 * else and a control with no container at rest is not legible as a control.
 *
 * The spoken description says which tile and which direction, because "Move up"
 * read five times in a row by VoiceView is five identical announcements.
 */
@Composable
private fun RowAction(
    label: String,
    description: String,
    onClick: () -> Unit,
    focusRequester: FocusRequester? = null,
) {
    val focus = if (focusRequester != null) {
        rememberTileFocusState(false, focusRequester)
    } else {
        rememberTileFocusState(false)
    }

    FernCap(
        label = label,
        description = description,
        focus = focus,
        shape = RoundedCornerShape(10.dp),
        textStyle = MaterialTheme.typography.bodyLarge,
        onClick = onClick,
        modifier = Modifier.width(ACTION_WIDTH).height(ACTION_HEIGHT),
    )
}

// The column measure is `CATALOGUE_COLUMN` in CaregiverCatalogScreen.kt. The
// three action widths and the number column are subtracted from it, and what is
// left is the tile's name -- 250 dp, which holds the longest name in the
// catalogue with room to spare.
private val ROW_HEIGHT = 76.dp
private val ACTION_WIDTH = 152.dp
private val ACTION_HEIGHT = 58.dp
private val ACTION_GAP = 8.dp
