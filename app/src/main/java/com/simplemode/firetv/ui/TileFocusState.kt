package com.simplemode.firetv.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.simplemode.firetv.ui.theme.Charcoal
import com.simplemode.firetv.ui.theme.Cream
import com.simplemode.firetv.ui.theme.Fern
import com.simplemode.firetv.ui.theme.FernBright

/**
 * The animated state behind every focusable thing on screen.
 *
 * **Focus fills. It does not outline.** The earlier build drew a fern ring
 * around the focused tile and grew it slightly. Held up against photographs
 * of a real Fire TV -- a set of reference photographs of a real Fire TV, images 05 to 07 --
 * that is the single thing that made ours read as a mock-up. On a real one
 * the selected item is a solid pill with its text knocked out of it and
 * everything unselected has no container at all. A ring is a line; a fill is
 * a shape, and a shape is what survives three metres of lit front room.
 *
 * So the focused state swaps a filled plate in:
 *
 * - **[plate]** goes from nothing at all to `#8FBF83`, the brightest colour
 *   in the app and reserved for this. On a tile the plate is the label band,
 *   roughly 55 dp of solid colour -- nine times the 6 dp
 *   the shared Fire TV craft reference sets as the floor for a ring, and not a line
 *   at any distance. At rest there is no container: the photograph's own
 *   scrim is what the label sits on, so an unfocused tile is a picture
 *   rather than a picture in a box.
 * - **[onPlate]** knocks the label back to charcoal, 8:1 against the plate.
 * - **[veil]** is a charcoal wash over the photograph that lifts on focus, so
 *   the focused picture comes up to full while its neighbours stay held
 *   back. That gap, not the plate, is what the eye catches first.
 * - **[scale]** and **[lift]** add geometry and depth. The shadow is never
 *   the only channel: at the dark end of the range it is a couple of per
 *   cent of luminance and room light erases it.
 *
 * Four redundant channels, none of them a stroke, all of them still legible
 * to somebody who cannot tell fern green from charcoal.
 *
 * 120 ms is the ceiling the shared Fire TV craft reference gives for a focus
 * transition to keep up with D-pad key repeat (~50 ms).
 *
 * Focus is read from `Modifier.onFocusChanged` on the node itself, not from
 * a `MutableInteractionSource` shared between `focusable` and `clickable`.
 * That sharing looked equivalent and was not: on any screen whose only
 * focusable is requested during first composition -- every message screen,
 * and the caregiver PIN gate -- the node held focus and the D-pad worked
 * while the tile drew its resting appearance, so the screen looked like a
 * dead remote.
 */
class TileFocusState(
    val isFocused: Boolean,
    val onFocusChanged: (Boolean) -> Unit,
    val scale: Float,
    val plate: Color,
    val onPlate: Color,
    val veil: Float,
    val lift: Dp,
    val glyphColor: Color,
    val focusRequester: FocusRequester,
)

private val RestingLift = 0.dp
private val FocusedLift = 22.dp
private const val FOCUSED_SCALE = 1.05f
// The wash that holds an unfocused picture back. Two thirds of full is the
// point at which a row of six still reads as six pictures rather than six
// dark squares, and the focused one is still unmistakably forward of them.
private const val RESTING_VEIL = 0.34f
private const val FOCUSED_VEIL = 0f
private const val FOCUS_MS = 120
private const val FOCUS_CLAIM_FRAMES = 12

/**
 * @param focusRequester supply one when the caller already holds the handle --
 *   a keyboard has to know every key's requester before it composes any of
 *   them, because each key's `focusProperties` names its four neighbours.
 *   Left alone, the state makes its own, which is what every tile does.
 */
@Composable
fun rememberTileFocusState(
    initialFocus: Boolean,
    focusRequester: FocusRequester = remember { FocusRequester() },
): TileFocusState {
    var isFocused by remember { mutableStateOf(false) }
    val tween = tween<Float>(FOCUS_MS)
    val scale by animateFloatAsState(if (isFocused) FOCUSED_SCALE else 1f, tween, label = "scale")
    val veil by animateFloatAsState(if (isFocused) FOCUSED_VEIL else RESTING_VEIL, tween, label = "veil")
    val plate by animateColorAsState(if (isFocused) FernBright else Color.Transparent, tween(FOCUS_MS), label = "plate")
    val onPlate by animateColorAsState(if (isFocused) Charcoal else Cream, tween(FOCUS_MS), label = "onPlate")
    val glyphColor by animateColorAsState(if (isFocused) FernBright else Fern, tween(FOCUS_MS), label = "glyph")
    val lift by animateDpAsState(if (isFocused) FocusedLift else RestingLift, tween(FOCUS_MS), label = "lift")

    LaunchedEffect(Unit) {
        if (!initialFocus) return@LaunchedEffect
        // requestFocus() throws if the node is not attached yet, which it is
        // not on the first frame of a freshly navigated screen -- hence the
        // catch. One attempt was not enough: on the home screen the strip of
        // controls above the wall composes first, so when the single request
        // lost the race Compose handed initial focus to the first focusable
        // it could find and the television opened with "Captions" selected
        // instead of the first thing to watch. Retrying for a few frames and
        // stopping as soon as the node actually reports focus is the
        // difference between usually landing right and always landing right.
        repeat(FOCUS_CLAIM_FRAMES) {
            runCatching { focusRequester.requestFocus() }
            withFrameNanos { }
            if (isFocused) return@LaunchedEffect
        }
    }

    return TileFocusState(
        isFocused = isFocused,
        onFocusChanged = { isFocused = it },
        scale = scale,
        plate = plate,
        onPlate = onPlate,
        veil = veil,
        lift = lift,
        glyphColor = glyphColor,
        focusRequester = focusRequester,
    )
}
