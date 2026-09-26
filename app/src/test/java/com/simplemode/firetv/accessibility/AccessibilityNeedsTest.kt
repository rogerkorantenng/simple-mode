package com.simplemode.firetv.accessibility

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

class AccessibilityNeedsTest {

    @Test
    fun `defaults are the safe, unadapted state`() {
        val needs = AccessibilityNeeds()
        assertFalse(needs.captionsEnabled)
        assertNull(needs.audioDescriptionsEnabled)
    }
}
