package com.simplemode.firetv.recovery

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OverlayRecedeTest {

    @Test
    fun `arrives at full size and full opacity`() {
        assertEquals(OverlayRecede.FULL_ALPHA, OverlayRecede.alphaAt(0L), 0f)
        assertEquals(OverlayRecede.FULL_SCALE, OverlayRecede.scaleAt(0L), 0f)
    }

    @Test
    fun `stays prominent for the whole dwell, so a slow reader can find it`() {
        val justBefore = OverlayRecede.DWELL_MS - 1
        assertEquals(OverlayRecede.FULL_ALPHA, OverlayRecede.alphaAt(justBefore), 0f)
        assertEquals(OverlayRecede.FULL_SCALE, OverlayRecede.scaleAt(justBefore), 0f)
    }

    @Test
    fun `recedes once the dwell is over, so it stops covering the picture`() {
        assertEquals(OverlayRecede.RECEDED_ALPHA, OverlayRecede.alphaAt(OverlayRecede.DWELL_MS), 0f)
        assertEquals(OverlayRecede.RECEDED_SCALE, OverlayRecede.scaleAt(OverlayRecede.DWELL_MS), 0f)
        assertEquals(OverlayRecede.RECEDED_ALPHA, OverlayRecede.alphaAt(10 * 60_000L), 0f)
    }

    @Test
    fun `receded is smaller and fainter, but never invisible`() {
        assertTrue(OverlayRecede.RECEDED_SCALE < OverlayRecede.FULL_SCALE)
        assertTrue(OverlayRecede.RECEDED_ALPHA < OverlayRecede.FULL_ALPHA)
        // A viewer who cannot see it cannot use it. Keep a real floor rather
        // than letting a later tweak fade it to nothing.
        assertTrue(OverlayRecede.RECEDED_ALPHA >= 0.35f)
        assertTrue(OverlayRecede.RECEDED_SCALE >= 0.5f)
    }

    @Test
    fun `the dwell is long enough to be read and short enough not to sit on the film`() {
        assertTrue(OverlayRecede.DWELL_MS >= 4_000L)
        assertTrue(OverlayRecede.DWELL_MS <= 15_000L)
    }

    @Test
    fun `a button that just appeared waits the full dwell`() {
        assertEquals(OverlayRecede.DWELL_MS, OverlayRecede.delayUntilRecede(0L))
    }

    @Test
    fun `a button already up waits only the remainder`() {
        assertEquals(1_000L, OverlayRecede.delayUntilRecede(OverlayRecede.DWELL_MS - 1_000L))
    }

    @Test
    fun `never schedules into the past`() {
        assertEquals(0L, OverlayRecede.delayUntilRecede(OverlayRecede.DWELL_MS))
        assertEquals(0L, OverlayRecede.delayUntilRecede(60_000L))
    }
}
