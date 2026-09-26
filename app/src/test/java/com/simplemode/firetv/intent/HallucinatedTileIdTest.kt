package com.simplemode.firetv.intent

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The model is told never to invent a tile id. It was told, and nothing
 * checked, which is the shape of defect this project found in seven apps
 * at once: a happy-path test plus a trusted prompt.
 *
 * These are the cases where the model does the thing it was told not to.
 * The app must not act on the invented id; it must give the offline
 * keyword matcher a turn instead; and if that finds nothing either it must
 * say so in words rather than leaving a viewer who cannot work a
 * television pressing a button that does nothing.
 */
class HallucinatedTileIdTest {

    private val tiles = listOf(
        TileSummary("live_tv", "Live TV", "Watch a channel from the guide."),
        TileSummary("her_shows", "Norah's Shows", "The shows already picked out for Norah."),
    )

    @Test
    fun `a tile id that was never sent is rejected, not acted on`() {
        val thrown = assertThrows(UnknownTileIdException::class.java) {
            BedrockIntentResolver.interpret("netflix", "Here's Netflix.", tiles)
        }
        assertEquals("netflix", thrown.tileId)
    }

    @Test
    fun `a tile id that was sent is accepted`() {
        val result = BedrockIntentResolver.interpret("live_tv", "Here's Live TV.", tiles)
        assertEquals(IntentMatch.Matched("live_tv", "Here's Live TV.", IntentSource.BEDROCK), result)
    }

    @Test
    fun `a null tile id is an honest no-match, not a rejection`() {
        val result = BedrockIntentResolver.interpret(null, "", tiles)
        assertTrue(result is IntentMatch.NoMatch)
    }

    @Test
    fun `an invented id falls through to the offline keyword match rather than acting`() = runBlocking {
        val hallucinating = IntentResolver { _, _ -> throw UnknownTileIdException("ghost_tile") }
        val resolver = FallbackIntentResolver(hallucinating, KeywordIntentResolver())

        val result = resolver.resolve("put the news on", tiles)

        // Not the invented id, and not silence: the offline matcher answered.
        assertEquals(IntentMatch.Matched("live_tv", "Here's Live TV.", IntentSource.OFFLINE_FALLBACK), result)
    }

    @Test
    fun `an invented id with nothing to fall back on says so in words`() = runBlocking {
        val hallucinating = IntentResolver { _, _ -> throw UnknownTileIdException("ghost_tile") }
        val resolver = FallbackIntentResolver(hallucinating, KeywordIntentResolver())

        val result = resolver.resolve("qwertyuiop", tiles)

        assertTrue(result is IntentMatch.NoMatch)
        assertTrue((result as IntentMatch.NoMatch).reply.isNotBlank())
        assertEquals(IntentSource.OFFLINE_FALLBACK, result.source)
    }
}
