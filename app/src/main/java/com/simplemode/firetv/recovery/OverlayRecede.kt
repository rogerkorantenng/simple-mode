package com.simplemode.firetv.recovery

/**
 * How the recovery button gets out of the way of the picture.
 *
 * Once the button only appears outside Simple Mode, the place it appears is
 * on top of someone else's app -- a film, a news stream, a photo. It has to
 * be legible when the viewer first lands somewhere unfamiliar, and it has to
 * stop being a green rectangle glued over the bottom-left of the picture two
 * minutes later.
 *
 * So it arrives at full size, holds for [DWELL_MS] -- long enough for a
 * viewer who reads slowly to find it -- and then recedes to a small, faint
 * marker that still says where the way back is without competing with the
 * content. It returns to full size on the next event we can actually observe:
 * the viewer leaving Simple Mode again, which is the moment the button
 * becomes relevant.
 *
 * It does **not** return on a key press. A `TYPE_APPLICATION_OVERLAY` window
 * carrying `FLAG_NOT_FOCUSABLE` never receives key input, and dropping that
 * flag would hand the whole remote to the overlay and strand the viewer in
 * the app behind it -- a trap, not a rescue. That limitation is written down
 * in FRICTION.md rather than papered over.
 *
 * The values are constants in one place, and pure functions read them, so the
 * timing is testable without waiting six seconds in a test.
 */
object OverlayRecede {

    /** Full-size hold after the button appears. */
    const val DWELL_MS = 6_000L

    /** How long the shrink itself takes, once the dwell is over. */
    const val FADE_MS = 600L

    const val FULL_ALPHA = 1.0f
    const val FULL_SCALE = 1.0f

    /** Still clearly visible against both bright and dark content, but no
     *  longer reading as part of the picture. */
    const val RECEDED_ALPHA = 0.45f

    /** Small enough to free the content under it, large enough that the
     *  home glyph is still a recognisable shape from a sofa. */
    const val RECEDED_SCALE = 0.62f

    fun alphaAt(millisSinceShown: Long): Float =
        if (millisSinceShown < DWELL_MS) FULL_ALPHA else RECEDED_ALPHA

    fun scaleAt(millisSinceShown: Long): Float =
        if (millisSinceShown < DWELL_MS) FULL_SCALE else RECEDED_SCALE

    /** Milliseconds to wait before starting the shrink, given how long the
     *  button has already been up. Never negative, so a caller that arrives
     *  late posts its runnable immediately instead of scheduling into the
     *  past. */
    fun delayUntilRecede(millisSinceShown: Long): Long =
        (DWELL_MS - millisSinceShown).coerceAtLeast(0L)
}
