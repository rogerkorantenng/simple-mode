package com.simplemode.firetv.ui

import androidx.annotation.DrawableRes
import com.simplemode.firetv.R

/**
 * A photograph for every row of every list screen.
 *
 * The home wall carried thirty real photographs and the screens one press
 * behind it carried a green play triangle on a flat rectangle. That is the
 * unfinished half of an app showing through at the first press, and on a wall
 * of photographs it reads as content that failed to load rather than as a
 * deliberately plain screen.
 *
 * Nothing here is a real title's artwork. These are evocative photographs from
 * Wikimedia Commons chosen for the *kind* of thing each row is -- a lighthouse
 * on a coast, a lit window on a wet street, bread on a worktop -- graded to a
 * different dominant tone each, because on a television that variation is most
 * of how somebody finds the row they want before they have read it. Licences,
 * authors and file pages are in `ATTRIBUTION.md`, read from the Commons API on
 * the same request that returned each image.
 *
 * Keyed by the row's own name. [SampleContent] is the only source of those
 * names and `ListArtworkTest` walks all five lists, so a row can never be
 * added without a picture going with it.
 */
@DrawableRes
fun listArtworkFor(label: String): Int? = ListArtwork[label]

private val ListArtwork: Map<String, Int> = mapOf(
    // Live TV
    "7.1 — Local News" to R.drawable.art_ch_news,
    "2 — Weather" to R.drawable.art_ch_weather,
    "11 — Classic Movies" to R.drawable.art_ch_classic,
    "4 — Game Shows" to R.drawable.art_ch_gameshows,
    // The shows picked out for her
    "The Sunday Service" to R.drawable.art_show_sunday,
    "Inspector Vane" to R.drawable.art_show_detective,
    "Baking Competition" to R.drawable.art_show_baking,
    // Films
    "The One About the Lighthouse" to R.drawable.art_film_lighthouse,
    "Sunday Afternoon Western" to R.drawable.art_film_western,
    "A Wedding in the Village" to R.drawable.art_film_wedding,
    "The Long Walk Home" to R.drawable.art_film_walk,
    // Box Sets
    "The Hospital, Series 2" to R.drawable.art_set_hospital,
    "Two Sisters, Series 1" to R.drawable.art_set_sisters,
    "The Allotment, Series 4" to R.drawable.art_set_allotment,
    // Family Videos
    "Emma's school concert" to R.drawable.art_fam_concert,
    "The dog in the garden" to R.drawable.art_fam_dog,
    "Birthday, from Tom" to R.drawable.art_fam_birthday,
)

/** Every row name the app can show, so a test can walk them without Compose. */
val AllListLabels: List<String> = listOf(
    SampleContent.channels,
    SampleContent.shows,
    SampleContent.films,
    SampleContent.boxSets,
    SampleContent.familyVideos,
).flatten()

/** The names this file has a photograph for. Exposed so the test can diff the two. */
val LabelsWithArtwork: Set<String> = ListArtwork.keys
