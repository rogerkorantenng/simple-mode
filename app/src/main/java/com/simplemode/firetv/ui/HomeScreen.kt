package com.simplemode.firetv.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.simplemode.firetv.accessibility.AccessibilityNeeds
import com.simplemode.firetv.accessibility.UiAdaptation
import com.simplemode.firetv.launch.Tile

// Three columns, not four: a real ten-foot canvas (1920x1080 at Fire TV's
// own density) rewards fewer, bigger targets over cramming more into the
// same row, which is the whole point of "huge" -- see FRICTION.md
// for the phone-screenshot this replaced. Six destinations in two rows of
// three is the whole wall, visible at once, with nothing below the fold.
private const val COLUMNS = 3
// the shared Fire TV craft reference's item 44: at the largest text setting the layout has to change
// rather than only scale. At the captions-on boost, three columns leave a
// tile label too little width and "Prime Video" wraps and clips. Two columns
// give it nearly four hundred dp, which is the point at which the screen is
// still readable rather than merely larger.
private const val COLUMNS_LARGE_TEXT = 2
private const val LARGE_TEXT_THRESHOLD = 1.1f
// Real content margins from the shared Fire TV craft reference's checked grid
// arithmetic, well inside the 48/30 dp overscan floor, and wide enough that
// two rows of 16:9 tiles plus the hero clear the bottom of the screen.
private const val MARGIN_H_DP = 72
private const val GRID_SCALE_CLEARANCE_DP = 10

/**
 * The one screen that carries the demo.
 *
 * Two parts, and a rule between them. A flat warm-charcoal band carrying the
 * app's name, the three controls that are not places to watch something --
 * Always Home, the television's own caption settings, the way through to the
 * family side -- and one line proving the accessibility adaptation is real
 * rather than asserted. Then the wall: six destinations as 16:9 photographs,
 * two rows of three, everything visible without scrolling.
 *
 * There used to be a photograph across the top of the band as well. Deleting
 * it is the largest single change in this app's de-templating; the argument is
 * in [HomeHero].
 *
 * The structure is the point. Simple Mode is for somebody who cannot work a
 * television, so the things she watches are large pictures and the things
 * that configure the television are small words, and the two never look
 * alike. That split is computed in [buildHomeTiles] and [buildHomeNavItems]
 * from each tile's own target, so a caregiver adding a settings shortcut
 * gets it in the strip rather than on the wall.
 *
 * Focus starts on the first tile on the wall -- the shared Fire TV craft reference
 * item 1, "the item the viewer most likely wants" -- not on the controls
 * above it.
 */
@Composable
fun HomeScreen(
    needs: AccessibilityNeeds,
    overlayEnabled: Boolean,
    gridTiles: List<Tile>,
    onTileClick: (Tile) -> Unit,
    onToggleOverlay: () -> Unit,
    onOpenFamily: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // The same reading that drives the status line also drives the layout:
    // captions-on grows text and spacing together (UiAdaptation), and at the
    // large step the wall drops to two columns so a grown label still fits
    // inside its own band.
    val uiScale = UiAdaptation.scaleFor(needs)
    val largeText = uiScale.text >= LARGE_TEXT_THRESHOLD
    val columns = if (largeText) COLUMNS_LARGE_TEXT else COLUMNS
    val tiles = buildHomeTiles(needs, gridTiles, onTileClick)
    val navItems = buildHomeNavItems(
        needs = needs,
        overlayEnabled = overlayEnabled,
        gridTiles = gridTiles,
        onToggleOverlay = onToggleOverlay,
        onTileClick = onTileClick,
        onOpenFamily = onOpenFamily,
    )

    Column(modifier = modifier.fillMaxSize()) {
        HomeHero(
            title = "Simple Mode",
            status = statusLine(needs),
            textScale = uiScale.text,
            // The hero's text lines up with the left edge of the first
            // tile, not with the grid's own margin: the wall is inset a
            // further 10 dp so the focused tile has room to grow, and a
            // title that ignored that read as eight dp of misalignment.
            marginDp = MARGIN_H_DP + GRID_SCALE_CLEARANCE_DP,
        ) {
            navItems.forEach { item ->
                NavItem(
                    label = item.title,
                    contentDescription = item.contentDescription,
                    icon = item.icon,
                    onClick = item.onClick,
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = MARGIN_H_DP.dp)
                .padding(top = 6.dp)
                // Nothing scrolls at the normal text setting. At the large
                // one the wall becomes two columns and three rows, and this
                // is what lets her reach the third.
                .verticalScroll(rememberScrollState()),
        ) {
            TileGrid(tiles = tiles, columns = columns, textScale = uiScale.text)
        }
    }
}

// Two short readings, not two fields joined by a middle dot, and each one says
// what it changed rather than only what it is. The line proves the adaptation
// is real rather than asserted, so it has to name the consequence.
//
// It has to fit on one line at the normal text setting, which is 796 dp at the
// home screen's margins, or about sixty characters at 28sp. Every combination
// below does except the one where both settings are on -- and that is the
// combination that also turns the text scale up and drops the wall to two
// scrolling columns, so the band has somewhere to grow into.
private fun statusLine(needs: AccessibilityNeeds): String {
    val captions = if (needs.captionsEnabled) {
        "Captions on, so this screen is larger."
    } else {
        "Captions off."
    }
    val audioDescription = when (needs.audioDescriptionsEnabled) {
        true -> "Audio description on, so buttons say more."
        false -> "Audio description off."
        null -> "This TV does not report audio description."
    }
    return "$captions $audioDescription"
}
