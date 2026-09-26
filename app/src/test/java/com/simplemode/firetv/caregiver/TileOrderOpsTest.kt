package com.simplemode.firetv.caregiver

import org.junit.Assert.assertEquals
import org.junit.Test

class TileOrderOpsTest {

    private val ids = listOf("a", "b", "c")

    @Test
    fun `moves a tile up one place`() {
        assertEquals(listOf("b", "a", "c"), TileOrderOps.moveUp(ids, "b"))
    }

    @Test
    fun `moving the first tile up does nothing`() {
        assertEquals(ids, TileOrderOps.moveUp(ids, "a"))
    }

    @Test
    fun `moves a tile down one place`() {
        assertEquals(listOf("a", "c", "b"), TileOrderOps.moveDown(ids, "b"))
    }

    @Test
    fun `moving the last tile down does nothing`() {
        assertEquals(ids, TileOrderOps.moveDown(ids, "c"))
    }

    @Test
    fun `moving an id that is not present does nothing`() {
        assertEquals(ids, TileOrderOps.moveUp(ids, "z"))
        assertEquals(ids, TileOrderOps.moveDown(ids, "z"))
    }

    @Test
    fun `removes a tile by id`() {
        assertEquals(listOf("a", "c"), TileOrderOps.remove(ids, "b"))
    }

    @Test
    fun `adds a new tile at the end`() {
        assertEquals(listOf("a", "b", "c", "d"), TileOrderOps.add(ids, "d"))
    }

    @Test
    fun `adding a tile already present does not duplicate it`() {
        assertEquals(ids, TileOrderOps.add(ids, "b"))
    }
}
