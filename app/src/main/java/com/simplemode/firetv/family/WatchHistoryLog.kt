package com.simplemode.firetv.family

import android.content.Context
import org.json.JSONObject

/**
 * "What I Watched" -- the real day-two feature a family actually wants:
 * something the person picked from Live TV or her own shows turns up here, so
 * a caregiver checking in remotely (or the viewer themselves) can see it
 * without re-tracing every tap. Empty until something is actually picked;
 * see [WatchHistoryScreen] for the honest empty state.
 */
data class WatchEntry(val label: String, val timestampMillis: Long)

object WatchHistoryLog {
    private const val KEY = "watched"
    private const val MAX_ENTRIES = 10

    fun record(context: Context, label: String, now: Long) {
        val entry = JSONObject().put("label", label).put("timestampMillis", now)
        LogStore.append(context, KEY, entry, MAX_ENTRIES)
    }

    fun recent(context: Context): List<WatchEntry> {
        val array = LogStore.read(context, KEY)
        return (0 until array.length()).map { i ->
            val obj = array.getJSONObject(i)
            WatchEntry(label = obj.getString("label"), timestampMillis = obj.getLong("timestampMillis"))
        }
    }
}
