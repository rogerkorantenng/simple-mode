package com.simplemode.firetv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * The band along the bottom of a tile that carries its name, and that fills
 * with `#8FBF83` when the tile is focused.
 *
 * It lives in its own file because it is the app's focus indicator, not a
 * detail of [TileCard]. the shared focus-plate design notes calls it the
 * plate and the whole focus argument turns on it: at rest there is no
 * container at all, so an unfocused tile is a photograph rather than a
 * photograph in a box; on focus the band becomes about 55 dp of solid colour,
 * which is nine times the 6 dp the shared Fire TV craft reference sets as the floor for a ring and
 * is not a line at any distance.
 *
 * The height is 38% of the tile, which is where the scrim baked into every
 * photograph ends, or one line of type at the current text setting plus its
 * padding -- whichever is larger. At the captions-on boost the type wins and
 * the plate grows over the picture rather than clipping the name.
 */
@Composable
fun BoxScope.TileLabelPlate(
    title: String,
    focus: TileFocusState,
    heightDp: Float,
) {
    Box(
        modifier = Modifier
            .align(Alignment.BottomStart)
            .fillMaxWidth()
            .height(heightDp.dp)
            .background(focus.plate),
        contentAlignment = Alignment.CenterStart,
    ) {
        Text(
            text = title,
            color = focus.onPlate,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            // Ellipsis, not Clip: "11 - Classic Movies" was silently
            // rendering as "11 - Classic", which reads as a different
            // channel rather than as a truncation.
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 18.dp),
        )
    }
}

/**
 * How tall that plate is on a tile of [tileHeightDp], at [textScale].
 * Shared so [TileCard] can size the artwork above it from the same number.
 */
fun labelPlateHeight(tileHeightDp: Int, textScale: Float): Float =
    maxOf(tileHeightDp * LABEL_BAND_FRACTION, MIN_BAND_DP * textScale)

private const val LABEL_BAND_FRACTION = 0.38f
private const val MIN_BAND_DP = 54f
