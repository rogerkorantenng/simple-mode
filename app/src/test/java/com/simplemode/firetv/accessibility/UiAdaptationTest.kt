package com.simplemode.firetv.accessibility

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UiAdaptationTest {

    @Test
    fun `no declared needs keeps the default scale`() {
        val scale = UiAdaptation.scaleFor(AccessibilityNeeds())
        assertEquals(1f, scale.text)
        assertEquals(1f, scale.spacing)
        assertEquals(1.5f, scale.borderWidth)
    }

    @Test
    fun `captions on grows text, spacing and the focus border together`() {
        val scale = UiAdaptation.scaleFor(AccessibilityNeeds(captionsEnabled = true))
        assertEquals(1.25f, scale.text)
        assertEquals(1.25f, scale.spacing)
        assertEquals(3f, scale.borderWidth)
    }

    @Test
    fun `audio description off never describes aloud`() {
        assertFalse(UiAdaptation.shouldDescribeAloud(AccessibilityNeeds(audioDescriptionsEnabled = false)))
    }

    @Test
    fun `a platform that never answers is treated the same as off, not as on`() {
        // This is the Fire OS 14-16 case Amazon's own docs flag: the secure
        // setting "only applies to Fire OS 8 devices and earlier."
        assertFalse(UiAdaptation.shouldDescribeAloud(AccessibilityNeeds(audioDescriptionsEnabled = null)))
    }

    @Test
    fun `audio description on describes aloud`() {
        assertTrue(UiAdaptation.shouldDescribeAloud(AccessibilityNeeds(audioDescriptionsEnabled = true)))
    }

    @Test
    fun `tile description is title only when audio description is off`() {
        val description = UiAdaptation.tileContentDescription(
            title = "Netflix",
            shortDescription = "Open the Netflix app.",
            needs = AccessibilityNeeds(audioDescriptionsEnabled = false),
        )
        assertEquals("Netflix", description)
    }

    @Test
    fun `tile description is title only when the platform never answered`() {
        val description = UiAdaptation.tileContentDescription(
            title = "Netflix",
            shortDescription = "Open the Netflix app.",
            needs = AccessibilityNeeds(audioDescriptionsEnabled = null),
        )
        assertEquals("Netflix", description)
    }

    @Test
    fun `tile description adds the short description when audio description is on`() {
        val description = UiAdaptation.tileContentDescription(
            title = "Netflix",
            shortDescription = "Open the Netflix app.",
            needs = AccessibilityNeeds(audioDescriptionsEnabled = true),
        )
        assertEquals("Netflix. Open the Netflix app.", description)
    }
}
