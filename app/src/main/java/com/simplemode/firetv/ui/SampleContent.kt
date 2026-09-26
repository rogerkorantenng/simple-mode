package com.simplemode.firetv.ui

/**
 * Sample data only. A real build would read the household's actual channel
 * lineup and watch history from a backend a caregiver configured; there is
 * no such backend here, and the README says so. This exists purely so
 * "Live TV" and her own shows are self-contained, guaranteed-to-work demo
 * paths that do not depend on any other app being installed on the test
 * device.
 */
object SampleContent {
    val channels = listOf(
        "7.1 — Local News",
        "2 — Weather",
        "11 — Classic Movies",
        "4 — Game Shows",
    )

    val shows = listOf(
        "The Sunday Service",
        "Inspector Vane",
        "Baking Competition",
    )

    val films = listOf(
        "The One About the Lighthouse",
        "Sunday Afternoon Western",
        "A Wedding in the Village",
        "The Long Walk Home",
    )

    val boxSets = listOf(
        "The Hospital, Series 2",
        "Two Sisters, Series 1",
        "The Allotment, Series 4",
    )

    val familyVideos = listOf(
        "Emma's school concert",
        "The dog in the garden",
        "Birthday, from Tom",
    )
}
