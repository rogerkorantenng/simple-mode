package com.simplemode.firetv.family

import org.junit.Assert.assertEquals
import org.junit.Test

class RelativeTimeTest {

    @Test
    fun `under a minute reads as just now`() {
        assertEquals("just now", formatRelativeTime(nowMillis = 10_000, thenMillis = 9_500))
    }

    @Test
    fun `singular minute is not pluralised`() {
        assertEquals("1 minute ago", formatRelativeTime(nowMillis = 60_000, thenMillis = 0))
    }

    @Test
    fun `plural minutes`() {
        assertEquals("5 minutes ago", formatRelativeTime(nowMillis = 300_000, thenMillis = 0))
    }

    @Test
    fun `singular hour is not pluralised`() {
        assertEquals("1 hour ago", formatRelativeTime(nowMillis = 3_600_000, thenMillis = 0))
    }

    @Test
    fun `plural days`() {
        assertEquals("2 days ago", formatRelativeTime(nowMillis = 2 * 86_400_000L, thenMillis = 0))
    }

    @Test
    fun `never goes negative even if the clock is odd`() {
        assertEquals("just now", formatRelativeTime(nowMillis = 0, thenMillis = 5_000))
    }
}
