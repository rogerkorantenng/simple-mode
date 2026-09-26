package com.simplemode.firetv.launch

/**
 * One target a viewer or "Tell Me" can reach. [shortDescription] is what
 * gets read aloud in addition to the title when the household has audio
 * description on; see
 * [com.simplemode.firetv.accessibility.UiAdaptation.tileContentDescription].
 *
 * [showOnHomeGrid] keeps the six-to-eight-huge-targets budget honest: Call
 * for Help is a real destination but is reached through "Tell Me" (saying
 * "help" or "I'm stuck") rather than taking one of the few precious home
 * tiles, now that Tell Me itself needs one.
 */
data class Tile(
    val id: String,
    val title: String,
    val shortDescription: String,
    val target: LaunchTarget,
    val showOnHomeGrid: Boolean = true,
)

sealed interface LaunchTarget {
    /** Another installed app, launched by package name. */
    data class ExternalApp(val packageName: String) : LaunchTarget

    /** A screen Simple Mode hosts itself, so the demo never depends on what
     *  happens to be installed. */
    data class InApp(val screen: InAppScreen) : LaunchTarget

    /** A real Fire OS system screen, e.g. the accessibility settings page. */
    data class SystemSetting(val action: String) : LaunchTarget
}

enum class InAppScreen {
    LIVE_GUIDE,
    HER_SHOWS,
    FILMS,
    BOX_SETS,
    FAMILY_VIDEOS,
    CALL_FOR_HELP,
    TELL_ME,
}
