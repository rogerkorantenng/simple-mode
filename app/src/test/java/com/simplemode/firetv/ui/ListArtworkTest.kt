package com.simplemode.firetv.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Every row of every list screen has a photograph.
 *
 * The screens behind the home wall shipped with a green play triangle on a
 * flat rectangle while the wall in front of them carried real photographs.
 * That is the kind of gap nobody notices from the code, because the code
 * compiles and the screen renders -- it is only obvious to somebody who
 * presses select once. This walks all five lists so a row cannot be added
 * without a picture going with it.
 */
class ListArtworkTest {

    @Test
    fun `every row in every list has a photograph`() {
        assertEquals(emptyList<String>(), AllListLabels.filterNot { it in LabelsWithArtwork })
    }

    @Test
    fun `no photograph is mapped to a row that no longer exists`() {
        assertEquals(emptySet<String>(), LabelsWithArtwork - AllListLabels.toSet())
    }

    @Test
    fun `no two rows share a photograph`() {
        // Two rows with the same picture is worse than no picture: it tells the
        // viewer they are the same thing.
        val artwork = AllListLabels.map { listArtworkFor(it) }
        assertEquals(artwork.size, artwork.toSet().size)
    }

    @Test
    fun `there are enough rows for the screen to be worth opening`() {
        listOf(
            SampleContent.channels,
            SampleContent.shows,
            SampleContent.films,
            SampleContent.boxSets,
            SampleContent.familyVideos,
        ).forEach { assertTrue("a list screen with fewer than three rows is a dead end", it.size >= 3) }
    }

    @Test
    fun `no row name is long enough to need a third line at the heading step`() {
        // A row gives the name about 620 dp. At the 32 sp heading step that is
        // roughly 37 characters a line and the row allows two, so 74 is the
        // point at which a name would be cut -- this pins the headroom rather
        // than the exact wrap.
        AllListLabels.forEach {
            assertTrue("'$it' is too long to fit two lines of a row", it.length <= 74)
        }
    }
}
