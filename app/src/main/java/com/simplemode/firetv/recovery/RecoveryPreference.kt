package com.simplemode.firetv.recovery

import android.content.Context

/**
 * Whether the caregiver has turned "Always Home" on, persisted across
 * process death.
 *
 * This exists because of a real bug caught during testing: the in-memory
 * toggle state alone is not enough. If Android kills the app's process (a
 * force-stop, a crash, low-memory reclaim), the foreground service and its
 * overlay window die with it, but nothing told the next cold start that the
 * overlay is supposed to come back. Without this, the home screen could
 * show "Always Home: On" right after a relaunch while no button was
 * actually on screen anywhere -- silently breaking the one promise this
 * app makes. [MainActivity][com.simplemode.firetv.MainActivity] re-asserts
 * the service in `onResume` whenever this flag is true and the permission
 * is still granted, and [BootReceiver] checks the same flag before
 * restarting after a reboot.
 */
object RecoveryPreference {
    private const val PREFS_NAME = "recovery"
    private const val KEY_ENABLED = "always_home_enabled"

    fun isEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ENABLED, false)

    fun setEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_ENABLED, enabled).apply()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
