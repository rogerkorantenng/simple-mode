package com.simplemode.firetv.intent

/**
 * What answering "what did the viewer mean" produced. [source] exists
 * purely for the "For the Family" transparency screen, so a caregiver can
 * see whether a given answer came from Bedrock or the offline fallback --
 * the same honesty rule as everywhere else in this app: never claim a
 * capability that did not actually run.
 */
sealed interface IntentMatch {
    val reply: String
    val source: IntentSource

    data class Matched(val tileId: String, override val reply: String, override val source: IntentSource) : IntentMatch
    data class NoMatch(override val reply: String, override val source: IntentSource) : IntentMatch
}

enum class IntentSource { BEDROCK, OFFLINE_FALLBACK }

/** Anything that can turn free text plus the known tiles into an [IntentMatch]. */
fun interface IntentResolver {
    suspend fun resolve(utterance: String, tiles: List<TileSummary>): IntentMatch
}

/** The minimal, framework-free view of a tile this package needs -- kept
 *  separate from [com.simplemode.firetv.launch.Tile] so this package does
 *  not need to depend on the launch package's Android-facing types. */
data class TileSummary(val id: String, val title: String, val shortDescription: String)
