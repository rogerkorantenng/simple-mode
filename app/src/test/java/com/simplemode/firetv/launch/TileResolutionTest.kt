package com.simplemode.firetv.launch

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TileResolutionTest {

    private val installedApps = setOf("com.netflix.ninja")
    private val resolver = PackageResolver { pkg -> pkg in installedApps }

    @Test
    fun `an installed external app launches`() {
        val tile = Tile("netflix", "Netflix", "Open Netflix.", LaunchTarget.ExternalApp("com.netflix.ninja"))
        assertEquals(LaunchResult.Launched, TileResolution.resolve(tile, resolver))
    }

    @Test
    fun `an app the caregiver never installed fails gracefully, never crashes`() {
        val tile = Tile(
            "prime_video",
            "Prime Video",
            "Open Prime Video.",
            LaunchTarget.ExternalApp("com.amazon.avod.thirdpartyclient"),
        )
        val result = TileResolution.resolve(tile, resolver)
        assertTrue(result is LaunchResult.NotAvailable)
        assertEquals(
            "Prime Video is not set up on this TV yet.",
            (result as LaunchResult.NotAvailable).message,
        )
    }

    @Test
    fun `an in-app screen always launches, since Simple Mode hosts it itself`() {
        val tile = Tile("live_tv", "Live TV", "Watch a channel.", LaunchTarget.InApp(InAppScreen.LIVE_GUIDE))
        assertEquals(LaunchResult.Launched, TileResolution.resolve(tile, resolver))
    }

    @Test
    fun `a system setting always launches`() {
        val tile = Tile(
            "captions",
            "Captions & Sound",
            "Open settings.",
            LaunchTarget.SystemSetting("android.settings.ACCESSIBILITY_SETTINGS"),
        )
        assertEquals(LaunchResult.Launched, TileResolution.resolve(tile, resolver))
    }
}
