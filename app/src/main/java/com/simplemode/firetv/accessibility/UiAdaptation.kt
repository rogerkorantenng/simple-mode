package com.simplemode.firetv.accessibility

import com.simplemode.firetv.ui.theme.UiScale

/**
 * Pure decision logic that turns [AccessibilityNeeds] into on-screen
 * behaviour. Kept free of any Android framework class so it can be unit
 * tested without an emulator or Robolectric.
 */
object UiAdaptation {

    private const val CAPTIONS_TEXT_BOOST = 1.25f
    private const val CAPTIONS_BORDER_WIDTH = 3f
    private const val DEFAULT_BORDER_WIDTH = 1.5f

    /** How much bigger text, spacing and tile borders should be. */
    fun scaleFor(needs: AccessibilityNeeds): UiScale {
        val boost = if (needs.captionsEnabled) CAPTIONS_TEXT_BOOST else 1f
        return UiScale(
            text = boost,
            spacing = boost,
            borderWidth = if (needs.captionsEnabled) CAPTIONS_BORDER_WIDTH else DEFAULT_BORDER_WIDTH,
        )
    }

    /**
     * True only when the platform explicitly said audio description is on.
     * A `null` reading (setting absent, or the Fire OS 8-and-earlier limit
     * Amazon documents) is never treated as "on".
     */
    fun shouldDescribeAloud(needs: AccessibilityNeeds): Boolean =
        needs.audioDescriptionsEnabled == true

    /**
     * The spoken label VoiceView / TalkBack will read for a tile. When the
     * household has declared audio description on, this app treats that as
     * a proxy for "wants more said, not less" and appends the short
     * description; otherwise the title alone is enough.
     */
    fun tileContentDescription(
        title: String,
        shortDescription: String,
        needs: AccessibilityNeeds,
    ): String = if (shouldDescribeAloud(needs)) {
        "$title. $shortDescription"
    } else {
        title
    }
}
