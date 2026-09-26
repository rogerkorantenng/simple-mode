package com.simplemode.firetv.ui

import androidx.annotation.DrawableRes
import com.simplemode.firetv.R
import com.simplemode.firetv.launch.LaunchTarget
import com.simplemode.firetv.launch.Tile

/**
 * Which picture goes on which tile.
 *
 * Every home-wall tile carries a 16:9 photograph from Wikimedia Commons,
 * cropped, graded to a tile-specific tone and scrimmed at the bottom for the
 * label -- see `tools/build_photos.py` and `ATTRIBUTION.md`. Two of the six
 * were replaced 2026-09-25 (an internal design review's item 3):
 * Live TV and Films had settled on the most literal, generic-stock reading of
 * their own category -- an old television, a cinema auditorium -- which reads
 * as a placeholder standing in for artwork rather than artwork. Both are now
 * a specific, real thing (a rooftop aerial; a real marquee's own neon) rather
 * than the universal symbol for the category.
 *
 * No tile carries a real service's mark, and since the catalogue stopped
 * naming services it cannot open, no tile carries a real service's name
 * either. Each picture is of the thing the viewer actually gets.
 *
 * Why artwork at all, on an app whose whole argument is restraint: a flat
 * rectangle with a word centred in it is what a wireframe looks like. Held
 * up against a real Fire TV home screen -- see
 * a set of reference photographs of a real Fire TV -- the difference is not taste, it is
 * whether the screen reads as a finished product or as a placeholder for
 * one.
 */
@DrawableRes
fun tileArtworkFor(id: String, isExternalApp: Boolean, isSystemSetting: Boolean): Int = when {
    id == "tell_me" -> R.drawable.art_tell_me
    id == "live_tv" -> R.drawable.art_live_tv
    id == "her_shows" -> R.drawable.art_her_shows
    id == "call_for_help" -> R.drawable.art_call_for_help
    id == "films" -> R.drawable.art_films
    id == "box_sets" -> R.drawable.art_box_sets
    id == "family_videos" -> R.drawable.art_family_videos
    isSystemSetting -> R.drawable.art_captions
    // Anything a caregiver adds later still gets a picture rather than
    // falling back to a coloured rectangle.
    isExternalApp -> R.drawable.art_another_app
    else -> R.drawable.art_another_app
}

@DrawableRes
fun tileArtworkFor(tile: Tile): Int = tileArtworkFor(
    id = tile.id,
    isExternalApp = tile.target is LaunchTarget.ExternalApp,
    isSystemSetting = tile.target is LaunchTarget.SystemSetting,
)
