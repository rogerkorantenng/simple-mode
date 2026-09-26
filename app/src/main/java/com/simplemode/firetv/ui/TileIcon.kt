package com.simplemode.firetv.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The app's own icons, drawn rather than typed.
 *
 * The first pass used characters: full-colour emoji for some tiles and
 * black text dingbats for others, on the same screen. Three separate
 * problems, and the third is the one that mattered most here. They are
 * rendered by whatever font the device ships, so they carry Google's or
 * Amazon's drawing rather than this app's and change from device to
 * device. At three metres on a 1080p panel they read as placeholders,
 * which is what they were. And this is an app whose whole claim is that
 * somebody thought carefully about a viewer who cannot work a television;
 * a borrowed icon argues the opposite of that, for free, on the first
 * screen a judge sees.
 *
 * So: one stroke weight, one cap and join treatment, one colour, drawn
 * to a 24-unit square and scaled to sit beside the type. Nothing here is
 * elaborate -- a television, a bookmark, a house -- because at this
 * distance detail is lost anyway and only the silhouette survives.
 */
enum class TileIcon {
    Home,
    HomeOn,
    Speak,
    Screen,
    Bookmark,
    Play,
    Captions,
    Phone,
    Lock,
}

/** Every tile in the catalogue gets an icon from this one set, including
 *  any a caregiver adds, so nothing can fall back to a character. */
fun tileIconFor(id: String, isExternalApp: Boolean, isSystemSetting: Boolean): TileIcon = when {
    id == "tell_me" -> TileIcon.Speak
    id == "live_tv" -> TileIcon.Screen
    id == "her_shows" -> TileIcon.Bookmark
    id == "call_for_help" -> TileIcon.Phone
    isSystemSetting -> TileIcon.Captions
    isExternalApp -> TileIcon.Play
    else -> TileIcon.Screen
}

private const val GRID = 24f
private const val STROKE_UNITS = 2.1f

@Composable
fun TileIconGlyph(icon: TileIcon, color: Color, size: Dp, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(size)) {
        val unit = this.size.minDimension / GRID
        val stroke = Stroke(
            width = STROKE_UNITS * unit,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        )
        drawTileIcon(icon, color, unit, stroke)
    }
}

private fun DrawScope.drawTileIcon(icon: TileIcon, color: Color, u: Float, stroke: Stroke) {
    fun p(x: Float, y: Float) = Offset(x * u, y * u)
    fun line(x1: Float, y1: Float, x2: Float, y2: Float) =
        drawLine(color, p(x1, y1), p(x2, y2), stroke.width, stroke.cap)
    fun path(build: Path.() -> Unit) = drawPath(Path().apply(build), color, style = stroke)
    fun roundRect(x: Float, y: Float, w: Float, h: Float, r: Float) = path {
        addRoundRect(
            androidx.compose.ui.geometry.RoundRect(
                Rect(Offset(x * u, y * u), Size(w * u, h * u)),
                androidx.compose.ui.geometry.CornerRadius(r * u, r * u),
            ),
        )
    }

    when (icon) {
        TileIcon.Home, TileIcon.HomeOn -> {
            path {
                moveTo(3f * u, 10f * u)
                lineTo(12f * u, 3f * u)
                lineTo(21f * u, 10f * u)
                lineTo(21f * u, 20f * u)
                lineTo(3f * u, 20f * u)
                close()
            }
            if (icon == TileIcon.HomeOn) {
                // The overlay is on. A tick inside the house, not a
                // different character: same silhouette, one added mark, so
                // the tile does not appear to change into something else.
                path {
                    moveTo(8.5f * u, 14.5f * u)
                    lineTo(11f * u, 17f * u)
                    lineTo(16f * u, 11.5f * u)
                }
            }
        }

        TileIcon.Speak -> path {
            // Speech bubble: rounded body with a tail dropped from the
            // lower left, drawn as one outline so the join reads cleanly
            // at distance.
            moveTo(6f * u, 18f * u)
            lineTo(6f * u, 21f * u)
            lineTo(10f * u, 18f * u)
            lineTo(18f * u, 18f * u)
            cubicTo(19.7f * u, 18f * u, 21f * u, 16.7f * u, 21f * u, 15f * u)
            lineTo(21f * u, 8f * u)
            cubicTo(21f * u, 6.3f * u, 19.7f * u, 5f * u, 18f * u, 5f * u)
            lineTo(6f * u, 5f * u)
            cubicTo(4.3f * u, 5f * u, 3f * u, 6.3f * u, 3f * u, 8f * u)
            lineTo(3f * u, 15f * u)
            cubicTo(3f * u, 16.7f * u, 4.3f * u, 18f * u, 6f * u, 18f * u)
            close()
        }

        TileIcon.Screen -> {
            // A set with its aerial up: the silhouette a viewer of this
            // age reads as "television" faster than a rectangle does.
            roundRect(2.5f, 9f, 19f, 11.5f, 2.5f)
            line(8f, 9f, 12f, 5f)
            line(16f, 9f, 12f, 5f)
        }

        TileIcon.Bookmark -> path {
            moveTo(6f * u, 3.5f * u)
            lineTo(18f * u, 3.5f * u)
            lineTo(18f * u, 20.5f * u)
            lineTo(12f * u, 15.5f * u)
            lineTo(6f * u, 20.5f * u)
            close()
        }

        TileIcon.Play -> path {
            moveTo(8f * u, 5f * u)
            lineTo(19f * u, 12f * u)
            lineTo(8f * u, 19f * u)
            close()
        }

        TileIcon.Captions -> {
            // Words on a screen. Two bars rather than the letters "CC",
            // which at this distance is two smudges.
            roundRect(2.5f, 5f, 19f, 14f, 2.5f)
            line(6.5f, 10.5f, 12f, 10.5f)
            line(6.5f, 14f, 17.5f, 14f)
        }

        TileIcon.Phone -> path {
            // A handset, tilted, in the same single-outline language.
            moveTo(5f * u, 4f * u)
            lineTo(9f * u, 4f * u)
            lineTo(10.5f * u, 9f * u)
            lineTo(8f * u, 11f * u)
            cubicTo(9.5f * u, 14.5f * u, 12f * u, 16.5f * u, 15f * u, 17.5f * u)
            lineTo(17f * u, 15f * u)
            lineTo(21f * u, 16.5f * u)
            lineTo(21f * u, 20f * u)
            cubicTo(13f * u, 20f * u, 5f * u, 13f * u, 5f * u, 4f * u)
            close()
        }

        TileIcon.Lock -> {
            roundRect(5f, 11f, 14f, 9.5f, 2f)
            path {
                // Shackle: a half-circle standing on two uprights.
                moveTo(8.5f * u, 11f * u)
                lineTo(8.5f * u, 8f * u)
                cubicTo(8.5f * u, 5.5f * u, 10f * u, 4f * u, 12f * u, 4f * u)
                cubicTo(14f * u, 4f * u, 15.5f * u, 5.5f * u, 15.5f * u, 8f * u)
                lineTo(15.5f * u, 11f * u)
            }
        }
    }
}
