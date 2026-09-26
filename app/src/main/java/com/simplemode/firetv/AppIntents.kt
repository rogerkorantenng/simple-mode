package com.simplemode.firetv

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.activity.ComponentActivity

/**
 * The two ways a tile leaves this app entirely: another installed app, or a
 * real Fire OS settings screen. Both can fail -- a caregiver's actual
 * television will not have everything installed, and this app does not control
 * which settings screens a given Fire OS version ships -- so both report
 * failure rather than throwing.
 *
 * **They used to report it with a `Toast`.** A toast is a phone control: Android
 * draws it as a small grey pill at phone type size, low on the screen, for a
 * couple of seconds. On a television three metres away it is unreadable, it is
 * visibly not part of this app, and it is gone before somebody who reads slowly
 * has finished. Simple Mode already has a full-screen way of saying this -- see
 * `NotAvailableScreen` -- so these return a boolean and the caller shows it.
 *
 * @return true if the app or settings screen actually opened.
 */
fun ComponentActivity.launchExternalApp(packageName: String): Boolean {
    val intent = packageManager.getLaunchIntentForPackage(packageName) ?: return false
    startActivity(intent)
    return true
}

fun ComponentActivity.openSystemSetting(action: String): Boolean = try {
    startActivity(Intent(action))
    true
} catch (_: ActivityNotFoundException) {
    false
}
