package com.simplemode.firetv.recovery

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ForegroundTallyTest {

    @Test
    fun `a fresh process has nothing on screen`() {
        val tally = ForegroundTally()
        assertFalse(tally.isOnScreen)
        assertFalse(tally.isInteractive)
    }

    @Test
    fun `starting an activity puts Simple Mode on screen`() {
        val tally = ForegroundTally()
        assertTrue("the answer changed", tally.activityStarted())
        assertTrue(tally.isOnScreen)
    }

    @Test
    fun `stopping the only activity takes Simple Mode off screen`() {
        val tally = ForegroundTally()
        tally.activityStarted()
        assertTrue("the answer changed", tally.activityStopped())
        assertFalse(tally.isOnScreen)
    }

    @Test
    fun `a pause alone never takes Simple Mode off screen`() {
        // Android's Activity docs: a paused activity "is still visible on
        // screen" -- under a transparent or non-full-sized activity, for
        // instance. If a pause counted as leaving, the recovery button would
        // appear on top of Simple Mode's own UI behind a dialog, which is the
        // bug this whole class exists to prevent.
        val tally = ForegroundTally()
        tally.activityStarted()
        tally.activityResumed()

        tally.activityPaused()
        assertTrue("pausing changes nothing about being on screen", tally.isOnScreen)
        assertFalse(tally.isInteractive)
    }

    @Test
    fun `an internal screen change never blinks the overlay on`() {
        // Android overlaps the handoff: the incoming activity starts and
        // resumes before the outgoing one pauses and stops. A boolean cleared
        // on the way out would report "gone" for a frame and flash the
        // recovery button over the app's own UI. The count must not dip.
        val tally = ForegroundTally()
        tally.activityStarted()

        assertFalse("no change when a second activity starts", tally.activityStarted())
        assertTrue(tally.isOnScreen)

        assertFalse("no change when the first one then stops", tally.activityStopped())
        assertTrue(tally.isOnScreen)
    }

    @Test
    fun `an unmatched stop cannot drive the count negative and latch the button off`() {
        val tally = ForegroundTally()
        tally.activityStopped()
        tally.activityStopped()
        assertEquals(0, tally.startedActivities)

        // The next real start must still register, which it could not if the
        // count had gone to -2.
        assertTrue(tally.activityStarted())
        assertTrue(tally.isOnScreen)
    }

    @Test
    fun `an unmatched pause cannot drive the resumed count negative either`() {
        val tally = ForegroundTally()
        tally.activityPaused()
        tally.activityPaused()
        assertEquals(0, tally.resumedActivities)
        assertTrue(tally.activityResumed())
        assertTrue(tally.isInteractive)
    }

    @Test
    fun `leaving for another app and coming back reports both transitions`() {
        val tally = ForegroundTally()
        tally.activityStarted()
        tally.activityResumed()

        // The viewer opens something else: pause, then stop.
        tally.activityPaused()
        assertTrue("left Simple Mode", tally.activityStopped())
        assertFalse(tally.isOnScreen)

        assertTrue("came back", tally.activityStarted())
        tally.activityResumed()
        assertTrue(tally.isOnScreen)
        assertTrue(tally.isInteractive)
        assertEquals(1, tally.startedActivities)
        assertEquals(1, tally.resumedActivities)
    }

    @Test
    fun `on screen is the broader of the two answers`() {
        val tally = ForegroundTally()
        tally.activityStarted()
        tally.activityResumed()
        assertTrue(tally.isOnScreen && tally.isInteractive)

        tally.activityPaused()
        assertTrue("still on screen after a pause", tally.isOnScreen)
        assertFalse("but no longer interactive", tally.isInteractive)
    }
}
