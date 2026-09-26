package com.simplemode.firetv.ui.keyboard

/**
 * The keyboard as data: which keys exist, where they sit, and where the
 * D-pad goes from each one. No Compose types anywhere in this file.
 *
 * That separation is the point. A television has no cursor, so a key the
 * D-pad cannot reach does not exist -- and that is a failure which compiles,
 * renders, photographs perfectly and is only discovered by somebody sitting
 * on a sofa pressing right thirty times. Keeping the traversal as arithmetic
 * means the function the screen moves focus with is the same function
 * `KeyGridTest` walks exhaustively.
 *
 * What is *on* a keyboard is a separate argument, and it lives in
 * `KeyboardLayouts.kt`.
 */

/** What pressing a key does. */
sealed interface KeyAction {
    /** Append this character to what has been typed. */
    data class Type(val character: Char) : KeyAction

    /** Remove the last character. There is no remote button for this -- see below. */
    data object Delete : KeyAction

    /** Append a space. A word key rather than a character key, so it can be wide. */
    data object Space : KeyAction

    /** Send what has been typed. */
    data object Submit : KeyAction
}

/**
 * One key. [label] is what is printed on it; [spoken] is what VoiceView says,
 * which for a bare letter has to be a sentence or the screen reader reads a
 * grid of thirty-six unexplained single characters.
 */
data class KeyboardKey(val label: String, val action: KeyAction, val spoken: String)

/** Where a key sits. Row 0 is the top row; column 0 is the left-hand key. */
data class KeyPosition(val row: Int, val column: Int)

/** The four things a D-pad can do. */
enum class KeyDirection { LEFT, RIGHT, UP, DOWN }

/**
 * Where a press landed.
 *
 * A press that runs off the edge does not wrap and does not stop dead: it
 * reports which edge it left by, and the screen decides what is out there --
 * the way back to the home screen, the results list, or nothing at all.
 *
 * Wrap-around is undocumented by Amazon either way. It is refused here on
 * legibility grounds: a highlight that jumps from the right-hand edge of one
 * row to the left-hand edge of the next has, at three metres, simply
 * teleported. The requirement that matters is that every key is *reachable*,
 * and a grid that clamps satisfies that on its own.
 */
sealed interface KeyMove {
    data class To(val position: KeyPosition) : KeyMove
    data object ExitLeft : KeyMove
    data object ExitRight : KeyMove
    data object ExitUp : KeyMove
    data object ExitDown : KeyMove
}

/**
 * A keyboard layout. Rows may hold different numbers of keys -- a row of nine
 * letters above a row of three wide word keys is the usual case -- so moving
 * up or down has to decide which key of the new row is "beneath" the old one.
 *
 * [columnAcross] answers that by matching key *centres* rather than indices.
 * From a row of nine into a row of three, columns 0-2 land on the first wide
 * key, 3-5 on the second and 6-8 on the third; coming back up lands on 1, 4
 * and 7, the middle of the group you left. Pressing down and then up returns
 * you to the same group you started in, which is the property that makes the
 * bottom row feel attached to the grid rather than bolted under it.
 */
class KeyGrid(val rows: List<List<KeyboardKey>>) {

    init {
        require(rows.isNotEmpty()) { "A keyboard with no rows is not a keyboard." }
        require(rows.all { it.isNotEmpty() }) { "A row with no keys leaves a hole in the grid." }
    }

    val rowCount: Int get() = rows.size

    /** Every position in the grid, in reading order. */
    val positions: List<KeyPosition>
        get() = rows.indices.flatMap { row -> rows[row].indices.map { KeyPosition(row, it) } }

    fun widthOf(row: Int): Int = rows[row].size

    fun keyAt(position: KeyPosition): KeyboardKey = rows[position.row][position.column]

    operator fun contains(position: KeyPosition): Boolean =
        position.row in rows.indices && position.column in rows[position.row].indices

    /** The first key, top left, which is where a screen opens. */
    val firstPosition: KeyPosition = KeyPosition(0, 0)

    fun move(from: KeyPosition, direction: KeyDirection): KeyMove {
        require(from in this) { "$from is not on this keyboard." }
        return when (direction) {
            KeyDirection.LEFT ->
                if (from.column > 0) KeyMove.To(from.copy(column = from.column - 1)) else KeyMove.ExitLeft

            KeyDirection.RIGHT ->
                if (from.column < widthOf(from.row) - 1) KeyMove.To(from.copy(column = from.column + 1)) else KeyMove.ExitRight

            KeyDirection.UP ->
                if (from.row > 0) KeyMove.To(KeyPosition(from.row - 1, columnAcross(from, from.row - 1))) else KeyMove.ExitUp

            KeyDirection.DOWN ->
                if (from.row < rowCount - 1) KeyMove.To(KeyPosition(from.row + 1, columnAcross(from, from.row + 1))) else KeyMove.ExitDown
        }
    }

    /**
     * The column in [toRow] whose key sits under (or over) the key at [from].
     * Centre to centre, so it is right whichever way the two rows differ.
     */
    fun columnAcross(from: KeyPosition, toRow: Int): Int {
        val fromWidth = widthOf(from.row)
        val toWidth = widthOf(toRow)
        if (fromWidth == toWidth) return from.column
        val centre = (from.column + 0.5) / fromWidth
        return (centre * toWidth).toInt().coerceIn(0, toWidth - 1)
    }
}
