package com.simplemode.firetv.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.simplemode.firetv.intent.IntentMatch
import com.simplemode.firetv.intent.IntentResolver
import com.simplemode.firetv.intent.TileSummary
import com.simplemode.firetv.ui.keyboard.KeyAction
import com.simplemode.firetv.ui.keyboard.TellMeKeyboard
import com.simplemode.firetv.ui.keyboard.TvKeyboard
import com.simplemode.firetv.ui.keyboard.TypedLine
import com.simplemode.firetv.ui.theme.Cream
import com.simplemode.firetv.ui.theme.CreamMuted
import kotlinx.coroutines.launch

/**
 * The flagship feature this build's own engineering brief asked for: say what you want in your
 * own words instead of aiming a D-pad at a grid.
 *
 * The screen used to be a stack that scrolled -- a field, a reply, an Ask
 * button, a Home button -- and focusing the field handed the television to
 * whatever keyboard the platform felt like showing. Everything now fits on one
 * unscrolling canvas: what you have typed along the top, the app's own
 * keyboard filling the bottom two thirds, and Ask sitting on that keyboard as
 * the key your thumb is already next to when you finish the last letter.
 *
 * Nothing scrolls, because a screen that scrolls on a television is a screen
 * with a part you have to know is there.
 */
@Composable
fun TellMeScreen(
    resolver: IntentResolver,
    tiles: List<TileSummary>,
    onMatched: (utterance: String, result: IntentMatch.Matched) -> Unit,
    onResolved: (utterance: String, result: IntentMatch) -> Unit,
    onHome: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var text by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var lastResult by remember { mutableStateOf<IntentMatch?>(null) }
    val scope = rememberCoroutineScope()
    val home = remember { FocusRequester() }

    fun submit() {
        val utterance = text.trim()
        if (utterance.isEmpty() || loading) return
        loading = true
        scope.launch {
            val result = resolver.resolve(utterance, tiles)
            onResolved(utterance, result)
            loading = false
            lastResult = result
            if (result is IntentMatch.Matched) onMatched(utterance, result)
        }
    }

    fun apply(action: KeyAction) {
        // A reply belongs to the question that produced it. As soon as the
        // next letter lands it is stale, so it goes.
        if (action != KeyAction.Submit) lastResult = null
        when (action) {
            is KeyAction.Type -> text += action.character
            KeyAction.Space -> if (text.isNotEmpty() && !text.endsWith(" ")) text += " "
            KeyAction.Delete -> text = text.dropLast(1)
            KeyAction.Submit -> submit()
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Home floats on the header band rather than sitting in a row of its
        // own. That is where navigation lives on a real Fire TV -- reference
        // image 05, where the nav strip floats on the hero with no card
        // boundary -- and it leaves the band under the readout for the one
        // thing that belongs there, which is the answer.
        Box(modifier = Modifier.fillMaxWidth()) {
            ScreenHeader(title = "Tell Me")
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 58.dp)
                    .width(168.dp),
            ) {
                FocusableRow(
                    label = "Home",
                    contentDescription = "Back to the Simple Mode home screen.",
                    focusRequester = home,
                    onClick = onHome,
                )
            }
        }
        TypedLine(
            modifier = Modifier.padding(horizontal = 58.dp).padding(top = 14.dp),
            text = text,
            placeholder = "For example: put on something funny",
            contentDescription = if (text.isEmpty()) {
                "Nothing typed yet. Use the keyboard below."
            } else {
                "You have typed: $text"
            },
        )
        // Reserved whether or not there is anything to say, so a reply arriving
        // does not shove the keyboard down under the viewer's thumb.
        Box(
            modifier = Modifier
                .padding(horizontal = 58.dp)
                .height(38.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.CenterStart,
        ) {
            when {
                loading -> Text("Thinking...", color = CreamMuted, style = MaterialTheme.typography.bodyLarge)
                lastResult != null -> Text(
                    text = lastResult!!.reply,
                    color = Cream,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        TvKeyboard(
            grid = TellMeKeyboard,
            onKey = ::apply,
            keyHeight = 50.dp,
            gap = 6.dp,
            exitUp = home,
            modifier = Modifier
                .padding(horizontal = 58.dp)
                .padding(top = 12.dp),
        )
    }
}
