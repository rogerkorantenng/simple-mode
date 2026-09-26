package com.simplemode.firetv.ui.keyboard

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType

/**
 * Lets a USB or Bluetooth keyboard type into the same readout the D-pad does.
 *
 * Fire TV pairs with real keyboards, and a caregiver who has one should not
 * have to spell a PIN out with a D-pad. It also means `adb shell input text`
 * drives the screen, which is how the shipped screenshots were taken.
 *
 * It must not touch the D-pad. Every directional and select key reports
 * `utf16CodePoint` 0 or a control character, so the guard is to act only on
 * something printable and hand everything else back to the focus system.
 */
internal fun handleHardwareKey(
    type: KeyEventType,
    pressed: Key,
    codePoint: Int,
    onKey: (KeyAction) -> Unit,
): Boolean {
    if (type != KeyEventType.KeyDown) return false
    if (pressed in NeverIntercepted) return false
    if (pressed == Key.Backspace || pressed == Key.Delete) {
        onKey(KeyAction.Delete)
        return true
    }
    val character = codePoint.toChar()
    return when {
        character == ' ' -> { onKey(KeyAction.Space); true }
        character.isLetterOrDigit() -> { onKey(KeyAction.Type(character.lowercaseChar())); true }
        else -> false
    }
}

private val NeverIntercepted = setOf(
    Key.DirectionUp,
    Key.DirectionDown,
    Key.DirectionLeft,
    Key.DirectionRight,
    Key.DirectionCenter,
    Key.Enter,
    Key.NumPadEnter,
    Key.Back,
)
