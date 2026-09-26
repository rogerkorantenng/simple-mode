package com.simplemode.firetv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusProperties
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.simplemode.firetv.ui.theme.Charcoal
import com.simplemode.firetv.ui.theme.Fern

/**
 * A small control that reads as an object at three metres: a filled `Fern`
 * plate carrying one word, which lights to `#8FBF83` with the word knocked out
 * in charcoal, and rises, when the D-pad reaches it.
 *
 * Two places use it and they arrived at it separately. The keyboard's keys got
 * containers because an internal review of this batch's three Fire TV apps together found that thirty-six
 * bare capitals read as a word search rather than as a keyboard; the
 * caregiver's catalogue got them because three bare glyphs at the end of a row
 * read as nothing at all. The fix was the same fix, so it is one component --
 * which is also the only way the claim in both files' KDoc, that the keys and
 * the catalogue's controls are the same shape, can be true rather than merely
 * asserted and then drifted apart.
 *
 * **Two fills, stacked.** The lower one is the cap and never changes; the upper
 * one is `focus.plate`, which animates from fully transparent to `#8FBF83` over
 * 120 ms. Layering them rather than branching on `isFocused` keeps the
 * transition, which a `when` would have turned into a jump cut.
 *
 * **The lift is not decoration.** Against a bare charcoal ground a bright plate
 * was the only shape on the screen. Against thirty-five neighbouring fern caps
 * it is 2.35:1, which is a shade. The focused cap therefore also rises, with
 * the lift and shadow the tiles use -- geometry rather than hue, so it survives
 * a viewer who cannot tell the two greens apart.
 *
 * @param modifier carries the caller's own sizing, and nothing else. A key
 *   takes `weight(1f)` so a row of three wide keys divides the same measure as
 *   a row of nine narrow ones, plus a `graphicsLayer` scale so a focused key
 *   grows over its neighbours instead of shoving the row about; a catalogue
 *   control takes a fixed width, so the three columns line up down the list.
 * @param neighbours names what lies in each direction, for a grid where
 *   Compose's own two-dimensional search would resolve by geometry and send a
 *   press somewhere surprising. Null leaves the search alone, which is right
 *   for a control that is simply the next thing in a row.
 */
@Composable
fun FernCap(
    label: String,
    description: String,
    focus: TileFocusState,
    textStyle: TextStyle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp),
    neighbours: (FocusProperties.() -> Unit)? = null,
) {
    val interaction = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .shadow(focus.lift, shape, ambientColor = Charcoal, spotColor = Charcoal)
            .clip(shape)
            .background(Fern)
            .background(focus.plate)
            .focusRequester(focus.focusRequester)
            // Kept in this position in the chain, after the requester and
            // before the listener, because that is where it sat when the
            // keyboard's D-pad traversal was verified key by key.
            .then(if (neighbours != null) Modifier.focusProperties(neighbours) else Modifier)
            .onFocusChanged { focus.onFocusChanged(it.isFocused) }
            // No indication: Material's ripple is the one purple thing that
            // would ever appear in an app built on one warm hue.
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = focus.onPlate,
            textAlign = TextAlign.Center,
            maxLines = 1,
            style = textStyle,
        )
    }
}
