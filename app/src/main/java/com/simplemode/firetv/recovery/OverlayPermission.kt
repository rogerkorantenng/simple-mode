package com.simplemode.firetv.recovery

import android.content.Context
import android.provider.Settings

/**
 * Whether this app can draw its floating recovery button over whatever else
 * is on screen. `SYSTEM_ALERT_WINDOW` is a normal, non-signature Android
 * permission -- the caregiver grants it once, from Settings, during setup,
 * not something the low-vision or low-dexterity viewer ever has to touch.
 */
object OverlayPermission {
    fun isGranted(context: Context): Boolean = Settings.canDrawOverlays(context)
}
