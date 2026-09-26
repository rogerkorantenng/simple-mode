package com.simplemode.firetv.family

import android.content.Context
import com.simplemode.firetv.intent.IntentMatch
import com.simplemode.firetv.intent.IntentSource
import org.json.JSONObject

/**
 * What was asked of "Tell Me" and how it was answered, for the "For the
 * Family" screen's own transparency: a caregiver should be able to see
 * whether the AI feature is actually working, not just be told it is.
 * This is real depth this app did not have before -- the query log this
 * screen reads is a genuine record of what Bedrock (or the offline
 * fallback) actually returned, not sample data.
 */
data class QueryLogEntry(
    val utterance: String,
    val matchedTitle: String?,
    val source: IntentSource,
    val timestampMillis: Long,
)

object QueryLog {
    private const val KEY = "queries"
    private const val MAX_ENTRIES = 10

    fun record(context: Context, utterance: String, matchedTitle: String?, result: IntentMatch, now: Long) {
        val entry = JSONObject()
            .put("utterance", utterance)
            .put("matchedTitle", matchedTitle)
            .put("source", result.source.name)
            .put("timestampMillis", now)
        LogStore.append(context, KEY, entry, MAX_ENTRIES)
    }

    fun recent(context: Context): List<QueryLogEntry> {
        val array = LogStore.read(context, KEY)
        return (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            QueryLogEntry(
                utterance = obj.getString("utterance"),
                matchedTitle = obj.optString("matchedTitle", null.toString()).takeUnless { it == "null" },
                source = IntentSource.valueOf(obj.getString("source")),
                timestampMillis = obj.getLong("timestampMillis"),
            )
        }
    }
}
