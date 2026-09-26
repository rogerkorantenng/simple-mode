package com.simplemode.firetv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.simplemode.firetv.caregiver.CaregiverPin
import com.simplemode.firetv.ui.keyboard.KeyAction
import com.simplemode.firetv.ui.keyboard.TvKeyboard
import com.simplemode.firetv.ui.keyboard.caregiverKeypad
import com.simplemode.firetv.ui.theme.Cream
import com.simplemode.firetv.ui.theme.CreamMuted
import com.simplemode.firetv.ui.theme.FernEdge
import com.simplemode.firetv.ui.theme.FernTitle
import com.simplemode.firetv.ui.theme.Plinth

/**
 * The gate an internal feature-depth review asked for: the catalogue editor
 * "cannot be changed by accident." First run sets a PIN rather than shipping a
 * default nobody would know to change; every run after that verifies it. Not a
 * security boundary against a determined attacker -- see CaregiverPin.kt --
 * just against an idle remote.
 *
 * **This is the one screen in Simple Mode with a different audience, so it is
 * the one screen with a different shape.** Everywhere else the app is
 * addressing somebody who cannot work a television, and it is built for her:
 * warm, wide, one full-bleed band of drawn front room across the top, every
 * control a full-width row. Here it is addressing her son, who is competent,
 * who has walked over to change something, and who is being stopped. A lock
 * should not feel like the room it is fitted to.
 *
 * Four things are deliberately unlike the rest of the app:
 *
 * - **Two columns.** Every other screen is a full-width vertical stack. What
 *   is happening sits on the left, the keypad on the right.
 * - **No drawn room.** `ScreenHeader`'s lamp and chair are what make every
 *   Simple Mode screen feel like the same front room, so the lock does not get
 *   them.
 * - **The ground drops to `Plinth`.** Darker than the app's charcoal, which is
 *   the colour the app otherwise uses only *underneath* things.
 * - **No text field and no alphabet.** A PIN is four digits. Thirty-six keys
 *   to collect four of them is thirty-two keys of D-pad distance spent on
 *   characters that cannot be part of the answer. See `caregiverKeypad`.
 */
@Composable
fun CaregiverPinScreen(onUnlocked: () -> Unit, onHome: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val settingNewPin = remember { !CaregiverPin.isSet(context) }
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val home = remember { FocusRequester() }
    val keypad = remember(settingNewPin) { caregiverKeypad(if (settingNewPin) "Set PIN" else "Unlock") }

    fun submit() {
        if (pin.length != PIN_LENGTH) {
            error = "Use exactly 4 digits."
            return
        }
        if (settingNewPin) {
            CaregiverPin.set(context, pin)
            onUnlocked()
        } else if (CaregiverPin.verify(context, pin)) {
            onUnlocked()
        } else {
            error = "That's not it. Try again."
            pin = ""
        }
    }

    fun apply(action: KeyAction) {
        when (action) {
            is KeyAction.Type -> if (pin.length < PIN_LENGTH) { pin += action.character; error = null }
            KeyAction.Delete -> { pin = pin.dropLast(1); error = null }
            KeyAction.Submit -> submit()
            KeyAction.Space -> Unit
        }
    }

    Box(modifier = modifier.fillMaxSize().background(Plinth)) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 58.dp, vertical = 40.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f).padding(end = 44.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp),
            ) {
                Text(
                    text = if (settingNewPin) "Set a family PIN" else "Enter the family PIN",
                    color = FernTitle,
                    style = MaterialTheme.typography.titleLarge,
                )
                PinSlabs(
                    filled = pin.length,
                    description = if (settingNewPin) {
                        "Choose a four digit family PIN. ${pin.length} of 4 entered."
                    } else {
                        "Enter the four digit family PIN. ${pin.length} of 4 entered."
                    },
                )
                Text(
                    text = error ?: if (settingNewPin) {
                        "Four digits. Only the family needs to know it."
                    } else {
                        "So this can only be changed on purpose."
                    },
                    color = if (error != null) Cream else CreamMuted,
                    style = MaterialTheme.typography.bodyLarge,
                )
                // A compact chip, not a full-width row. Stretched across the
                // column the icon ended up marooned a long way from its own
                // label, with the width of the screen between them.
                Box(modifier = Modifier.width(208.dp)) {
                    FocusableRow(
                        label = "Home",
                        contentDescription = "Back to the Simple Mode home screen.",
                        icon = TileIcon.Home,
                        focusRequester = home,
                        onClick = onHome,
                    )
                }
            }
            // The one vertical rule in the app. Every other screen separates
            // planes horizontally; a lock has a side that asks and a side that
            // answers, and the rule says which is which. 3 dp of FernEdge, the
            // same device ScreenHeader uses between its two planes.
            Box(modifier = Modifier.width(3.dp).height(KeypadHeight).background(FernEdge))
            TvKeyboard(
                grid = keypad,
                onKey = ::apply,
                keyHeight = KeyHeight,
                gap = KeyGap,
                keyShape = RoundedCornerShape(14.dp),
                exitLeft = home,
                modifier = Modifier.width(376.dp).padding(start = 24.dp),
            )
        }
    }
}

private val KeyHeight = 88.dp
private val KeyGap = 10.dp

/** Four keys and three gaps. The vertical rule is cut to match it exactly. */
private val KeypadHeight = KeyHeight * 4 + KeyGap * 3

/** A PIN is four digits. The screen collects them and [PinSlabs] shows them. */
internal const val PIN_LENGTH = 4
