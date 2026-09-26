package com.simplemode.firetv.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.simplemode.firetv.ui.theme.FernTitle

/**
 * One labelled block on [FamilyScreen]: a heading plus whatever content that
 * section turns out to have.
 *
 * The heading takes `headlineSmall`, not `titleLarge`. `titleLarge` is the
 * step a screen title takes, so three sections wearing it meant nothing on the
 * screen was clearly the name of the screen -- and at 38 sp a label sat above
 * its own 28 sp content, which reads as three screens stacked rather than
 * three sections of one. Fern green rather than muted cream, so the headings
 * are a different thing from the lines under them without being louder.
 */
@Composable
fun FamilySection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = title, color = FernTitle, style = MaterialTheme.typography.headlineSmall)
        content()
    }
}
