package com.simplemode.firetv.accessibility

/**
 * The viewer's own declared accessibility needs, read from Fire OS rather
 * than asked for. No setup wizard produces this; it is either read once at
 * launch or pushed by the platform on change.
 *
 * [audioDescriptionsEnabled] is nullable on purpose: Amazon's own
 * documentation notes the underlying secure setting "only applies to Fire OS
 * 8 devices and earlier," so on current hardware this app may get no answer
 * at all. `null` means "platform did not say," and every caller in this app
 * treats that as false rather than crashing or guessing.
 */
data class AccessibilityNeeds(
    val captionsEnabled: Boolean = false,
    val audioDescriptionsEnabled: Boolean? = null,
)
