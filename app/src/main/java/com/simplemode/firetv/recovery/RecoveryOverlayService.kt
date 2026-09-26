package com.simplemode.firetv.recovery

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import android.view.WindowManager
import android.widget.Button
import com.simplemode.firetv.MainActivity
import com.simplemode.firetv.SimpleModeApp

/**
 * The real answer to "one button that always gets you back here."
 *
 * Fire TV documents no launch-on-boot or launcher-replacement API (see
 * internal research into Fire TV's own documentation, section 2, and the 404 on
 * /docs/fire-tv/launch-on-boot.html). This app does not bet the promise on
 * either. Instead it draws a small, always-on-top button using
 * `TYPE_APPLICATION_OVERLAY` -- a normal, non-signature Android mechanism
 * (the same one chat-head style apps have used for years) -- that can sit on
 * top of any app, DRM-protected video included, because it never touches that
 * app's window, only draws its own on top of the screen. Pressing it always
 * relaunches [MainActivity].
 *
 * **The button is conditional, and that is the point.** An earlier version
 * added it in `onCreate` and removed it in `onDestroy`, so once Always Home
 * was on the button was on screen permanently: over Simple Mode's own home
 * grid, where it covered the first tile's label, and on a real television
 * over whatever the viewer was watching. It now appears only when
 * [OverlayVisibility] says the viewer is somewhere other than Simple Mode,
 * which the app learns from its own activity lifecycle via [ForegroundWatcher]
 * and no permission at all. Once up, it recedes on the schedule in
 * [OverlayRecede] so it stops covering the picture.
 *
 * This does still need the caregiver to grant "draw over other apps" once
 * (see [OverlayPermission]) and to have started this service once. Surviving
 * a reboot without that is a separate, best-effort question; see
 * [BootReceiver].
 */
class RecoveryOverlayService : Service() {

    private var windowManager: WindowManager? = null
    private var button: Button? = null
    private var shownAtUptimeMs: Long = 0L

    private val handler = Handler(Looper.getMainLooper())
    private val recedeRunnable = Runnable { applyPresence(animated = true) }

    private val foregroundListener: (Boolean) -> Unit = { applyVisibility() }

    /**
     * Null only if the process is running under some other [android.app.Application]
     * -- an instrumentation harness, say. In that case the service falls back
     * to the old unconditional behaviour rather than to a button that never
     * appears: a visible button in the wrong place is a cosmetic fault, a
     * missing one breaks the only promise this app makes.
     */
    private val watcher: ForegroundWatcher?
        get() = (application as? SimpleModeApp)?.foreground

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, RecoveryNotification.build(this))
        val watcher = watcher
        if (watcher != null) {
            // addListener calls straight back with the current answer, so
            // subscribing also performs the first evaluation.
            watcher.addListener(foregroundListener)
        } else {
            applyVisibility()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Re-evaluate on every start: a caregiver toggling Always Home back on
        // from the settings screen restarts this service while Simple Mode is
        // in front, and the button must not flash up over its own home grid.
        applyVisibility()
        return START_STICKY
    }

    override fun onDestroy() {
        watcher?.removeListener(foregroundListener)
        handler.removeCallbacks(recedeRunnable)
        removeOverlayButton()
        super.onDestroy()
    }

    private fun applyVisibility() {
        val shouldShow = OverlayVisibility.shouldShowButton(
            alwaysHomeEnabled = RecoveryPreference.isEnabled(this),
            overlayPermissionGranted = OverlayPermission.isGranted(this),
            // No watcher means no lifecycle signal; treat Simple Mode as not
            // on screen so the button still exists. See [watcher].
            simpleModeOnScreen = watcher?.isOnScreen ?: false,
        )
        if (shouldShow) showOverlayButton() else removeOverlayButton()
    }

    private fun showOverlayButton() {
        if (button != null) return

        val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        windowManager = wm

        val newButton = OverlayButton.create(this) { bringSimpleModeToFront() }
        val params = OverlayButton.layoutParams()
        OverlayButton.applyInsets(this, params)
        wm.addView(newButton, params)
        button = newButton
        shownAtUptimeMs = SystemClock.uptimeMillis()

        // Full size first, then recede. Every fresh appearance is a fresh
        // dwell, because every fresh appearance means the viewer just left
        // Simple Mode and is the moment the button matters most.
        applyPresence(animated = false)
        handler.removeCallbacks(recedeRunnable)
        handler.postDelayed(recedeRunnable, OverlayRecede.delayUntilRecede(elapsedSinceShown()))
    }

    private fun applyPresence(animated: Boolean) {
        val visible = button ?: return
        val elapsed = elapsedSinceShown()
        OverlayButton.applyPresence(
            button = visible,
            alpha = OverlayRecede.alphaAt(elapsed),
            scale = OverlayRecede.scaleAt(elapsed),
            animated = animated,
        )
    }

    private fun elapsedSinceShown(): Long = SystemClock.uptimeMillis() - shownAtUptimeMs

    private fun removeOverlayButton() {
        handler.removeCallbacks(recedeRunnable)
        val wm = windowManager ?: return
        button?.let { wm.removeView(it) }
        button = null
        shownAtUptimeMs = 0L
    }

    private fun bringSimpleModeToFront() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(MainActivity.EXTRA_RECOVERED, true)
        }
        startActivity(intent)
    }

    companion object {
        private const val NOTIFICATION_ID = 1

        fun start(context: Context) {
            val intent = Intent(context, RecoveryOverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
