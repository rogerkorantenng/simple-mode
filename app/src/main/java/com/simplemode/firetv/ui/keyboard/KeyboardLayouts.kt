package com.simplemode.firetv.ui.keyboard

/**
 * The two keyboards this app has.
 *
 * Split from [KeyGrid] because they are different kinds of thing. `KeyGrid` is
 * arithmetic -- where a D-pad press lands, given any grid -- and does not change
 * when a key does. This is where an argument about what belongs on a keyboard
 * gets settled, and there are two of those:
 *
 * **Why alphabetical rather than QWERTY.** Amazon's own *Design and User
 * Experience Guidelines (Fire TV)* describes its search as: "the user moves
 * LEFT and RIGHT through the alphabet, pressing SELECT on each letter to type
 * a search query." QWERTY is muscle memory for ten fingers; with one D-pad
 * there is no muscle memory, only hunting, and a hunt through A-B-C beats a
 * hunt through Q-W-E for anyone -- and beats it by a mile for the viewer this
 * app is actually for, who may never have used a typewriter keyboard.
 *
 * **Why there is no shift key and no symbols page.** The same page's Text
 * Entry section documents exactly one keyboard, carrying "letters and
 * numbers", and names no mode key of any kind. Nor does the case matter here:
 * `KeywordIntentResolver` lowercases the utterance before it matches, so a
 * capital could not change a single outcome. A mode key that provably does
 * nothing is worse than no mode key, and on a remote it also costs a press
 * and a rule to remember. Digits get a row of their own instead, in the open.
 */

/**
 * Tell Me's keyboard. Every character the screen can produce is on it at once:
 * nothing is behind a mode, so there is nothing to discover and nothing to
 * remember.
 *
 * ```
 * A B C D E F G H I
 * J K L M N O P Q R
 * S T U V W X Y Z 0
 * 1 2 3 4 5 6 7 8 9
 * [ Space ][ Delete ][  Ask  ]
 * ```
 *
 * Reading order runs A to Z and straight on through 0 to 9 without a break,
 * so "where is the 4" has the same answer as "where is the D": further along.
 * Zero sits at the end of the letters rather than after nine because that is
 * where continuing to read puts it.
 *
 * Delete and Ask are printed keys because Amazon's *Remote Control Input*
 * reference (updated February 2026) lists no keycode for either. It documents
 * `KEYCODE_BACK` as "return to previous operation or screen", so wiring Back
 * to backspace -- the obvious shortcut -- would break the one remote button
 * whose meaning every Fire TV owner already knows.
 */
val TellMeKeyboard: KeyGrid = KeyGrid(
    listOf(
        lettersRow("ABCDEFGHI"),
        lettersRow("JKLMNOPQR"),
        lettersRow("STUVWXYZ0"),
        lettersRow("123456789"),
        listOf(
            KeyboardKey("Space", KeyAction.Space, "Space"),
            KeyboardKey("Delete", KeyAction.Delete, "Delete the last letter"),
            KeyboardKey("Ask", KeyAction.Submit, "Ask Tell Me"),
        ),
    ),
)

/**
 * The caregiver's keypad, and it is deliberately not the keyboard above.
 *
 * A PIN is four digits. Offering thirty-six keys to collect four digits is
 * thirty-two keys of D-pad distance spent on characters that cannot be part
 * of the answer, and it makes a lock look like a search box.
 *
 * The order is a cash machine's, not a calculator's: 1-2-3 along the top.
 * Every keypad an adult meets while being asked to prove something -- an ATM,
 * a door entry panel, a phone -- is arranged this way, and the bottom row of
 * an ATM is exactly cancel, zero, enter.
 *
 * @param submitLabel what the bottom-right key does, in the words of what it
 *   does: "Set PIN" the first time, "Unlock" every time after. A key that says
 *   "Enter" describes the gesture rather than the outcome.
 */
fun caregiverKeypad(submitLabel: String): KeyGrid = KeyGrid(
    listOf(
        digitsRow("123"),
        digitsRow("456"),
        digitsRow("789"),
        listOf(
            KeyboardKey("Delete", KeyAction.Delete, "Delete the last digit"),
            KeyboardKey("0", KeyAction.Type('0'), "Zero"),
            KeyboardKey(submitLabel, KeyAction.Submit, submitLabel),
        ),
    ),
)

private fun lettersRow(characters: String): List<KeyboardKey> =
    characters.map { character ->
        KeyboardKey(
            label = character.toString(),
            action = KeyAction.Type(character.lowercaseChar()),
            spoken = if (character.isDigit()) "Number $character" else "Letter $character",
        )
    }

private fun digitsRow(characters: String): List<KeyboardKey> =
    characters.map { KeyboardKey(it.toString(), KeyAction.Type(it), "Number $it") }
