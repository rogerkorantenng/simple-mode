package com.simplemode.firetv

import android.app.Application
import com.simplemode.firetv.recovery.ForegroundWatcher

/**
 * Exists for one reason: something has to start counting resumed activities
 * before anything else runs, so the recovery overlay knows whether Simple
 * Mode is in front from the first moment it is asked.
 *
 * `RecoveryOverlayService` can be started by a caregiver's toggle, by
 * `MainActivity.onResume` re-asserting itself after process death, or by
 * `BootReceiver` with no activity on screen at all. Registering the watcher
 * from the Application means all three see the same, already-correct answer
 * instead of each having to bootstrap its own.
 */
class SimpleModeApp : Application() {

    val foreground = ForegroundWatcher()

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(foreground)
    }
}
