package com.simplemode.firetv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.simplemode.firetv.ui.theme.Cream

/**
 * One control in the strip. Unfocused it is plain text on the photograph
 * with no container at all; focused it is a solid `#8FBF83` pill with the
 * label knocked out of it. That is the mechanic read straight off the
 * reference photographs, and the reason nothing on this screen is ever
 * outlined.
 */
@Composable
fun NavItem(
    label: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    icon: TileIcon? = null,
    initialFocus: Boolean = false,
    onClick: () -> Unit,
) {
    val focus = rememberTileFocusState(initialFocus)
    Row(
        modifier = modifier
            .graphicsLayer { scaleX = focus.scale; scaleY = focus.scale }
            .clip(RoundedCornerShape(percent = 50))
            .background(if (focus.isFocused) focus.plate else Color.Transparent)
            .focusRequester(focus.focusRequester)
            .onFocusChanged { focus.onFocusChanged(it.isFocused) }
            .clickable(onClick = onClick)
            .semantics { this.contentDescription = contentDescription }
            .padding(horizontal = 22.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (icon != null) {
            TileIconGlyph(
                icon = icon,
                color = if (focus.isFocused) focus.onPlate else Cream,
                size = 34.dp,
            )
        }
        Text(
            text = label,
            color = if (focus.isFocused) focus.onPlate else Cream,
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
        )
    }
}
