package com.simplemode.firetv.intent

/**
 * The offline fallback for "Tell Me": no network, no model, just word
 * overlap between what was said and each tile's own title and
 * description, plus a small synonym list for the phrasing an accessibility
 * audience actually uses ("I can't hear" should find Captions even though
 * neither word appears in that tile's own text). This is what keeps the
 * demo alive without a network connection -- see this build's own engineering brief's rule
 * that every model call needs a fallback.
 */
class KeywordIntentResolver : IntentResolver {
    override suspend fun resolve(utterance: String, tiles: List<TileSummary>): IntentMatch =
        matchKeywords(utterance, tiles)

    companion object {
        private val STOP_WORDS = setOf(
            "a", "an", "the", "to", "want", "watch", "i", "her", "his", "my",
            "is", "it", "me", "on", "some", "something", "with", "for", "of",
        )

        private val SYNONYMS = mapOf(
            "captions_and_sound" to listOf("hear", "hearing", "loud", "volume", "sound", "caption", "subtitle", "quiet"),
            "call_for_help" to listOf("help", "stuck", "confused", "lost", "family", "call"),
            "live_tv" to listOf("channel", "news", "guide"),
            "her_shows" to listOf("show", "program", "favourite", "favorite"),
        )

        /** Pure so it is directly unit-testable with no coroutine or
         *  interface involved. */
        fun matchKeywords(utterance: String, tiles: List<TileSummary>): IntentMatch {
            val words = tokenize(utterance)
            if (words.isEmpty()) return noMatch()

            val scored = tiles.associateWith { tile -> score(words, tile) }
            val best = scored.maxByOrNull { it.value }
            return if (best != null && best.value > 0) {
                IntentMatch.Matched(best.key.id, "Here's ${best.key.title}.", IntentSource.OFFLINE_FALLBACK)
            } else {
                noMatch()
            }
        }

        private fun score(words: Set<String>, tile: TileSummary): Int {
            val tileWords = tokenize("${tile.title} ${tile.shortDescription}") + (SYNONYMS[tile.id] ?: emptyList())
            return words.count { it in tileWords }
        }

        private fun tokenize(text: String): Set<String> =
            text.lowercase()
                .split(Regex("[^a-z']+"))
                .filter { it.isNotBlank() && it !in STOP_WORDS }
                .toSet()

        private fun noMatch() = IntentMatch.NoMatch(
            "I couldn't tell what you meant. Try one of the buttons below.",
            IntentSource.OFFLINE_FALLBACK,
        )
    }
}
