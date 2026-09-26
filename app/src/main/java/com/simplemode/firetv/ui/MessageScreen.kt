package com.simplemode.firetv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.simplemode.firetv.ui.theme.Cream
import com.simplemode.firetv.ui.theme.Plinth

/**
 * Shared layout for every screen that is just "a short message and the one way
 * back" -- three of Simple Mode's screens are this shape, so the shape lives
 * once.
 *
 * **It used to have two holes in it.** an internal review of this batch's three Fire TV apps together names
 * `simple-mode-not-available.png` as the app's weakest frame: "Header band,
 * then roughly 260px of black, then one sentence, then roughly 250px of black,
 * then a full-width button hard against the bottom edge. Nothing anchors the
 * sentence to anything... Empty space below content reads as finished; empty
 * space *around* content reads as broken."
 *
 * Both holes came from one decision -- centring the sentence in whatever was
 * left over -- and both are closed by dropping it. The message now sits
 * directly under the fern rule in the same `Plinth` panel the caregiver's
 * catalogue rows use, the way out sits under the message with one gap, and
 * whatever is left over is left over at the bottom, where it reads as a screen
 * that has finished rather than as one that failed to load.
 *
 * Both the panel and the button are 620 dp, which is a measure of about 35
 * characters at 28sp -- the low end of the range the shared Fire TV craft reference
 * gives this app -- and matching widths turn two elements into one block.
 *
 * The `titleColor` parameter every caller used to pass is gone. [ScreenHeader]
 * has always set the title in `FernTitle` regardless, so the three call sites
 * were arguing about a colour nothing read.
 *
 * This is the one screen shape that does *not* also float a Home chip on the
 * header band: a dead end with a single sentence on it needs one obvious way
 * out, and two controls saying the same words read as a mistake rather than as
 * a choice.
 */
@Composable
internal fun MessageScreen(
    title: String,
    detail: String,
    onHome: () -> Unit,
    modifier: Modifier = Modifier,
    body: String? = null,
) {
    val home = remember { FocusRequester() }

    Column(modifier = modifier.fillMaxSize()) {
        ScreenHeader(title = title)
        Column(
            modifier = Modifier
                .padding(horizontal = 58.dp)
                .padding(top = 22.dp, bottom = 30.dp)
                // Nothing on these three screens is long enough to need this,
                // and it is here so that nothing ever can be: a paragraph that
                // grows by one line must not be able to take the only control
                // on the screen off the bottom of the television with it.
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            if (body != null) {
                Text(
                    text = body,
                    color = Cream,
                    style = MaterialTheme.typography.headlineLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.width(MESSAGE_COLUMN),
                )
            }
            Box(
                modifier = Modifier
                    .width(MESSAGE_COLUMN)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Plinth)
                    .padding(horizontal = 26.dp, vertical = 22.dp),
            ) {
                Text(text = detail, color = Cream, style = MaterialTheme.typography.bodyLarge)
            }
            Box(modifier = Modifier.width(MESSAGE_COLUMN)) {
                FocusableRow(
                    label = "Back to Home",
                    contentDescription = "Back to the Simple Mode home screen.",
                    icon = TileIcon.Home,
                    prominent = true,
                    initialFocus = true,
                    focusRequester = home,
                    onClick = onHome,
                )
            }
        }
    }
}

// About 35 characters a line at the reading size, which is the measure
// the shared Fire TV craft reference gives this app. Run full bleed across the 844 dp safe
// width these paragraphs were 62 characters a line and read as a wall from the
// sofa.
private val MESSAGE_COLUMN = 620.dp
