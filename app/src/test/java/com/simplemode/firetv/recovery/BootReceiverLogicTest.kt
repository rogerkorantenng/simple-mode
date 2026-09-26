package com.simplemode.firetv.recovery

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BootReceiverLogicTest {

    @Test
    fun `restarts the overlay after boot only when the caregiver had it on and permission still holds`() {
        assertTrue(BootReceiver.shouldRestartOverlay(wasEnabledByCaregiver = true, overlayPermissionGranted = true))
    }

    @Test
    fun `never restarts an overlay the caregiver never turned on`() {
        assertFalse(BootReceiver.shouldRestartOverlay(wasEnabledByCaregiver = false, overlayPermissionGranted = true))
    }

    @Test
    fun `never restarts an overlay it is no longer allowed to draw, even if it was on before`() {
        assertFalse(BootReceiver.shouldRestartOverlay(wasEnabledByCaregiver = true, overlayPermissionGranted = false))
    }

    @Test
    fun `never restarts when neither condition holds`() {
        assertFalse(BootReceiver.shouldRestartOverlay(wasEnabledByCaregiver = false, overlayPermissionGranted = false))
    }
}
