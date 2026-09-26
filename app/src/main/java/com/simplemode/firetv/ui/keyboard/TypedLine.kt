package com.simplemode.firetv.ui.keyboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.simplemode.firetv.ui.theme.Cream
import com.simplemode.firetv.ui.theme.CreamMuted
import com.simplemode.firetv.ui.theme.Fern
import com.simplemode.firetv.ui.theme.Plinth

/**
 * Where the letters land.
 *
 * This replaced `SimpleModeTextField`, and the change is not cosmetic. A
 * `BasicTextField` is focusable, and focusing it is what summons the platform
 * IME -- the whole problem. With the app drawing its own keys there is nothing
 * left for this to do except show what has been typed, so it is a readout: not
 * focusable, never in the D-pad's path, and incapable of putting somebody
 * else's keyboard over the screen.
 *
 * The old field's one good idea is kept. It could not swap its own fill for
 * the focus colour, because text being typed has to stay readable, so it wore
 * a solid plate along its bottom edge instead of a stroke. That plate is still
 * here and is now `Fern` rather than `FernBright`: it marks the destination
 * rather than the focus, and `#8FBF83` belongs to focus alone.
 */
@Composable
fun TypedLine(
    text: String,
    placeholder: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            // Pinned, because the placeholder sits on the reading step and the
            // typed line on the one above it. Left to wrap, the readout grew by
            // ten dp the moment the first letter arrived and shoved the whole
            // keyboard down under the viewer's thumb.
            .heightIn(min = 70.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Plinth)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.CenterStart,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (text.isEmpty()) {
                Text(
                    text = placeholder,
                    color = CreamMuted,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            } else {
                Text(
                    // The *end* of the line is the part somebody typing is
                    // looking at, so a long utterance loses its head rather
                    // than its tail and the caret never walks off the edge.
                    text = visibleTail(text),
                    color = Cream,
                    // The pressable step, not the screen-title step: this is a
                    // readout of what the keys did, and it sits under a title
                    // that is already 50sp.
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                // A caret, and it does not blink. Nothing else on a Simple Mode
                // screen moves unless the viewer moved it, and a flashing block
                // is exactly the kind of thing that reads as a fault to someone
                // who is already unsure the television is working.
                Box(
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(width = 6.dp, height = 40.dp)
                        .background(Fern),
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .height(10.dp)
                .background(Fern),
        )
    }
}

/**
 * The last [VISIBLE_CHARACTERS] of what has been typed, with a leading ellipsis
 * once anything has scrolled off. 28 characters is what fits on one line at
 * 38 sp across the working width, measured rather than guessed.
 */
internal fun visibleTail(text: String): String =
    if (text.length <= VISIBLE_CHARACTERS) text else "…" + text.takeLast(VISIBLE_CHARACTERS)

private const val VISIBLE_CHARACTERS = 28
