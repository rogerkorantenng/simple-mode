package com.simplemode.firetv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.simplemode.firetv.ui.theme.Charcoal

/** The whole app's ground: warm charcoal, unconditionally dark. Fire TV's
 *  night-mode qualifier never fires (`uimode=television` is permanent), so
 *  this is not a dark theme that could fail to apply -- it is the only
 *  theme. */
@Composable
fun FullScreenSurface(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(Charcoal)) {
        content()
    }
}
