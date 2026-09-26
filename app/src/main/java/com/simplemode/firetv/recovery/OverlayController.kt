package com.simplemode.firetv.recovery

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

/**
 * Owns the one piece of state the whole app is judged on: is the recovery
 * overlay actually showing. Pulled out of [com.simplemode.firetv.MainActivity]
 * so that Activity stays a thin lifecycle shell and this class stays a
 * single, testable answer to "what does Always Home mean right now."
 *
 * The displayed state is always re-derived from the persisted preference
 * plus a live permission check (see [refresh]) rather than trusted from
 * memory -- that is the fix for a real bug found during testing, where a
 * force-stop killed the foreground service but left the toggle showing on.
 */
class OverlayController(private val activity: Activity) {

    private val enabledState: MutableState<Boolean> = mutableStateOf(false)
    val enabled: MutableState<Boolean> get() = enabledState

    /** Call on every resume: catches a caregiver returning from the system
     *  permission screen, and re-asserts the service after any process
     *  death. */
    fun refresh() {
        val granted = OverlayPermission.isGranted(activity)
        val wantsOverlay = RecoveryPreference.isEnabled(activity)
        if (granted && wantsOverlay) {
            RecoveryOverlayService.start(activity)
        }
        enabledState.value = granted && wantsOverlay
    }

    fun toggle() {
        if (enabledState.value) {
            RecoveryPreference.setEnabled(activity, false)
            activity.stopService(Intent(activity, RecoveryOverlayService::class.java))
            enabledState.value = false
            return
        }
        // Recorded as "wanted on" immediately, even before the permission
        // check below settles, so refresh() can finish the job once the
        // caregiver returns from the system permission screen.
        RecoveryPreference.setEnabled(activity, true)
        if (OverlayPermission.isGranted(activity)) {
            RecoveryOverlayService.start(activity)
            enabledState.value = true
        } else {
            requestPermission()
        }
    }

    private fun requestPermission() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${activity.packageName}"),
        )
        try {
            activity.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(activity, "This TV has no overlay permission screen.", Toast.LENGTH_LONG).show()
        }
    }
}
