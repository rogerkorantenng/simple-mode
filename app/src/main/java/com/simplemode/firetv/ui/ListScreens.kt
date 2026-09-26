package com.simplemode.firetv.ui

/**
 * The five places that are a list of names rather than a wall of pictures.
 *
 * Each is the same screen with different contents, so the screen itself
 * lives in [ListScreen] and these are only the five entry points AppRoot
 * routes to by name. Splitting them apart keeps the shared behaviour --
 * where focus lands, how the way out is reached -- in one place, so fixing
 * it once fixes it for all five.
 */

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.simplemode.firetv.Viewer

@Composable
fun LiveGuideScreen(onSelectChannel: (String) -> Unit, onHome: () -> Unit, modifier: Modifier = Modifier) {
    ListScreen(
        title = "Live TV",
        items = SampleContent.channels,
        icon = TileIcon.Screen,
        onSelect = onSelectChannel,
        onHome = onHome,
        modifier = modifier,
    )
}

@Composable
fun HerShowsScreen(onSelectShow: (String) -> Unit, onHome: () -> Unit, modifier: Modifier = Modifier) {
    ListScreen(
        title = "${Viewer.POSSESSIVE} Shows",
        items = SampleContent.shows,
        icon = TileIcon.Bookmark,
        onSelect = onSelectShow,
        onHome = onHome,
        modifier = modifier,
    )
}

@Composable
fun FilmsScreen(onSelectFilm: (String) -> Unit, onHome: () -> Unit, modifier: Modifier = Modifier) {
    ListScreen(
        title = "Films",
        items = SampleContent.films,
        icon = TileIcon.Play,
        onSelect = onSelectFilm,
        onHome = onHome,
        modifier = modifier,
    )
}

@Composable
fun BoxSetsScreen(onSelectSeries: (String) -> Unit, onHome: () -> Unit, modifier: Modifier = Modifier) {
    ListScreen(
        title = "Box Sets",
        items = SampleContent.boxSets,
        icon = TileIcon.Bookmark,
        onSelect = onSelectSeries,
        onHome = onHome,
        modifier = modifier,
    )
}

@Composable
fun FamilyVideosScreen(onSelectClip: (String) -> Unit, onHome: () -> Unit, modifier: Modifier = Modifier) {
    ListScreen(
        title = "Family Videos",
        items = SampleContent.familyVideos,
        icon = TileIcon.Play,
        onSelect = onSelectClip,
        onHome = onHome,
        modifier = modifier,
    )
}
