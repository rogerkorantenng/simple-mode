package com.simplemode.firetv.recovery

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Typeface
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button

/**
 * Builds the floating "back to Simple Mode" button: the window parameters
 * and the view itself. Kept separate from [RecoveryOverlayService] so the
 * service reads as pure lifecycle, and this file is the one place that
 * knows the actual on-screen geometry.
 *
 * `BOTTOM|START` keeps the x/y insets unambiguous (positive always means
 * "further onto the screen"), unlike `BOTTOM|END`, where a positive x
 * offset pushes the button past the right edge and off the display --
 * verified the hard way, see FRICTION.md.
 */
object OverlayButton {
    // WindowManager.LayoutParams offsets are pixels, not dp, and 48 of them
    // at Fire TV's density 320 is 24 dp -- half of Amazon's 48 dp / 30 dp
    // overscan floor, so the one control whose entire job is "this always
    // gets you back" was the one element sitting inside the region a
    // television is free to crop. These are the floor plus clearance,
    // converted at the density the device actually reports.
    private const val INSET_LEFT_DP = 58
    private const val INSET_BOTTOM_DP = 40

    fun layoutParams(): WindowManager.LayoutParams = WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
        PixelFormat.TRANSLUCENT,
    ).apply {
        gravity = Gravity.BOTTOM or Gravity.START
    }

    fun applyInsets(context: Context, params: WindowManager.LayoutParams) {
        val density = context.resources.displayMetrics.density
        params.x = (INSET_LEFT_DP * density).toInt()
        params.y = (INSET_BOTTOM_DP * density).toInt()
    }

    fun create(context: Context, onClick: () -> Unit): Button = Button(context).apply {
        text = "⌂ Simple Mode"
        // 28sp, the body floor the shared Fire TV craft reference sets for this app
        // specifically. 18sp was below even the 20sp absolute floor, on the
        // one control that has to be readable from wherever the viewer has
        // wandered to.
        textSize = 28f
        typeface = Typeface.DEFAULT_BOLD
        setTextColor(Color.parseColor("#FBF3E7"))
        setBackgroundColor(Color.parseColor("#4F7A52"))
        setPadding(40, 24, 40, 24)
        setOnClickListener { onClick() }
    }

    /**
     * How much of the screen the button is currently claiming. [OverlayRecede]
     * decides the numbers; this only applies them.
     *
     * Scaling the view inside its own window rather than resizing the window
     * keeps the change free of a layout pass, and because the window is
     * `WRAP_CONTENT` around the button, shrinking simply leaves transparent
     * space -- nothing clips, and nothing else on screen moves.
     *
     * @param animated false when the window has just been added and there is
     *        nothing to animate from.
     */
    fun applyPresence(button: Button, alpha: Float, scale: Float, animated: Boolean) {
        // Pivot at the bottom-left, matching the window's BOTTOM|START
        // gravity, so a shrink pulls the button towards the edge of the screen
        // and frees the content rather than leaving a gap around it.
        button.pivotX = 0f
        button.pivotY = button.height.toFloat()

        button.animate().cancel()
        if (!animated) {
            button.alpha = alpha
            button.scaleX = scale
            button.scaleY = scale
            return
        }
        button.animate()
            .alpha(alpha)
            .scaleX(scale)
            .scaleY(scale)
            .setDuration(OverlayRecede.FADE_MS)
            .start()
    }
}
