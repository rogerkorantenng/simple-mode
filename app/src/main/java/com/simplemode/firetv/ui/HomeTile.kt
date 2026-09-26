package com.simplemode.firetv.ui

import androidx.annotation.DrawableRes
import com.simplemode.firetv.accessibility.AccessibilityNeeds
import com.simplemode.firetv.accessibility.UiAdaptation
import com.simplemode.firetv.launch.LaunchTarget
import com.simplemode.firetv.launch.Tile

/** One rendered tile on the home wall: display-ready, with no further
 *  Android or accessibility lookups needed at draw time. */
data class HomeTile(
    val title: String,
    @DrawableRes val artwork: Int,
    val contentDescription: String,
    val onClick: () -> Unit,
)

/** One control in the strip that floats on the hero. */
data class HomeNavItem(
    val title: String,
    val icon: TileIcon,
    val contentDescription: String,
    val onClick: () -> Unit,
)

/**
 * Splits the caregiver's own chosen and ordered tiles (see
 * `caregiver/TileRepository.kt`) into the two things the home screen draws.
 *
 * A destination she watches goes on the wall as a picture. A control -- the
 * Always Home toggle, the shortcut into the television's own caption
 * settings, the way through to the family side -- goes in the strip as a
 * word. They are different kinds of thing and the earlier build drew them
 * identically, which is why the wall had eight items on it, would not fit on
 * one screen, and made "open the accessibility settings" look like something
 * to watch.
 *
 * The split is read off the tile's own target rather than a hand-kept list,
 * so a caregiver who adds a settings shortcut gets it in the right place
 * without anyone editing this file.
 */
fun buildHomeTiles(
    needs: AccessibilityNeeds,
    gridTiles: List<Tile>,
    onTileClick: (Tile) -> Unit,
): List<HomeTile> = gridTiles
    .filterNot { it.target is LaunchTarget.SystemSetting }
    .map { tile ->
        HomeTile(
            title = tile.title,
            artwork = tileArtworkFor(tile),
            contentDescription = UiAdaptation.tileContentDescription(tile.title, tile.shortDescription, needs),
            onClick = { onTileClick(tile) },
        )
    }

fun buildHomeNavItems(
    needs: AccessibilityNeeds,
    overlayEnabled: Boolean,
    gridTiles: List<Tile>,
    onToggleOverlay: () -> Unit,
    onTileClick: (Tile) -> Unit,
    onOpenFamily: () -> Unit,
): List<HomeNavItem> = buildList {
    add(
        HomeNavItem(
            title = "Always Home",
            icon = if (overlayEnabled) TileIcon.HomeOn else TileIcon.Home,
            contentDescription = if (overlayEnabled) {
                "Always Home is on. A button stays on screen everywhere to bring you back here."
            } else {
                "Always Home is off. Turn it on so a button stays on screen everywhere and brings you back here."
            },
            onClick = onToggleOverlay,
        ),
    )
    gridTiles
        .filter { it.target is LaunchTarget.SystemSetting }
        .forEach { tile ->
            add(
                HomeNavItem(
                    title = tile.title,
                    icon = TileIcon.Captions,
                    contentDescription = UiAdaptation.tileContentDescription(tile.title, tile.shortDescription, needs),
                    onClick = { onTileClick(tile) },
                ),
            )
        }
    add(
        HomeNavItem(
            title = "Family setup",
            icon = TileIcon.Lock,
            contentDescription = "Family setup. Watch history, Tell Me's answers, and the PIN-protected home screen editor.",
            onClick = onOpenFamily,
        ),
    )
}
