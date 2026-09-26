package com.simplemode.firetv.ui

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp

/**
 * A list screen: the title across the top, the way out floating on it, and
 * the rows filling everything below.
 *
 * Two things changed here and both were visible in one glance at the old
 * screen. Every row now carries a photograph rather than a green play triangle
 * on a flat rectangle -- see [listArtworkFor] -- and the rows are rows rather
 * than a three-column grid, because that grid truncated every title in the app
 * and left a hole where a fourth item should have been. [ListRow] carries the
 * arithmetic for why a grid cannot be rescued on a 540 dp canvas.
 *
 * Four rows fill the screen exactly and nothing scrolls. The list is lazy so a
 * longer one still works, but the row height is fixed rather than divided, so
 * a list of twenty looks like a list of four that carries on -- which is what
 * a viewer needs to be able to tell.
 *
 * Home floats on the header band rather than sitting under the content. That
 * is where navigation sits on a real Fire TV -- a set of reference photographs of a real Fire TV
 * image 05, where the nav strip floats on the hero with no card boundary --
 * and under the content it was a row's worth of canvas spent on the exit.
 */
@Composable
internal fun ListScreen(
    title: String,
    items: List<String>,
    icon: TileIcon,
    onSelect: (String) -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val home = remember { FocusRequester() }

    Column(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth()) {
            ScreenHeader(title = title)
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
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 58.dp)
                // One focus group, so a D-pad press cannot be resolved by
                // whatever happens to be geometrically nearest -- which on a
                // screen with a chip floating above the list is the chip.
                .focusGroup(),
        ) {
            // The rows divide whatever height the screen has left, between a
            // floor and a ceiling. A list of three used to leave a fifth of the
            // television empty under it and a list of four filled it exactly,
            // which made the short ones look like the long one had failed to
            // load. Three rows now stand taller and fill the same space; past
            // six the height hits its floor and the list scrolls instead.
            val gaps = ROW_GAP.times(maxOf(items.size - 1, 0))
            val usable = maxHeight - TOP_PAD - BOTTOM_PAD - gaps
            val rowHeight = if (items.isEmpty()) MIN_ROW else (usable / items.size).coerceIn(MIN_ROW, MAX_ROW)

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(ROW_GAP),
                contentPadding = PaddingValues(top = TOP_PAD, bottom = BOTTOM_PAD),
            ) {
                itemsIndexed(items, key = { _, label -> label }) { index, label ->
                    ListRow(
                        title = label,
                        contentDescription = label,
                        height = rowHeight,
                        artwork = listArtworkFor(label),
                        // Only used if a row is ever added without a photograph,
                        // which `ListArtworkTest` is there to stop.
                        icon = icon,
                        initialFocus = index == 0,
                        onClick = { onSelect(label) },
                    )
                }
            }
        }
    }
}

/**
 * The floor is 84 dp, which still gives a 149 dp 16:9 thumbnail and a row a
 * thumb can aim at. The ceiling is 128 dp, past which a row stops reading as a
 * row and starts reading as a banner. Four rows at 93 dp and three gaps is
 * 408 dp, which with an 88 dp header and the 30 dp vertical safe area is
 * exactly the 540 dp canvas -- so the lists this app actually has are all on
 * screen at once with nothing to scroll to.
 */
private val MIN_ROW = 84.dp
private val MAX_ROW = 128.dp
private val ROW_GAP = 12.dp
private val TOP_PAD = 14.dp
private val BOTTOM_PAD = 30.dp
