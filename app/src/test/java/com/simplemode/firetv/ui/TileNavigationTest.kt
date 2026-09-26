package com.simplemode.firetv.ui

import com.simplemode.firetv.launch.InAppScreen
import com.simplemode.firetv.launch.LaunchTarget
import com.simplemode.firetv.launch.PackageResolver
import com.simplemode.firetv.launch.Tile
import org.junit.Assert.assertEquals
import org.junit.Test

class TileNavigationTest {

    private val alwaysInstalled = PackageResolver { true }
    private val neverInstalled = PackageResolver { false }

    @Test
    fun `an in-app tile navigates to its screen`() {
        val tile = Tile("live_tv", "Live TV", "Watch.", LaunchTarget.InApp(InAppScreen.LIVE_GUIDE))
        assertEquals(TileOutcome.GoTo(Screen.LiveGuide), resolveTileOutcome(tile, alwaysInstalled))
    }

    @Test
    fun `tell me routes to the tell me screen`() {
        val tile = Tile("tell_me", "Tell Me", "Say it.", LaunchTarget.InApp(InAppScreen.TELL_ME))
        assertEquals(TileOutcome.GoTo(Screen.TellMe), resolveTileOutcome(tile, alwaysInstalled))
    }

    @Test
    fun `an installed external app launches`() {
        val tile = Tile("netflix", "Netflix", "Open.", LaunchTarget.ExternalApp("com.netflix.ninja"))
        assertEquals(TileOutcome.LaunchExternal("com.netflix.ninja"), resolveTileOutcome(tile, alwaysInstalled))
    }

    @Test
    fun `an uninstalled external app shows not available, never crashes`() {
        val tile = Tile("netflix", "Netflix", "Open.", LaunchTarget.ExternalApp("com.netflix.ninja"))
        val outcome = resolveTileOutcome(tile, neverInstalled)
        assertEquals(TileOutcome.ShowNotAvailable("Netflix is not set up on this TV yet."), outcome)
    }

    @Test
    fun `a system setting opens that setting`() {
        val tile = Tile("captions_and_sound", "Captions", "Open.", LaunchTarget.SystemSetting("android.settings.ACCESSIBILITY_SETTINGS"))
        assertEquals(TileOutcome.OpenSetting("android.settings.ACCESSIBILITY_SETTINGS"), resolveTileOutcome(tile, alwaysInstalled))
    }
}
