package com.simplemode.firetv.intent

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.IOException

/**
 * Everything here uses fake [IntentResolver]s, never the real network
 * client -- this is the "mock at the API boundary" rule applied to Tell
 * Me: the interface is the boundary, so a test double behind it is honest,
 * unlike faking data the app itself would present as real.
 */
class FallbackIntentResolverTest {

    private val tiles = listOf(TileSummary("live_tv", "Live TV", "Watch a channel."))

    @Test
    fun `uses the primary result when Bedrock succeeds`() = runBlocking {
        val primary = FakeResolver { IntentMatch.Matched("live_tv", "Sure.", IntentSource.BEDROCK) }
        val fallback = FakeResolver { error("should never be called") }
        val resolver = FallbackIntentResolver(primary, fallback)

        val result = resolver.resolve("turn on the tv", tiles)

        assertEquals(IntentSource.BEDROCK, result.source)
    }

    @Test
    fun `falls back on a network failure`() = runBlocking {
        val primary = FakeResolver { throw IOException("no route to host") }
        val fallback = FakeResolver { IntentMatch.Matched("live_tv", "Here.", IntentSource.OFFLINE_FALLBACK) }
        val resolver = FallbackIntentResolver(primary, fallback)

        val result = resolver.resolve("turn on the tv", tiles)

        assertEquals(IntentSource.OFFLINE_FALLBACK, result.source)
    }

    @Test
    fun `falls back on a real coroutine timeout, not a simulated one`() = runBlocking {
        // Forces an actual TimeoutCancellationException from withTimeout,
        // the same one BedrockIntentResolver's own timeout would throw,
        // rather than constructing one by hand.
        val primary = FakeResolver { withTimeout(1) { delay(100); error("unreachable") } }
        val fallback = FakeResolver { IntentMatch.Matched("live_tv", "Here.", IntentSource.OFFLINE_FALLBACK) }
        val resolver = FallbackIntentResolver(primary, fallback)

        val result = resolver.resolve("turn on the tv", tiles)

        assertEquals(IntentSource.OFFLINE_FALLBACK, result.source)
    }

    @Test
    fun `never swallows cancellation from the surrounding scope`() {
        val primary = FakeResolver { throw CancellationException("scope gone") }
        val fallback = FakeResolver { error("should never be called") }
        val resolver = FallbackIntentResolver(primary, fallback)

        assertThrows(CancellationException::class.java) {
            runBlocking { resolver.resolve("turn on the tv", tiles) }
        }
    }

    private class FakeResolver(private val block: suspend () -> IntentMatch) : IntentResolver {
        override suspend fun resolve(utterance: String, tiles: List<TileSummary>): IntentMatch = block()
    }
}
