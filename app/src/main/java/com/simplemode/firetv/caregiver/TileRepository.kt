package com.simplemode.firetv.caregiver

import android.content.Context
import com.simplemode.firetv.launch.Tile
import com.simplemode.firetv.launch.TileCatalog
import org.json.JSONArray

/**
 * What used to be true here -- "the seven tiles are fixed in code" -- was
 * the single largest gap this app had, per an internal feature-depth review.
 * [TileCatalog] is still the fixed *universe* of things Simple Mode knows
 * how to launch; this repository is the caregiver's chosen order and
 * selection from that universe, persisted so it survives a relaunch.
 * Until a caregiver ever opens the catalogue editor, this returns exactly
 * the same default set the static catalogue always did.
 */
object TileRepository {
    private const val PREFS_NAME = "tile_repository"
    private const val KEY_ORDER = "grid_tile_ids"

    fun currentGridTileIds(context: Context): List<String> {
        val stored = readStoredIds(context)
        return stored ?: TileCatalog.tiles.filter { it.showOnHomeGrid }.map { it.id }
    }

    fun currentGridTiles(context: Context): List<Tile> =
        currentGridTileIds(context).mapNotNull { TileCatalog.findById(it) }

    fun setGridTileIds(context: Context, ids: List<String>) {
        val array = JSONArray()
        ids.forEach { array.put(it) }
        prefs(context).edit().putString(KEY_ORDER, array.toString()).apply()
    }

    /** Every tile that exists but is not currently on the grid -- what the
     *  catalogue editor's "add" list offers. */
    fun availableToAdd(context: Context): List<Tile> {
        val current = currentGridTileIds(context).toSet()
        return TileCatalog.tiles.filterNot { it.id in current }
    }

    private fun readStoredIds(context: Context): List<String>? {
        val raw = prefs(context).getString(KEY_ORDER, null) ?: return null
        return try {
            val array = JSONArray(raw)
            (0 until array.length()).map { array.getString(it) }
        } catch (_: Exception) {
            null
        }
    }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
