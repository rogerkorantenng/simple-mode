package com.simplemode.firetv.ui

import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.simplemode.firetv.caregiver.TileOrderOps
import com.simplemode.firetv.caregiver.TileRepository
import com.simplemode.firetv.launch.TileCatalog
import com.simplemode.firetv.ui.theme.CreamMuted
import com.simplemode.firetv.ui.theme.FernTitle
import com.simplemode.firetv.Viewer

/**
 * The caregiver's catalogue editor, behind the PIN. Reading and writing go
 * through [TileRepository]; the list edits themselves go through the pure
 * functions in `TileOrderOps.kt`, so this Composable is only ever wiring.
 *
 * Three things were wrong and all three came from the same habit. The way out
 * was a full-width [TileCard] at 170 dp, which is a component sized for a
 * 255 dp tile and became a slab with a house hanging in space above a label at
 * the far left. That slab also claimed initial focus, so the screen opened with
 * the exit selected and the obvious next press sent the caregiver back where
 * they came from. And every "add a tile" line was a `Text` with `clickable`,
 * so it had no focus state a television could show.
 *
 * Home now floats on the header band, the way it does on every other screen in
 * the app; focus opens on the first row's first control; and adding a tile is a
 * [FocusableRow], which is the app's own full-width control.
 *
 * This screen is the one place scrolling is right: the catalogue is as long as
 * the household made it, and a caregiver setting a television up is not the
 * viewer the ten-foot rule is written for.
 */
@Composable
fun CaregiverCatalogScreen(onHome: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var ids by remember { mutableStateOf(TileRepository.currentGridTileIds(context)) }
    val home = remember { FocusRequester() }
    val firstAction = remember { FocusRequester() }

    fun update(newIds: List<String>) {
        ids = newIds
        TileRepository.setGridTileIds(context, newIds)
    }

    val available = TileCatalog.tiles.filterNot { it.id in ids }

    // Focus opens on the first row's first control, not on the way out.
    // `requestFocus()` throws while the node is unattached, which it is for the
    // first frame or two of a freshly navigated screen, so this retries for a
    // dozen frames -- the same claim loop every other screen in the app uses.
    LaunchedEffect(ids.firstOrNull()) {
        if (ids.isEmpty()) return@LaunchedEffect
        repeat(12) {
            if (runCatching { firstAction.requestFocus() }.isSuccess) return@LaunchedEffect
            withFrameNanos { }
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth()) {
            ScreenHeader(
                title = "What ${Viewer.NAME} sees",
                subtitle = "These are the tiles on ${Viewer.POSSESSIVE} home screen, in the order she sees them.",
            )
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
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 58.dp)
                .focusGroup(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 14.dp, bottom = 30.dp),
        ) {
            itemsIndexed(ids, key = { _, id -> id }) { index, id ->
                val tile = TileCatalog.findById(id)
                if (tile != null) {
                    CatalogRow(
                        position = index + 1,
                        title = tile.title,
                        onMoveUp = { update(TileOrderOps.moveUp(ids, id)) },
                        onMoveDown = { update(TileOrderOps.moveDown(ids, id)) },
                        onRemove = { update(TileOrderOps.remove(ids, id)) },
                        firstAction = if (id == ids.firstOrNull()) firstAction else null,
                    )
                }
            }
            if (available.isNotEmpty()) {
                item(key = "add-heading") {
                    Text(
                        text = "Add a tile",
                        color = FernTitle,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(top = 14.dp, bottom = 4.dp),
                    )
                }
                items(available, key = { "add-${it.id}" }) { tile ->
                    // Held to the same measure as the rows above, so the
                    // screen is one column of controls rather than a list of
                    // short rows followed by a list of full-width ones.
                    Box(modifier = Modifier.width(CATALOGUE_COLUMN)) {
                        FocusableRow(
                            label = tile.title,
                            contentDescription = "Add ${tile.title} to ${Viewer.POSSESSIVE} home screen.",
                            icon = TileIcon.Home,
                            onClick = { update(TileOrderOps.add(ids, tile.id)) },
                        )
                    }
                }
            }
            if (ids.isEmpty() && available.isEmpty()) {
                item(key = "empty") {
                    Text(
                        text = "${Viewer.POSSESSIVE} home screen is empty. Add a tile below.",
                        color = CreamMuted,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }
        }
    }
}

/**
 * Shared with [CatalogRow], so every control on this screen sits in one column.
 *
 * 820 dp of the 844 dp the side margins leave. It was 760 and "Norah's Shows"
 * ellipsised at the third row, which on a screen whose whole job is naming the
 * tiles is the worst possible thing to truncate.
 */
internal val CATALOGUE_COLUMN = 820.dp
