package com.simplemode.firetv.ui.keyboard

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * A key the D-pad cannot reach does not exist.
 *
 * That failure compiles, renders, and photographs perfectly. It is found by a
 * person on a sofa pressing right thirty times and never arriving at Z, which
 * is the worst possible place to find it, so it is found here instead. The
 * first test walks the whole grid and is the one that matters; the rest pin
 * the specific joins that a hand-written layout tends to get wrong.
 */
class KeyGridTest {

    private val tellMe = TellMeKeyboard
    private val keypad = caregiverKeypad("Unlock")

    // --- reachability: the test this file exists for -------------------------

    @Test
    fun `every key on the Tell Me keyboard is reachable from the first key`() {
        assertEquals(emptySet<KeyPosition>(), unreachableIn(tellMe))
    }

    @Test
    fun `every key on the caregiver keypad is reachable from the first key`() {
        assertEquals(emptySet<KeyPosition>(), unreachableIn(keypad))
    }

    @Test
    fun `every key on the Tell Me keyboard can get back to the first key`() {
        // Reachability is not symmetric on its own: a grid can let you walk
        // into a corner it will not walk you out of.
        tellMe.positions.forEach { start ->
            assertTrue("$start cannot reach the first key", tellMe.firstPosition in reachableFrom(tellMe, start))
        }
    }

    @Test
    fun `every character the Tell Me keyboard can produce appears exactly once`() {
        val typed = tellMe.rows.flatten().mapNotNull { (it.action as? KeyAction.Type)?.character }
        assertEquals((('a'..'z') + ('0'..'9')).sorted(), typed.sorted())
        assertEquals(typed.size, typed.toSet().size)
    }

    @Test
    fun `the Tell Me keyboard carries a delete and a submit, because the remote has neither`() {
        val actions = tellMe.rows.flatten().map { it.action }
        assertTrue(KeyAction.Delete in actions)
        assertTrue(KeyAction.Submit in actions)
        assertTrue(KeyAction.Space in actions)
    }

    // --- the joins between rows of different widths --------------------------

    @Test
    fun `pressing down from a letter lands on the wide key beneath it`() {
        // Nine letters over three wide keys: the first three letters share the
        // first wide key, the next three the second, the last three the third.
        val functionRow = tellMe.rowCount - 1
        val landedOn = (0..8).map { column ->
            val move = tellMe.move(KeyPosition(functionRow - 1, column), KeyDirection.DOWN)
            (move as KeyMove.To).position.column
        }
        assertEquals(listOf(0, 0, 0, 1, 1, 1, 2, 2, 2), landedOn)
    }

    @Test
    fun `pressing up from a wide key lands in the middle of the group it serves`() {
        val functionRow = tellMe.rowCount - 1
        val landedOn = (0..2).map { column ->
            val move = tellMe.move(KeyPosition(functionRow, column), KeyDirection.UP)
            (move as KeyMove.To).position.column
        }
        assertEquals(listOf(1, 4, 7), landedOn)
    }

    @Test
    fun `down then up returns to the group you started in`() {
        val letterRow = tellMe.rowCount - 2
        (0..8).forEach { column ->
            val down = tellMe.move(KeyPosition(letterRow, column), KeyDirection.DOWN) as KeyMove.To
            val backUp = tellMe.move(down.position, KeyDirection.UP) as KeyMove.To
            assertEquals(column / 3, backUp.position.column / 3)
        }
    }

    // --- edges: every one of them reports which edge it was -------------------

    @Test
    fun `the left-hand column exits left rather than wrapping to the far end`() {
        tellMe.rows.indices.forEach { row ->
            assertEquals(KeyMove.ExitLeft, tellMe.move(KeyPosition(row, 0), KeyDirection.LEFT))
        }
    }

    @Test
    fun `the right-hand column exits right rather than wrapping to the next row`() {
        tellMe.rows.indices.forEach { row ->
            val lastColumn = tellMe.widthOf(row) - 1
            assertEquals(KeyMove.ExitRight, tellMe.move(KeyPosition(row, lastColumn), KeyDirection.RIGHT))
        }
    }

