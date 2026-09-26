package com.simplemode.firetv.ui

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * The home wall: rows of 16:9 tiles, sized from the width they are given
 * rather than from a hard-coded height, so the picture inside each one is
 * never letterboxed or cropped off-centre whatever the margins become.
 *
 * Not a `LazyVerticalGrid`: a handful of fixed tiles need no virtualisation,
 * and a simple, non-scrolling-by-itself layout is far less prone to the
 * nested-scrollable sizing bugs a lazy grid invites when its own height is
 * not pinned down by its parent -- see FRICTION.md.
 *
 * Each row is a `focusGroup()`, per the shared Fire TV craft reference's section 2:
 * without it, Compose's bidirectional focus search can jump a D-pad press
 * sideways into a row that merely happens to be geometrically closer, which
 * is the most common "focus went somewhere stupid" bug in a TV app.
 *
 * Gaps are small and even. That is not crowding: the reference photographs
 * show a real Fire TV packing tiles tightly and staying calm, because the
 * artwork and the filled focus carry the separation that a sparse grid has
 * to get from empty space.
 */
@Composable
fun TileGrid(
    tiles: List<HomeTile>,
    columns: Int,
    textScale: Float,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val gaps = GAP_DP.dp * (columns - 1)
        // Leave the focused tile somewhere to grow into. It scales by 5%
        // on `graphicsLayer`, never on measured size, so nothing reflows --
        // but it still has to stay inside the 5% overscan line Amazon
        // singles out as the one thing that must never be crossed.
        val available = maxWidth - SCALE_CLEARANCE_DP.dp * 2
        val tileWidth = (available - gaps) / columns
        val tileHeight = (tileWidth.value * 9f / 16f).toInt()

        val rows = tiles.chunked(columns)
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = SCALE_CLEARANCE_DP.dp)) {
            rows.forEachIndexed { rowIndex, rowTiles ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        // No gap under the last row: it would push the wall
                        // 14 dp closer to the bottom of the screen for
                        // nothing, and that is 14 dp of overscan margin.
                        .padding(bottom = if (rowIndex == rows.lastIndex) 0.dp else GAP_DP.dp)
                        .focusGroup(),
                    horizontalArrangement = Arrangement.spacedBy(GAP_DP.dp),
                ) {
                    rowTiles.forEachIndexed { columnIndex, tile ->
                        TileCard(
                            title = tile.title,
                            artwork = tile.artwork,
                            contentDescription = tile.contentDescription,
                            heightDp = tileHeight,
                            textScale = textScale,
                            initialFocus = rowIndex == 0 && columnIndex == 0,
                            onClick = tile.onClick,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    // Pad out a short final row so every tile keeps the same
                    // width as a full row, rather than stretching to fill it.
                    repeat(columns - rowTiles.size) {
                        Row(modifier = Modifier.weight(1f)) {}
                    }
                }
            }
        }
    }
}

private const val GAP_DP = 14
private const val SCALE_CLEARANCE_DP = 10
