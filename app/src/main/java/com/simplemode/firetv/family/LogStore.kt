package com.simplemode.firetv.family

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Small shared plumbing for "the last N things that happened", persisted
 * as a JSON array in `SharedPreferences`. [QueryLog] and [WatchHistoryLog]
 * are both just this plus their own entry shape -- pulled out so the
 * actual persistence logic exists once, not twice.
 */
internal object LogStore {
    private const val PREFS_NAME = "family_log"

    fun read(context: Context, key: String): JSONArray {
        val raw = prefs(context).getString(key, null) ?: return JSONArray()
        return try {
            JSONArray(raw)
        } catch (_: Exception) {
            JSONArray()
        }
    }

    fun append(context: Context, key: String, entry: JSONObject, maxEntries: Int) {
        val entries = read(context, key)
        val trimmed = JSONArray()
        // Newest first: the new entry, then as many old ones as fit.
        trimmed.put(entry)
        for (i in 0 until minOf(entries.length(), maxEntries - 1)) {
            trimmed.put(entries.get(i))
        }
        prefs(context).edit().putString(key, trimmed.toString()).apply()
    }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
