package com.simplemode.firetv.recovery

/**
 * Counts how many of this app's own activities are started and how many are
 * resumed.
 *
 * This is the whole of the foreground question that Fire OS will actually
 * answer for a third party. `ActivityManager.getRunningTasks` has been
 * unavailable to third-party apps since API 21 and its own reference page
 * says it "should never be used for core logic in an application";
 * `UsageStatsManager` needs `PACKAGE_USAGE_STATS`, a Settings-granted special
 * access whose screen AOSP's TV Settings did not even carry until Android 11,
 * one release after Fire OS 7's base. Neither is needed here: the overlay does
 * not have to know *which* app is in front, only whether Simple Mode is on
 * screen. An app always knows that about itself, for free, from its own
 * lifecycle callbacks. See FRICTION.md for the sources.
 *
 * **Two counts, because `onPause` is not "gone".** Android's own `Activity`
 * documentation is explicit that a paused activity "is still visible on
 * screen", and that an activity rests in paused state under a transparent or
 * non-full-sized activity. Treating a pause as a departure would put the
 * recovery button on top of Simple Mode's own screen behind a dialog -- a
 * smaller version of the bug this class exists to fix. `onStop` is the
 * documented "no longer visible to the user", so [isOnScreen] is what the
 * overlay acts on and [isInteractive] is kept for completeness.
 *
 * Counting rather than holding a boolean matters because Android overlaps the
 * handoff between two activities: the incoming one starts and resumes before
 * the outgoing one pauses and stops. A boolean cleared on the way out would
 * blink the overlay on during every internal screen change; a count never
 * dips to zero unless the app really did leave.
 *
 * Each mutator returns whether the answer it governs changed -- [isOnScreen]
 * for start/stop, [isInteractive] for resume/pause -- so callers can act on
 * transitions and ignore the churn in between.
 */
class ForegroundTally {

    var startedActivities: Int = 0
        private set

    var resumedActivities: Int = 0
        private set

    /** Simple Mode has something on screen. The overlay's condition. */
    val isOnScreen: Boolean get() = startedActivities > 0

    /** Simple Mode has the viewer's input as well as the screen. */
    val isInteractive: Boolean get() = resumedActivities > 0

    fun activityStarted(): Boolean {
        val before = isOnScreen
        startedActivities += 1
        return isOnScreen != before
    }

    /**
     * Floored at zero, like [activityPaused]. A stop without a matching start
     * is not supposed to happen, but a process restart and an `onActivityStopped`
     * for an activity that started before this tally existed both produce one,
     * and a negative count would latch the overlay off forever.
     */
    fun activityStopped(): Boolean {
        val before = isOnScreen
        if (startedActivities > 0) startedActivities -= 1
        return isOnScreen != before
    }

    fun activityResumed(): Boolean {
        val before = isInteractive
        resumedActivities += 1
        return isInteractive != before
    }

    fun activityPaused(): Boolean {
        val before = isInteractive
        if (resumedActivities > 0) resumedActivities -= 1
        return isInteractive != before
    }
}
