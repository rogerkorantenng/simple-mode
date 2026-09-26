package com.simplemode.firetv.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.simplemode.firetv.family.QueryLog
import com.simplemode.firetv.intent.IntentMatch
import com.simplemode.firetv.intent.IntentResolver
import com.simplemode.firetv.intent.TileSummary
import com.simplemode.firetv.launch.Tile
import com.simplemode.firetv.launch.TileCatalog

/** Wires [TellMeScreen] to the query log and the tile catalogue, pulled
 *  out of [AppRoot] purely to keep that file's own `when` block short. */
@Composable
fun TellMeRoute(resolver: IntentResolver, onMatched: (Tile) -> Unit, onHome: () -> Unit) {
    val context = LocalContext.current
    TellMeScreen(
        resolver = resolver,
        tiles = TileCatalog.tiles.map { TileSummary(it.id, it.title, it.shortDescription) },
        onResolved = { utterance, result ->
            QueryLog.record(context, utterance, matchedTitle(result), result, System.currentTimeMillis())
        },
        onMatched = { _, result -> TileCatalog.findById(result.tileId)?.let(onMatched) },
        onHome = onHome,
    )
}

private fun matchedTitle(result: IntentMatch): String? =
    (result as? IntentMatch.Matched)?.tileId?.let { TileCatalog.findById(it)?.title }
