package com.simplemode.firetv.launch

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TileCatalogTest {

    @Test
    fun `the home GRID has between six and eight tiles, per the brief`() {
        val count = TileCatalog.tiles.count { it.showOnHomeGrid }
        assertTrue("expected 6-8 grid tiles, got $count", count in 6..8)
    }

    @Test
    fun `every tile has a unique id`() {
        val ids = TileCatalog.tiles.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `every tile has a non-empty title and a spoken description`() {
        TileCatalog.tiles.forEach { tile ->
            assertTrue("${tile.id} has no title", tile.title.isNotBlank())
            assertTrue("${tile.id} has no short description", tile.shortDescription.isNotBlank())
        }
    }

    @Test
    fun `live TV and her shows are hosted in-app, not dependent on another app being installed`() {
        val liveTv = TileCatalog.tiles.first { it.id == "live_tv" }
        val herShows = TileCatalog.tiles.first { it.id == "her_shows" }
        assertTrue(liveTv.target is LaunchTarget.InApp)
        assertTrue(herShows.target is LaunchTarget.InApp)
    }

    @Test
    fun `call for help is a real destination but does not take a home grid slot`() {
        val callForHelp = TileCatalog.findById("call_for_help")
        assertTrue(callForHelp != null)
        assertFalse(callForHelp!!.showOnHomeGrid)
    }

    @Test
    fun `tell me is on the home grid`() {
        val tellMe = TileCatalog.findById("tell_me")
        assertTrue(tellMe != null)
        assertTrue(tellMe!!.showOnHomeGrid)
    }

    @Test
    fun `findById returns null for an id that does not exist`() {
        assertEquals(null, TileCatalog.findById("nonexistent"))
    }
}
