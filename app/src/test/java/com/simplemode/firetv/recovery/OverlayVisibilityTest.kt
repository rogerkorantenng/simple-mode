package com.simplemode.firetv.recovery

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The regression these exist for: the overlay button used to be added in
 * `onCreate` and removed in `onDestroy`, so it sat on screen permanently --
 * over Simple Mode's own home grid, and on a television over whatever the
 * viewer was watching. Every assertion below is one way that could come back.
 */
class OverlayVisibilityTest {

    @Test
    fun `shows the way back when the viewer has wandered out of Simple Mode`() {
        assertTrue(
            OverlayVisibility.shouldShowButton(
                alwaysHomeEnabled = true,
                overlayPermissionGranted = true,
                simpleModeOnScreen = false,
            ),
        )
    }

    @Test
    fun `hides while Simple Mode is already on screen, which is the bug this replaces`() {
        assertFalse(
            OverlayVisibility.shouldShowButton(
                alwaysHomeEnabled = true,
                overlayPermissionGranted = true,
                simpleModeOnScreen = true,
            ),
        )
    }

    @Test
    fun `never shows when the caregiver has not turned Always Home on`() {
        assertFalse(
            OverlayVisibility.shouldShowButton(
                alwaysHomeEnabled = false,
                overlayPermissionGranted = true,
                simpleModeOnScreen = false,
            ),
        )
    }

    @Test
    fun `never shows once the caregiver revokes draw-over-other-apps`() {
        assertFalse(
            OverlayVisibility.shouldShowButton(
                alwaysHomeEnabled = true,
                overlayPermissionGranted = false,
                simpleModeOnScreen = false,
            ),
        )
    }

    @Test
    fun `being on screen beats every other reason to show it`() {
        // Belt and braces: whatever else is true, the button must not appear
        // over the app's own screens.
        for (enabled in listOf(true, false)) {
            for (granted in listOf(true, false)) {
                assertFalse(
                    "enabled=$enabled granted=$granted",
                    OverlayVisibility.shouldShowButton(
                        alwaysHomeEnabled = enabled,
                        overlayPermissionGranted = granted,
                        simpleModeOnScreen = true,
                    ),
                )
            }
        }
    }

    @Test
    fun `shows only for the one combination that means a lost viewer`() {
        var shown = 0
        for (enabled in listOf(true, false)) {
            for (granted in listOf(true, false)) {
                for (onScreen in listOf(true, false)) {
                    if (OverlayVisibility.shouldShowButton(enabled, granted, onScreen)) shown += 1
                }
            }
        }
        org.junit.Assert.assertEquals(1, shown)
    }
}