    @Test
    fun `the top row exits up and the bottom row exits down`() {
        assertEquals(KeyMove.ExitUp, tellMe.move(KeyPosition(0, 4), KeyDirection.UP))
        val bottom = tellMe.rowCount - 1
        assertEquals(KeyMove.ExitDown, tellMe.move(KeyPosition(bottom, 1), KeyDirection.DOWN))
    }

    @Test
    fun `nothing in the middle of the grid exits`() {
        tellMe.positions
            .filter { it.row in 1 until tellMe.rowCount - 1 && it.column in 1 until tellMe.widthOf(it.row) - 1 }
            .forEach { position ->
                KeyDirection.entries.forEach { direction ->
                    assertTrue(
                        "$position went off the edge pressing $direction",
                        tellMe.move(position, direction) is KeyMove.To,
                    )
                }
            }
    }

    // --- the keypad is a lock, not a keyboard ---------------------------------

    @Test
    fun `the caregiver keypad is a cash machine, not a calculator`() {
        assertEquals(listOf("1", "2", "3"), keypad.rows[0].map { it.label })
        assertEquals(listOf("7", "8", "9"), keypad.rows[2].map { it.label })
        assertEquals(listOf("Delete", "0", "Unlock"), keypad.rows[3].map { it.label })
    }

    @Test
    fun `the caregiver keypad offers ten digits and no letters`() {
        val typed = keypad.rows.flatten().mapNotNull { (it.action as? KeyAction.Type)?.character }
        assertEquals(('0'..'9').toList(), typed.sorted())
    }

    @Test
    fun `the submit key says what it will do, first run and after`() {
        assertEquals("Set PIN", caregiverKeypad("Set PIN").rows[3][2].label)
        assertEquals("Unlock", caregiverKeypad("Unlock").rows[3][2].label)
    }

    @Test
    fun `no key on either layout is silent to a screen reader`() {
        (tellMe.rows.flatten() + keypad.rows.flatten()).forEach { key ->
            assertTrue("${key.label} has nothing for VoiceView to say", key.spoken.isNotBlank())
        }
    }

    @Test
    fun `a bare letter is spoken as a letter, not left as one character`() {
        assertEquals("Letter A", tellMe.rows[0][0].spoken)
        assertEquals("Number 7", tellMe.rows[3][6].spoken)
    }

    // --- a hardware keyboard types, and never eats the D-pad ------------------

    @Test
    fun `a printable character from a hardware keyboard is typed`() {
        var typed: KeyAction? = null
        val handled = handleHardwareKey(
            KeyEventType.KeyDown,
            Key.B,
            'B'.code,
        ) { typed = it }
        assertTrue(handled)
        assertEquals(KeyAction.Type('b'), typed)
    }

    @Test
    fun `the D-pad is never swallowed by the hardware keyboard handler`() {
        listOf(
            Key.DirectionUp,
            Key.DirectionDown,
            Key.DirectionLeft,
            Key.DirectionRight,
            Key.DirectionCenter,
            Key.Back,
            Key.Enter,
        ).forEach { key ->
            assertTrue(
                "$key was intercepted and would never reach the focus system",
                !handleHardwareKey(KeyEventType.KeyDown, key, 0) { },
            )
        }
    }

    // --- helpers --------------------------------------------------------------

    /** Breadth-first over the four directions, which is exactly what a thumb does. */
    private fun reachableFrom(grid: KeyGrid, start: KeyPosition): Set<KeyPosition> {
        val seen = mutableSetOf(start)
        val queue = ArrayDeque(listOf(start))
        while (queue.isNotEmpty()) {
            val here = queue.removeFirst()
            KeyDirection.entries.forEach { direction ->
                val move = grid.move(here, direction)
                if (move is KeyMove.To && seen.add(move.position)) queue.addLast(move.position)
            }
        }
        return seen
    }

    private fun unreachableIn(grid: KeyGrid): Set<KeyPosition> =
        grid.positions.toSet() - reachableFrom(grid, grid.firstPosition)
}
