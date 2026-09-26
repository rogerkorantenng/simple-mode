package com.simplemode.firetv

/**
 * The person whose television this is.
 *
 * Until now the app never named her. Every screen the family reads called her
 * "she" -- "What she's watched", "Edit What She Sees", "what shows on her home
 * screen" -- which an internal review of this batch's three Fire TV apps together picked up as the app
 * being written for the family *about* her rather than for the household she is
 * in. That is a small thing on one screen and a posture across five of them:
 * the television in her front room, running an app that will not say who it
 * belongs to.
 *
 * So there is a name, and it is used everywhere the copy used to say "she".
 *
 * It is a constant here because this build has no setup wizard -- SPEC.md is
 * explicit that nothing asks the viewer anything, and the accessibility
 * settings are read from the television rather than collected. In a real build
 * the name is the one fact the caregiver types, on the phone that pairs the
 * set, and it arrives with the rest of the household configuration. Keeping it
 * in one place means that change is one field, not a search for pronouns.
 */
object Viewer {
    /** What the family calls her, and what every screen in the app calls her. */
    const val NAME = "Norah"

    /** "Norah's" -- the possessive, so no screen has to build one by hand. */
    const val POSSESSIVE = "Norah's"
}
