package com.simplemode.firetv.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.simplemode.firetv.accessibility.AccessibilityNeeds
import com.simplemode.firetv.caregiver.TileRepository
import com.simplemode.firetv.family.WatchHistoryLog
import com.simplemode.firetv.intent.IntentResolver
import com.simplemode.firetv.launch.PackageResolver
import com.simplemode.firetv.launch.Tile

/**
 * The whole navigation graph, deliberately small: everything a viewer with
 * no working knowledge of a television needs is one tap from Home, and
 * "Home" is always reachable by the same button wherever they land.
 * Tile-tap logic lives in [resolveTileOutcome], reused by both the grid and
 * a Tell Me match, so the two paths cannot drift apart. The home grid's
 * own contents come from [TileRepository], the caregiver's chosen order,
 * not a fixed list.
 */
@Composable
fun AppRoot(
    needs: AccessibilityNeeds,
    overlayEnabled: Boolean,
    resolver: PackageResolver,
    intentResolver: IntentResolver,
    onToggleOverlay: () -> Unit,
    /** @return true if the app actually opened. */
    onLaunchExternal: (String) -> Boolean,
    /** @return true if the settings screen actually opened. */
    onOpenSystemSetting: (String) -> Boolean,
    goHomeSignal: Int,
) {
    var screen by remember { mutableStateOf<Screen>(Screen.Home) }
    val context = LocalContext.current

    // A press of the recovery overlay's button, or a fresh cold start,
    // always lands on Home -- not wherever the viewer last was.
    LaunchedEffect(goHomeSignal) {
        screen = Screen.Home
    }

    fun goHome() { screen = Screen.Home }

    // Back always goes up one level; in this app every screen's one level
    // up is Home, matching the visible Home tile every screen already
    // carries -- deep breadcrumb navigation would fight the app's own
    // "one button always gets you back" premise. From Home itself this
    // handler is disabled, so Back does the platform default and exits to
    // the Fire TV launcher rather than trapping the viewer.
    BackHandler(enabled = screen != Screen.Home) { goHome() }

    fun applyOutcome(outcome: TileOutcome) {
        when (outcome) {
            is TileOutcome.ShowNotAvailable -> screen = Screen.NotAvailable(outcome.message)
            is TileOutcome.GoTo -> screen = outcome.screen
            // A failure here gets the app's own full-screen message, not a
            // toast. See AppIntents.kt.
            is TileOutcome.LaunchExternal ->
                if (!onLaunchExternal(outcome.packageName)) {
                    screen = Screen.NotAvailable("That app is not set up on this TV yet.")
                }
            is TileOutcome.OpenSetting ->
                if (!onOpenSystemSetting(outcome.action)) {
                    screen = Screen.NotAvailable("This TV does not have a settings screen for that.")
                }
        }
    }

    fun handleTile(tile: Tile) = applyOutcome(resolveTileOutcome(tile, resolver))

    fun selectWatched(label: String) {
        WatchHistoryLog.record(context, label, System.currentTimeMillis())
        screen = Screen.NowPlaying(label)
    }

    // The screen swap used to be a raw `when` with no transition wrapper: an
    // instant cut on every navigation. See ScreenTransition.kt for the timing
    // and why a fade rather than a slide.
    val animated = screenAnimationsEnabled()
    AnimatedContent(
        targetState = screen,
        transitionSpec = screenCrossfade(animated),
        label = "screen",
    ) { current ->
        when (current) {
            Screen.Home -> HomeScreen(
                needs = needs,
                overlayEnabled = overlayEnabled,
                gridTiles = TileRepository.currentGridTiles(context),
                onTileClick = ::handleTile,
                onToggleOverlay = onToggleOverlay,
                onOpenFamily = { screen = Screen.Family },
            )
            Screen.LiveGuide -> LiveGuideScreen(onSelectChannel = ::selectWatched, onHome = ::goHome)
            Screen.HerShows -> HerShowsScreen(onSelectShow = ::selectWatched, onHome = ::goHome)
            Screen.Films -> FilmsScreen(onSelectFilm = ::selectWatched, onHome = ::goHome)
            Screen.BoxSets -> BoxSetsScreen(onSelectSeries = ::selectWatched, onHome = ::goHome)
            Screen.FamilyVideos -> FamilyVideosScreen(onSelectClip = ::selectWatched, onHome = ::goHome)
            Screen.CallForHelp -> CallForHelpScreen(onHome = ::goHome)
            Screen.TellMe -> TellMeRoute(resolver = intentResolver, onMatched = ::handleTile, onHome = ::goHome)
            Screen.Family -> FamilyRoute(
                needs = needs,
                onEditCatalogue = { screen = Screen.CaregiverPin },
                onHome = ::goHome,
            )
            Screen.CaregiverPin -> CaregiverPinScreen(
                onUnlocked = { screen = Screen.CaregiverCatalog },
                onHome = ::goHome,
            )
            Screen.CaregiverCatalog -> CaregiverCatalogScreen(onHome = ::goHome)
            is Screen.NowPlaying -> NowPlayingScreen(label = current.label, onHome = ::goHome)
            is Screen.NotAvailable -> NotAvailableScreen(message = current.message, onHome = ::goHome)
        }
    }
}
