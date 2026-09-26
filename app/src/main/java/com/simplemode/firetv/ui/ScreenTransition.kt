package com.simplemode.firetv.ui

import android.provider.Settings
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * How long a whole-screen change takes to cross-fade, and why it is not 120 ms.
 *
 * the shared Fire TV craft reference's Timing section gives two numbers, not one: focus
 * transitions at 120 ms or less (matched in [TileFocusState]) and **screen
 * transitions at 200 to 250 ms**. Those are different motions doing different
 * jobs -- focus has to keep up with a D-pad repeating every ~50 ms, a screen
 * change does not -- so they were always meant to run at different speeds.
 * 220 sits in the middle of that screen-transition range and next to, not on
 * top of, the 180 ms focus lift of a mock Fire TV launcher used elsewhere to
 * compare motion across this batch's apps, which is the other motion a viewer
 * sees in the same sitting.
 *
 * Before this, every navigation in this app was a raw `when (screen)` swap: an
 * instant cut, home to Films, home to the household log, every one of them a
 * hard frame change. an internal design review's item 1 names this as
 * the single biggest gap between these apps and the mock launcher they sit
 * next to on screen, which gained the same kind of motion first.
 */
const val SCREEN_TRANSITION_MS = 220

/**
 * False when the person has turned system animations off.
 *
 * Mirrors the shared Fire TV craft reference's item 48 and the identical helper in
 * Profile Gate ([com.profilegate.app.ui.components.reducedMotion]) and Steady
 * ([com.steady.app.ui.components.animationsEnabled]) -- Simple Mode had no
 * such read anywhere before this change, so this is that same one-line check
 * given a home here rather than three different names for one fact.
 */
@Composable
fun screenAnimationsEnabled(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) != 0f
        }.getOrDefault(true)
    }
}

/**
 * A calm cross-fade for a screen swap: no slide, no direction, because
 * nothing in this app's navigation implies "screens live to the left or
 * right of each other" -- every screen is one level from Home and none of
 * them are siblings in a sequence. A fade says "somewhere new," which is
 * all that's true here.
 */
fun screenCrossfade(animated: Boolean): AnimatedContentTransitionScope<Screen>.() -> ContentTransform = {
    val duration = if (animated) SCREEN_TRANSITION_MS else 0
    fadeIn(tween(duration)) togetherWith fadeOut(tween(duration))
}
