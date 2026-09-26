package com.simplemode.firetv.launch

import com.simplemode.firetv.Viewer

/**
 * The fixed set of tiles Simple Mode knows about. Six is the low end of
 * the "six or eight huge targets" brief; a caregiver-configurable
 * catalogue is explicitly out of scope for this build -- see SPEC.md
 * "What this does not do." Not every tile shows on the home grid; see
 * [Tile.showOnHomeGrid].
 *
 * Every tile here opens something this app can actually deliver. Three of
 * them used to be Netflix, Prime Video and YouTube, launched by real
 * package name so [TileResolution] was exercising the real "is this
 * actually installed" question. On any machine we can demo on, the answer
 * is no, so a third of the home wall did nothing when pressed -- and the
 * names were somebody else's. Naming a destination after a service we
 * cannot open is worse than naming it after what the viewer gets, and what
 * she gets is a short list of films, a shelf of series and the clips her
 * family sent. [TileResolution] still runs on every press; it is simply
 * never the thing that fails now.
 *
 * External-app tiles remain fully supported -- a caregiver can add one in
 * the editor, and the graceful "not set up yet" path is still what happens
 * when the app is missing. They are just not what the shipped home wall is
 * made of.
 */
object TileCatalog {
    val tiles: List<Tile> = listOf(
        Tile(
            id = "tell_me",
            title = "Tell Me",
            shortDescription = "Say what you want in your own words.",
            target = LaunchTarget.InApp(InAppScreen.TELL_ME),
        ),
        Tile(
            id = "live_tv",
            title = "Live TV",
            shortDescription = "Watch a channel from the guide.",
            target = LaunchTarget.InApp(InAppScreen.LIVE_GUIDE),
        ),
        Tile(
            id = "her_shows",
            title = "${Viewer.POSSESSIVE} Shows",
            shortDescription = "The shows already picked out for ${Viewer.NAME}.",
            target = LaunchTarget.InApp(InAppScreen.HER_SHOWS),
        ),
        Tile(
            id = "films",
            title = "Films",
            shortDescription = "A few films, ready to watch.",
            target = LaunchTarget.InApp(InAppScreen.FILMS),
        ),
        Tile(
            id = "box_sets",
            title = "Box Sets",
            shortDescription = "Series with more than one episode left.",
            target = LaunchTarget.InApp(InAppScreen.BOX_SETS),
        ),
        Tile(
            id = "family_videos",
            title = "Family Videos",
            shortDescription = "Short clips the family has sent.",
            target = LaunchTarget.InApp(InAppScreen.FAMILY_VIDEOS),
        ),
        Tile(
            id = "captions_and_sound",
            title = "Captions",
            shortDescription = "Open the TV's own caption and audio description settings.",
            target = LaunchTarget.SystemSetting("android.settings.ACCESSIBILITY_SETTINGS"),
        ),
        Tile(
            id = "call_for_help",
            title = "Call for Help",
            shortDescription = "Show who set this TV up and how to reach them.",
            target = LaunchTarget.InApp(InAppScreen.CALL_FOR_HELP),
            showOnHomeGrid = false,
        ),
    )

    fun findById(id: String): Tile? = tiles.find { it.id == id }
}
