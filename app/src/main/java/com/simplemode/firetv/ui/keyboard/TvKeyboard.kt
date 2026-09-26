package com.simplemode.firetv.ui.keyboard

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.simplemode.firetv.ui.FernCap
import com.simplemode.firetv.ui.rememberTileFocusState

/**
 * Simple Mode's on-screen keyboard.
 *
 * Compose hands a television whatever the platform IME is, which on this
 * emulator is Google's Android TV keyboard and on a real Fire TV is a
 * different thing again -- either way it is not the app's, it arrives over the
 * top of the screen, and on a set in somebody's front room the difference is
 * the first thing an owner notices. So the app draws its own and no IME is
 * ever summoned: the readout above is a readout, not a field, and these keys
 * are the only way characters get into it.
 *
 * **Every key is a key.** Until now an unfocused key was a bare capital letter
 * with no container at all -- the app's tile rule ("no container at rest,
 * filled plate on focus") applied to a grid of thirty-six things. On a wall of
 * photographs that rule is right. On a keyboard it produced what an internal
 * review of this batch's Fire TV apps described as a word search: nine columns
 * of floating capitals in which the only thing that looked like a control was
 * the single letter the D-pad happened to be on. In the app built for somebody
 * who cannot work a television, that is the register failing in the one place
 * it cannot afford to.
 *
 * So the keys are now caps -- see [FernCap], which is the same component the
 * caregiver's catalogue controls use. Focus still works by filling; it just
 * fills something that was already there, the way a real Fire TV's own
 * navigation strip does, per a set of reference photographs of a real Fire TV
 * (image 05: a solid pill on a lighter strip, not an outline on nothing).
 *
 * The honest caveat: none of the seven reference photographs of a real Fire TV
 * is of a keyboard, so the fill rule is verified for the launcher's tiles and
 * navigation and applied here by consistency.
 *
 * Every neighbour is named explicitly through `focusProperties` rather than
 * left to Compose's two-dimensional focus search. On a grid of near-identical
 * boxes that search resolves by geometry, and the row of three wide keys
 * under a row of nine narrow ones is exactly the shape that sends it
 * somewhere surprising. [KeyGrid] decides; this only draws.
 */
@Composable
fun TvKeyboard(
    grid: KeyGrid,
    onKey: (KeyAction) -> Unit,
    keyHeight: Dp,
    modifier: Modifier = Modifier,
    gap: Dp = 8.dp,
    keyShape: RoundedCornerShape = RoundedCornerShape(12.dp),
    /** What lies beyond each edge of the grid. Null leaves that edge to
     *  Compose's own search, which with nothing out there means focus simply
     *  stays put -- the right answer for the bottom of a keyboard. */
    exitUp: FocusRequester? = null,
    exitDown: FocusRequester? = null,
    exitLeft: FocusRequester? = null,
    exitRight: FocusRequester? = null,
) {
    val requesters = remember(grid) { grid.rows.map { row -> List(row.size) { FocusRequester() } } }

    fun requesterFor(move: KeyMove): FocusRequester? = when (move) {
        is KeyMove.To -> requesters[move.position.row][move.position.column]
        KeyMove.ExitUp -> exitUp
        KeyMove.ExitDown -> exitDown
        KeyMove.ExitLeft -> exitLeft
        KeyMove.ExitRight -> exitRight
    }

    // Focus lands on the first key rather than nowhere. `requestFocus()` throws
    // while the node is unattached, which it is for the first frame or two of a
    // freshly navigated screen, so this retries for a dozen frames and stops the
    // moment it takes -- the same claim loop every other screen in the app uses.
    LaunchedEffect(requesters) {
        repeat(FOCUS_CLAIM_FRAMES) {
            if (runCatching { requesters[0][0].requestFocus() }.isSuccess) return@LaunchedEffect
            withFrameNanos { }
        }
    }

    Column(
        modifier = modifier
            .focusGroup()
            .onPreviewKeyEvent { event -> handleHardwareKey(event.type, event.key, event.utf16CodePoint, onKey) },
        verticalArrangement = Arrangement.spacedBy(gap),
    ) {
        grid.rows.forEachIndexed { rowIndex, row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(gap),
            ) {
                row.forEachIndexed { columnIndex, key ->
                    val position = KeyPosition(rowIndex, columnIndex)
                    KeyCap(
                        key = key,
                        keyHeight = keyHeight,
                        shape = keyShape,
                        own = requesters[rowIndex][columnIndex],
                        toLeft = requesterFor(grid.move(position, KeyDirection.LEFT)),
                        toRight = requesterFor(grid.move(position, KeyDirection.RIGHT)),
                        toUp = requesterFor(grid.move(position, KeyDirection.UP)),
                        toDown = requesterFor(grid.move(position, KeyDirection.DOWN)),
                        onClick = { onKey(key.action) },
                    )
                }
            }
        }
    }
}

/**
 * One key: a [FernCap] with a keyboard's sizing and a keyboard's neighbours.
 *
 * `weight(1f)` is what makes a row of three wide keys line up under a row of
 * nine narrow ones without any of the widths being written down: each row
 * divides the same measure, so three keys are three columns each. Scale is on
 * `graphicsLayer`, never on the measured height, so a focused key grows over
 * its neighbours instead of shoving the row about.
 *
 * Every neighbour is named rather than searched for. `FocusProperties` puts
 * `left`/`right`/`up`/`down` in scope, which is why the parameters here are not
 * called that: an implicit receiver shadows an outer parameter, and
 * `left = left` would have compiled into assigning the property to itself.
 */
@Composable
private fun RowScope.KeyCap(
    key: KeyboardKey,
    keyHeight: Dp,
    shape: RoundedCornerShape,
    own: FocusRequester,
    toLeft: FocusRequester?,
    toRight: FocusRequester?,
    toUp: FocusRequester?,
    toDown: FocusRequester?,
    onClick: () -> Unit,
) {
    val focus = rememberTileFocusState(initialFocus = false, focusRequester = own)

    FernCap(
        label = key.label,
        description = key.spoken,
        focus = focus,
        shape = shape,
        // A word key carries more characters than a letter key, so it sits one
        // step down the scale to keep the two the same optical size.
        textStyle = if (key.label.length == 1) {
            MaterialTheme.typography.headlineSmall
        } else {
            MaterialTheme.typography.bodyLarge
        },
        onClick = onClick,
        neighbours = {
            toLeft?.let { left = it }
            toRight?.let { right = it }
            toUp?.let { up = it }
            toDown?.let { down = it }
        },
        modifier = Modifier
            .weight(1f)
            .height(keyHeight)
            .graphicsLayer {
                scaleX = focus.scale
                scaleY = focus.scale
            },
    )
}

private const val FOCUS_CLAIM_FRAMES = 12
