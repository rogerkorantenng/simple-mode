package com.simplemode.firetv.caregiver

/**
 * The caregiver catalogue editor's list operations, kept as pure functions
 * on a `List<String>` of tile ids so they are directly unit-testable with
 * no Android framework, no Compose state, and no persistence involved.
 * [CaregiverCatalogScreen] calls these and hands the result to
 * [TileRepository] to persist -- the editing logic and the storage never
 * have to be tested together.
 */
object TileOrderOps {
    fun moveUp(ids: List<String>, id: String): List<String> {
        val index = ids.indexOf(id)
        if (index <= 0) return ids
        return ids.toMutableList().apply { add(index - 1, removeAt(index)) }
    }

    fun moveDown(ids: List<String>, id: String): List<String> {
        val index = ids.indexOf(id)
        if (index < 0 || index >= ids.lastIndex) return ids
        return ids.toMutableList().apply { add(index + 1, removeAt(index)) }
    }

    fun remove(ids: List<String>, id: String): List<String> = ids.filterNot { it == id }

    /** Adds at the end; a no-op if already present, so a caregiver cannot
     *  accidentally duplicate a tile. */
    fun add(ids: List<String>, id: String): List<String> = if (id in ids) ids else ids + id
}
