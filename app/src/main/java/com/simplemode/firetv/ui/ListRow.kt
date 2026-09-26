package com.simplemode.firetv.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.simplemode.firetv.ui.theme.Charcoal
import com.simplemode.firetv.ui.theme.Cream
import com.simplemode.firetv.ui.theme.Plinth

/**
 * One row of a list screen: a photograph on the left, the name beside it in
 * type big enough to read from a sofa.
 *
 * **Why a row and not a tile in a grid.** The grid these screens used to be
 * truncated every single title -- "The One About t...", "Sunday Afterno..." --
 * which on an app for somebody who cannot work a television is the worst
 * available failure, because it defeats the product rather than merely looking
 * bad. The arithmetic says a grid cannot be rescued on this canvas:
 *
 * - The longest name is 28 characters. At the app's 28 sp body step that is
 *   about 409 dp of type, so a one-line label needs a tile about 445 dp wide,
 *   which is two columns at most.
 * - A two-column 16:9 tile is 400 x 225 dp, so two rows of them is 470 dp.
 * - The whole canvas is 540 dp and a header and a way out have to fit too.
 *
 * So two rows of picture-plus-readable-label does not fit a Fire TV, full
 * stop, and a two-line label inside a tile eats the picture it sits on. A full
 * width row gives the name 620 dp -- 43 characters at 28 sp, comfortable at
 * the 32 sp heading step -- and gives the photograph a 16:9 frame beside it.
 *
 * It is also the right distinction to draw. The home wall answers *where do I
 * go*; these screens answer *what shall I watch*. They should not be the same
 * shape, and a real Fire TV does not use one layout for both either.
 *
 * **Focus fills.** At rest a row has no container at all -- a photograph and
 * a name on the ground. Focused, the row becomes a solid `#8FBF83` plate with
 * the name knocked out in charcoal and the photograph's veil lifted. Three
 * redundant channels and not a stroke among them; scale is deliberately not a
 * fourth here, because 5% of an 844 dp row is 42 dp of overhang and a focused
 * row would climb out of the overscan margin.
 */
@Composable
fun ListRow(
    title: String,
    contentDescription: String,
    height: Dp,
    modifier: Modifier = Modifier,
    artwork: Int? = null,
    icon: TileIcon? = null,
    initialFocus: Boolean = false,
    focusRequester: FocusRequester? = null,
    onClick: () -> Unit,
) {
    val focus = if (focusRequester != null) {
        rememberTileFocusState(initialFocus, focusRequester)
    } else {
        rememberTileFocusState(initialFocus)
    }
    val shape = RoundedCornerShape(RADIUS)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(shape)
            .background(if (focus.isFocused) focus.plate else Color.Transparent)
            .focusRequester(focus.focusRequester)
            .onFocusChanged { focus.onFocusChanged(it.isFocused) }
            .clickable(onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(height * 16f / 9f)
                .fillMaxHeight()
                // Rounded on the outer edge only. Rounded on all four, the
                // photograph's right-hand corners bit a notch out of the focus
                // plate where the two met.
                .clip(RoundedCornerShape(topStart = RADIUS, bottomStart = RADIUS))
                .background(Plinth),
            contentAlignment = Alignment.Center,
        ) {
            if (artwork != null) {
                Image(
                    painter = painterResource(artwork),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                // The wash that holds an unfocused picture back, and lifts on
                // focus. On a list this is the channel that does the most work:
                // the eye finds the one bright photograph among four held-back
                // ones before it has read a word.
                Box(modifier = Modifier.fillMaxSize().background(Charcoal.copy(alpha = focus.veil)))
            } else if (icon != null) {
                TileIconGlyph(icon = icon, color = focus.glyphColor, size = height * 0.5f)
            }
        }
        Text(
            text = title,
            color = if (focus.isFocused) focus.onPlate else Cream,
            style = MaterialTheme.typography.headlineSmall,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 24.dp),
        )
    }
}

private val RADIUS = 14.dp
