package com.simplemode.firetv.recovery

import android.app.Activity
import android.app.Application
import android.os.Bundle

/**
 * Turns this app's own activity lifecycle into one boolean the recovery
 * overlay can act on, with no permission of any kind.
 *
 * Registered once from [com.simplemode.firetv.SimpleModeApp], which means it
 * is counting from process start -- before [RecoveryOverlayService] exists,
 * and whether or not the service is ever started. The service reads
 * [isOnScreen] when it starts and then listens for changes.
 *
 * `Application.ActivityLifecycleCallbacks` documents its methods only as
 * relays ("Called when the Activity calls `super.onStop()`"), so the meaning
 * comes from `Activity` itself: `onStop` is "no longer visible to the user",
 * while `onPause` explicitly still means visible. [ForegroundTally] holds that
 * distinction and the arithmetic; this class is only the Android plumbing
 * around it.
 *
 * All callbacks arrive on the main thread, and so does every service
 * lifecycle method that reads this, so the counts need no synchronisation.
 */
class ForegroundWatcher : Application.ActivityLifecycleCallbacks {

    private val tally = ForegroundTally()
    private val listeners = mutableListOf<(Boolean) -> Unit>()

    /** Whether any of this app's activities is currently visible. */
    val isOnScreen: Boolean get() = tally.isOnScreen

    /** Fires on transitions only, and fires once immediately with the
     *  current answer so a new subscriber never has to guess. */
    fun addListener(listener: (Boolean) -> Unit) {
        listeners += listener
        listener(tally.isOnScreen)
    }

    fun removeListener(listener: (Boolean) -> Unit) {
        listeners -= listener
    }

    override fun onActivityStarted(activity: Activity) {
        if (tally.activityStarted()) publish()
    }

    override fun onActivityStopped(activity: Activity) {
        if (tally.activityStopped()) publish()
    }

    // Tracked but not published: a pause is not a departure, and the overlay
    // must not appear over Simple Mode's own screen behind a dialog.
    override fun onActivityResumed(activity: Activity) {
        tally.activityResumed()
    }

    override fun onActivityPaused(activity: Activity) {
        tally.activityPaused()
    }

    private fun publish() {
        val value = tally.isOnScreen
        // Copied because a listener may remove itself while being called.
        listeners.toList().forEach { it(value) }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
}
