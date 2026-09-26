package com.simplemode.firetv.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.simplemode.firetv.accessibility.AccessibilityNeeds
import com.simplemode.firetv.family.QueryLog
import com.simplemode.firetv.family.WatchHistoryLog

/** Wires [FamilyScreen] to the two logs it reads, pulled out of [AppRoot]
 *  purely to keep that file's own `when` block short. */
@Composable
fun FamilyRoute(needs: AccessibilityNeeds, onEditCatalogue: () -> Unit, onHome: () -> Unit) {
    val context = LocalContext.current
    FamilyScreen(
        needs = needs,
        queries = QueryLog.recent(context),
        watched = WatchHistoryLog.recent(context),
        now = System.currentTimeMillis(),
        onEditCatalogue = onEditCatalogue,
        onHome = onHome,
    )
}
