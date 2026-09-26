package com.simplemode.firetv.recovery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Best-effort restart of the recovery overlay after a reboot.
 *
 * `RECEIVE_BOOT_COMPLETED` is plain Android, documented by Google, not by
 * Amazon -- the Fire TV docs are silent on launch-on-boot entirely (see
 * internal research into Fire TV's own documentation, section 2: no entry in the Fire
 * TV docs nav, and /docs/fire-tv/launch-on-boot.html 404s). Many OEM Android
 * skins throttle or block third-party boot receivers to save power, and
 * nothing in Amazon's own material says whether Fire OS does. This receiver
 * is therefore treated as a bonus, never the promise: [shouldRestartOverlay]
 * is the honest condition, "restart only if a caregiver already turned
 * Always Home on and the permission is still granted," which is exactly
 * what makes this logic testable without a device at all.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        if (shouldRestartOverlay(RecoveryPreference.isEnabled(context), OverlayPermission.isGranted(context))) {
            RecoveryOverlayService.start(context)
        }
    }

    companion object {
        /** Pure so it is unit-testable without a BroadcastReceiver instance. */
        fun shouldRestartOverlay(wasEnabledByCaregiver: Boolean, overlayPermissionGranted: Boolean): Boolean =
            wasEnabledByCaregiver && overlayPermissionGranted
    }
}
