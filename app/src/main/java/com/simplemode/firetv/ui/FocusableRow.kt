package com.simplemode.firetv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.simplemode.firetv.ui.theme.Cream
import com.simplemode.firetv.ui.theme.CreamMuted

/**
 * A full-width secondary control that reads as a control. Family setup used
 * to be a plain `Text` with `clickable` on it, which left Compose's own
 * default focus indication carrying the whole state: measured on the
 * emulator at 1.03:1 against the ground, which is the figure
 * the shared Fire TV craft reference names when it says tonal elevation is not a
 * focus mechanism. It also read as a caption rather than as the door to
 * the caregiver's half of the app. Same four channels as [TileCard], in a
 * quieter shape so it does not compete with the grid.
 *
 * Filled, never outlined, for the same reason as [TileCard]: at rest it is
 * plain type on the ground with no container, and focus puts a solid
 * `#8FBF83` plate behind it with the label knocked out in charcoal.
 */
@Composable
fun FocusableRow(
    label: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    icon: TileIcon? = null,
    prominent: Boolean = false,
    initialFocus: Boolean = false,
    /** Supply one when something else has to be able to send focus here --
     *  a keyboard, whose top row names this as what lies above it. */
    focusRequester: FocusRequester = remember { FocusRequester() },
    onClick: () -> Unit,
) {
    val focus = rememberTileFocusState(initialFocus, focusRequester)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (focus.isFocused) focus.plate else Color.Transparent)
            .focusRequester(focus.focusRequester)
            .onFocusChanged { focus.onFocusChanged(it.isFocused) }
            .clickable(onClick = onClick)
            .semantics { this.contentDescription = contentDescription }
            .padding(horizontal = 24.dp, vertical = if (prominent) 20.dp else 14.dp),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (icon != null) {
            TileIconGlyph(
                icon = icon,
                color = if (focus.isFocused) focus.onPlate else CreamMuted,
                // 96 dp made a prominent row 140 dp tall, which is a
                // quarter of the canvas spent on one glyph.
                size = if (prominent) 64.dp else 56.dp,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = 18.dp),
            )
        }
        Text(
            text = label,
            color = if (focus.isFocused) focus.onPlate else Cream,
            style = if (prominent) {
                MaterialTheme.typography.headlineSmall
            } else {
                MaterialTheme.typography.bodyLarge
            },
        )
    }
}
