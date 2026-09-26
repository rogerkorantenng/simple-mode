package com.simplemode.firetv.ui

import com.simplemode.firetv.launch.InAppScreen
import com.simplemode.firetv.launch.LaunchResult
import com.simplemode.firetv.launch.LaunchTarget
import com.simplemode.firetv.launch.PackageResolver
import com.simplemode.firetv.launch.Tile
import com.simplemode.firetv.launch.TileResolution

/** What pressing a tile (from the grid, or matched by Tell Me) should do
 *  to the app: change screen, leave the app, or open a system settings
 *  page. Kept separate from any Compose state so it is a pure, directly
 *  testable function -- the same tile-tapping logic Tell Me's matches
 *  reuse rather than duplicate. */
sealed interface TileOutcome {
    data class GoTo(val screen: Screen) : TileOutcome
    data class LaunchExternal(val packageName: String) : TileOutcome
    data class OpenSetting(val action: String) : TileOutcome
    data class ShowNotAvailable(val message: String) : TileOutcome
}

fun resolveTileOutcome(tile: Tile, resolver: PackageResolver): TileOutcome =
    when (val result = TileResolution.resolve(tile, resolver)) {
        is LaunchResult.NotAvailable -> TileOutcome.ShowNotAvailable(result.message)
        LaunchResult.Launched -> when (val target = tile.target) {
            is LaunchTarget.InApp -> TileOutcome.GoTo(
                when (target.screen) {
                    InAppScreen.LIVE_GUIDE -> Screen.LiveGuide
                    InAppScreen.HER_SHOWS -> Screen.HerShows
                    InAppScreen.FILMS -> Screen.Films
                    InAppScreen.BOX_SETS -> Screen.BoxSets
                    InAppScreen.FAMILY_VIDEOS -> Screen.FamilyVideos
                    InAppScreen.CALL_FOR_HELP -> Screen.CallForHelp
                    InAppScreen.TELL_ME -> Screen.TellMe
                },
            )
            is LaunchTarget.ExternalApp -> TileOutcome.LaunchExternal(target.packageName)
            is LaunchTarget.SystemSetting -> TileOutcome.OpenSetting(target.action)
        }
    }
