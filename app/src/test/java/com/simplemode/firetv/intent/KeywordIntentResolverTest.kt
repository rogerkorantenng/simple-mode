package com.simplemode.firetv.intent

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class KeywordIntentResolverTest {

    private val tiles = listOf(
        TileSummary("live_tv", "Live TV", "Watch a channel from the guide."),
        TileSummary("her_shows", "Norah's Shows", "The shows already picked out for Norah."),
        TileSummary("captions_and_sound", "Captions", "Open the TV caption and audio description settings."),
        TileSummary("call_for_help", "Call for Help", "Show who set this TV up."),
    )
    private val resolver = KeywordIntentResolver()

    @Test
    fun `matches a tile whose own words overlap the utterance`() = runBlocking {
        val result = resolver.resolve("show me her shows", tiles)
        assertEquals(IntentMatch.Matched("her_shows", "Here's Norah's Shows.", IntentSource.OFFLINE_FALLBACK), result)
    }

    @Test
    fun `matches an accessibility phrase via the synonym list, not the tile's own words`() = runBlocking {
        val result = resolver.resolve("I can't hear anything", tiles)
        assertTrue(result is IntentMatch.Matched)
        assertEquals("captions_and_sound", (result as IntentMatch.Matched).tileId)
    }

    @Test
    fun `matches a request for help`() = runBlocking {
        val result = resolver.resolve("I'm stuck and confused", tiles)
        assertTrue(result is IntentMatch.Matched)
        assertEquals("call_for_help", (result as IntentMatch.Matched).tileId)
    }

    @Test
    fun `an utterance with no overlap at all returns no match`() = runBlocking {
        val result = resolver.resolve("what time is my flight tomorrow", tiles)
        assertTrue(result is IntentMatch.NoMatch)
        assertEquals(IntentSource.OFFLINE_FALLBACK, result.source)
    }

    @Test
    fun `blank input returns no match rather than an arbitrary tile`() = runBlocking {
        val result = resolver.resolve("   ", tiles)
        assertTrue(result is IntentMatch.NoMatch)
    }

    @Test
    fun `every match reports the offline source, never claiming to be Bedrock`() = runBlocking {
        val result = resolver.resolve("live tv please", tiles)
        assertEquals(IntentSource.OFFLINE_FALLBACK, result.source)
    }
}
