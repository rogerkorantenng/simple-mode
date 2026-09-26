package com.simplemode.firetv.recovery

/**
 * The single condition that decides whether the floating "back to Simple
 * Mode" button belongs on screen right now.
 *
 * Before this existed, [RecoveryOverlayService] added the button in
 * `onCreate` and removed it in `onDestroy`, so once a caregiver turned Always
 * Home on the button was on screen permanently -- over Simple Mode's own home
 * grid, where it covered the first tile's label, and on real hardware over
 * whatever the viewer was actually watching. A control whose entire job is to
 * bring a lost viewer *back* to Simple Mode has no reason to exist while
 * Simple Mode is already in front.
 *
 * Kept as a pure function, like [BootReceiver.shouldRestartOverlay], because
 * "shown when elsewhere, hidden when home" is exactly the kind of condition
 * that silently regresses and exactly the kind a unit test can pin down
 * without a device.
 */
object OverlayVisibility {

    /**
     * @param alwaysHomeEnabled the caregiver's persisted Always Home toggle
     *        ([RecoveryPreference]).
     * @param overlayPermissionGranted whether this app may still draw over
     *        other apps ([OverlayPermission]). Re-checked live rather than
     *        remembered: a caregiver can revoke it from Settings at any time.
     * @param simpleModeOnScreen whether any of this app's own activities is
     *        still visible ([ForegroundTally.isOnScreen]). Deliberately
     *        "visible", not "resumed": Android's `Activity` docs say a paused
     *        activity "is still visible on screen", so treating a pause as a
     *        departure would raise the button over Simple Mode's own UI behind
     *        a dialog.
     */
    fun shouldShowButton(
        alwaysHomeEnabled: Boolean,
        overlayPermissionGranted: Boolean,
        simpleModeOnScreen: Boolean,
    ): Boolean = alwaysHomeEnabled && overlayPermissionGranted && !simpleModeOnScreen
}
