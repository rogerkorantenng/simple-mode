package com.simplemode.firetv.intent

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException

/**
 * Tries [primary] (Bedrock) first; on any real failure -- timeout, no
 * proxy running, no network, a malformed response -- falls back to
 * [fallback] (the offline keyword matcher) rather than letting "Tell Me"
 * die in front of a judge. This is where this build's own engineering brief's "every model
 * call needs a timeout and a fallback" rule is enforced; the timeout
 * itself lives in [BedrockIntentResolver].
 *
 * Deliberately re-throws a plain [CancellationException] (the screen was
 * navigated away, the surrounding scope was cancelled) rather than
 * swallowing it into a fallback call that has no reason to keep running;
 * only our own timeout and genuine call failures trigger the fallback.
 */
class FallbackIntentResolver(
    private val primary: IntentResolver,
    private val fallback: IntentResolver,
) : IntentResolver {
    override suspend fun resolve(utterance: String, tiles: List<TileSummary>): IntentMatch =
        try {
            primary.resolve(utterance, tiles)
        } catch (e: CancellationException) {
            if (e is TimeoutCancellationException) fallback.resolve(utterance, tiles) else throw e
        } catch (_: Exception) {
            fallback.resolve(utterance, tiles)
        }
}
