package com.simplemode.firetv.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.simplemode.firetv.ui.theme.Charcoal
import com.simplemode.firetv.ui.theme.Plinth

/**
 * One huge, D-pad-focusable target: a 16:9 picture running edge to edge,
 * with the tile's name in a band along the bottom of it.
 *
 * The picture is not decoration and it is not an icon floated over a
 * colour. It is chosen for this tile, it fills the whole shape, and it is
 * the reason the wall reads as content rather than as a form. Each one is
 * graded to a different dominant tone, because on a real Fire TV that
 * variation is most of how somebody finds the tile they want without
 * reading it. See [tileArtworkFor].
 *
 * The label stays. A real Fire TV drops captions entirely and lets the
 * artwork do the naming, which works when the artwork is a poster somebody
 * already recognises. Nothing here is recognisable on sight and the viewer
 * this app exists for reads the words, so every tile keeps its name in
 * type sized for the back of the room. What the reference actually settles
 * is where the name goes: inside the picture, in a filled band, not floated
 * on the ground below it.
 *
 * Every picture is a real photograph, cropped to 16:9 and carrying a
 * gradient scrim baked into its lower half. The scrim, rather than a dimmed
 * frame or a drawn box, is what the label sits on: the photograph keeps its
 * own contrast everywhere above it. Provenance for all of them is in the
 * app's ATTRIBUTION.md.
 *
 * The same band is the focus indicator. At rest there is no container at
 * all. On focus it fills with `#8FBF83` and the label knocks out to
 * charcoal. Nothing is outlined at any point -- see [rememberTileFocusState].
 *
 * [artwork] is optional so the screens that are lists of names rather than
 * walls of content -- the live guide, her shows, the caregiver's editor --
 * can keep a drawn glyph and still pick up the same focus behaviour.
 */
@Composable
fun TileCard(
    title: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    @DrawableRes artwork: Int? = null,
    icon: TileIcon? = null,
    heightDp: Int = 220,
    textScale: Float = 1f,
    initialFocus: Boolean = false,
    onClick: () -> Unit,
) {
    val focus = rememberTileFocusState(initialFocus)
    val shape = RoundedCornerShape(TILE_RADIUS_DP.dp)
    val bandDp = labelPlateHeight(heightDp, textScale)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(heightDp.dp)
            .graphicsLayer { scaleX = focus.scale; scaleY = focus.scale }
            .shadow(focus.lift, shape, ambientColor = Charcoal, spotColor = Charcoal)
            .clip(shape)
            .background(Plinth)
            .focusRequester(focus.focusRequester)
            .onFocusChanged { focus.onFocusChanged(it.isFocused) }
            .clickable(onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
    ) {
        if (artwork != null) {
            Image(
                painter = painterResource(artwork),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else if (icon != null) {
            // No picture for this one -- a channel in the guide, a row in the
            // caregiver's editor. The drawn glyph stands in, in fern rather
            // than cream and sized to the space above the label, so it reads
            // as a mark on the tile rather than as a large hollow object
            // hanging off the corner of it.
            TileIconGlyph(
                icon = icon,
                color = focus.glyphColor,
                size = ((heightDp - bandDp) * 0.62f).dp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = ((heightDp - bandDp) * 0.18f).dp),
            )
        }
        // The wash that holds an unfocused tile back. Drawn over the
        // picture and under the label band, so the name never dims.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Charcoal.copy(alpha = focus.veil)),
        )
        TileLabelPlate(title = title, focus = focus, heightDp = bandDp)
    }
}

/** One radius everywhere, the way a real Fire TV packs its tiles. */
const val TILE_RADIUS_DP = 16
