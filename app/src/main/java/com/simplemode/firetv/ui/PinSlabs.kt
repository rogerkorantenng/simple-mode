package com.simplemode.firetv.ui

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.simplemode.firetv.ui.theme.CharcoalElevated
import com.simplemode.firetv.ui.theme.Fern

/**
 * The four digits, as four slabs that fill rather than four dots.
 *
 * An empty slab is a `Fern` bar along its own base; a filled one is the whole
 * slab in `Fern`, the bar having grown up through it. So the state difference
 * is a change in the size of a filled shape, which is the only kind of
 * difference that survives a lit room at three metres -- the same argument
 * the shared focus-plate design notes makes about the focus plate, made here
 * about progress.
 *
 * `Fern`, never `FernBright`. The bright green means focus and means nothing
 * else anywhere in this app, and a row of four bright slabs beside a keypad
 * would leave two things claiming to be the focused one.
 */
@Composable
fun PinSlabs(filled: Int, description: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.semantics { contentDescription = description },
    ) {
        repeat(PIN_LENGTH) { index ->
            val isFilled = index < filled
            val fill by animateDpAsState(
                targetValue = if (isFilled) SlabHeight else EmptyBarHeight,
                animationSpec = tween(FILL_MS),
                label = "slab",
            )
            Box(
                modifier = Modifier
                    .size(width = SlabWidth, height = SlabHeight)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CharcoalElevated),
                contentAlignment = Alignment.BottomCenter,
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(fill).background(Fern))
            }
        }
    }
}

private val SlabWidth = 72.dp
private val SlabHeight = 96.dp
private val EmptyBarHeight = 10.dp
private const val FILL_MS = 120
