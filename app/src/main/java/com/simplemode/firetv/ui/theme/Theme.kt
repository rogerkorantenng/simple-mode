package com.simplemode.firetv.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.simplemode.firetv.R

// One hue family end to end: warm charcoal ground, fern green at two tints,
// cream text. Deliberately narrow. See SPEC.md "Visual direction" for why.
// Colours are unconditionally dark: Fire TV reports uimode=television and
// night=notnight permanently, so `values-night` and isSystemInDarkTheme()
// never fire here. There is no light mode to fall back to and none is
// offered -- see the shared Fire TV craft reference's section 1.
val Charcoal = Color(0xFF1C1A17)
val CharcoalElevated = Color(0xFF2A2620)
val CharcoalFocused = Color(0xFF3A342A)
val Fern = Color(0xFF4F7A52)
// Screen titles. #4F7A52 measured 3.5:1 against the ground, which made the
// one word telling the viewer where she is the dimmest text on the screen --
// backwards in an app for a low-vision audience. #6FA06A is 5.72:1 and still
// sits well below the focus ring, so SPEC.md's claim that the ring is the
// brightest colour in the app stays literally true.
val FernTitle = Color(0xFF6FA06A)
// The resting edge of every tile. A tile fill of CharcoalElevated on the
// Charcoal ground separates by 1.15:1 -- one flat grey at three metres in a
// lit room. the shared Fire TV craft reference's item 29 forbids separating two surfaces
// by luminance alone, so tiles carry a border instead: 4.46:1 against the
// ground and 3.86:1 against their own fill, clearing 3:1 on both sides.
val FernEdge = Color(0xFF5E8C60)
val FernBright = Color(0xFF8FBF83)
val Cream = Color(0xFFFBF3E7)
val CreamMuted = Color(0xFFC9C0B2)

// The three planes the home screen is built from, darkest first. Plinth is
// the band the tile artwork stands on and the band the label sits in, so it
// matches the bottom of every illustration exactly; swapping it for
// FernBright is the whole focus mechanism (see TileFocusState). HeroPlane
// and NavPlane are the top band and the strip of controls that floats on
// it -- two flat planes, no gradient, because an eight-bit gradient across
// a 1920-wide panel bands visibly (the shared Fire TV craft reference's section 3).
val Plinth = Color(0xFF14120F)
val HeroPlane = Color(0xFF231F16)
val NavPlane = Color(0xFF2F2A20)

private val SimpleModeColors = darkColorScheme(
    primary = Fern,
    onPrimary = Cream,
    secondary = FernBright,
    onSecondary = Charcoal,
    background = Charcoal,
    onBackground = Cream,
    surface = CharcoalElevated,
    onSurface = Cream,
    surfaceVariant = CharcoalElevated,
    onSurfaceVariant = CreamMuted,
    // Compose's OutlinedTextField reaches for surfaceContainerHighest, and a
    // darkColorScheme() that does not name it inherits Material's own
    // blue-tinted neutral. That put the app's only cool colour on its two
    // text fields, including the PIN gate. Named here so the one hue family
    // SPEC.md argues for survives contact with a stock component.
    surfaceContainerHighest = CharcoalElevated,
    surfaceContainerHigh = CharcoalElevated,
    surfaceContainer = CharcoalElevated,
    outline = FernEdge,
    outlineVariant = FernEdge,
)

/**
 * Scale multiplier applied to every piece of type and every touch target on
 * screen. Driven by the viewer's own declared accessibility needs rather than
 * a setting inside this app. See [com.simplemode.firetv.accessibility.AccessibilityNeeds].
 */
data class UiScale(val text: Float, val spacing: Float, val borderWidth: Float)

@Composable
fun SimpleModeTheme(
    uiScale: UiScale = UiScale(text = 1f, spacing = 1f, borderWidth = 1f),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = SimpleModeColors,
        typography = simpleModeTypography(uiScale.text),
        content = content,
    )
}

/**
 * Atkinson Hyperlegible, from the Braille Institute of America, under the SIL
 * Open Font License. Licence and provenance are in this app's ATTRIBUTION.md.
 *
 * Simple Mode shipped in stock Roboto until now, which is to say it had no
 * typeface -- the thing an Android app renders in when nobody chose one. That
 * is also what Profile Gate does, and it is one of the three reasons
 * an internal review of this batch's three Fire TV apps together reads the set as one product.
 *
 * Of every face that is free to bundle, this one is the only one drawn for the
 * problem this app is about. The Braille Institute commissioned it to be read
 * by people with low vision, and it works by pulling apart the letter pairs
 * that collapse into each other when sight is poor: the I, l and 1 are all
 * different shapes, the b and d are not mirrored, the tail of the a and the
 * spur of the g are exaggerated, and the counters are open. Simple Mode claims
 * to be an interface for someone who cannot read her television. Setting it in
 * a face designed for exactly that makes the claim checkable rather than
 * asserted, which is a better argument in front of a judge than another
 * paragraph saying so.
 *
 * It is also nothing like Steady's Libre Franklin, which is a grotesque with
 * closed apertures and a narrow, even rhythm.
 *
 * **One weight, and the family is the reason.** Atkinson Hyperlegible ships
 * Regular and Bold and nothing in between. the shared Fire TV craft reference's item 19
 * forbids Regular outright -- thin strokes are the first thing a lit room
 * erases -- so there is exactly one weight this app can set, and it sets it
 * everywhere. Hierarchy here comes from size and colour, which suits a screen
 * with four sizes and three text colours on it. Regular is bundled anyway so
 * that a stray `FontWeight.Normal` renders in the family rather than dropping
 * silently back to Roboto, which is how the old default got in.
 */
val AtkinsonHyperlegible = FontFamily(
    Font(R.font.atkinson_hyperlegible_regular, FontWeight.Normal),
    // Bold is registered at Medium and SemiBold too. Compose resolves a weight
    // it has no file for by picking the nearest, and for Medium (500) "nearest"
    // resolves downwards to Regular -- so without these two lines every step in
    // the scale below would quietly render in the one weight the shared Fire TV craft reference forbids.
    Font(R.font.atkinson_hyperlegible_bold, FontWeight.Medium),
    Font(R.font.atkinson_hyperlegible_bold, FontWeight.SemiBold),
    Font(R.font.atkinson_hyperlegible_bold, FontWeight.Bold),
)

/**
 * Four sizes: **28, 38, 50, 66**.
 *
 * This app used to ship six -- 72/48/38/32/28/24 -- which is Profile Gate's
 * and Steady's six numbers moved up one step, with the same comment in all
 * three files citing the shared Fire TV craft reference as the source. That document is
 * a floor and it had been read as a specification, so three products that have
 * nothing to do with each other set a screen title at the same size in the
 * same weight and read as one product.
 *
 * These four do not come from that document. They come from who is reading
 * the screen. Simple Mode is for a viewer who cannot work her television and
 * may not be able to see it well; every other app in this repository is for
 * somebody competent. Two things follow, and neither of them follows for the
 * other two apps:
 *
 * **Fewer steps.** There are four kinds of text in this app and there should
 * be four sizes, not six. The two smallest steps had collapsed into a
 * distinction nobody could act on -- 28sp body against 24sp meta is a sixth of
 * a size at three metres, a difference the viewer cannot see and therefore
 * cannot use. They are now one step, and what separates a caption from a
 * sentence is colour, which is visible.
 *
 * **Wider intervals.** The ratio is a perfect fourth, about 1.33, against the
 * roughly 1.25 the other two use. That is deliberate: a scale whose steps are
 * further apart is a scale whose steps are still distinguishable when sight is
 * poor, and a hierarchy you cannot resolve is not a hierarchy.
 *
 * - **28** what you read. Body copy, tile labels, the status line, every
 *   caption. the shared Fire TV craft reference's Simple-Mode body override, kept because it is a
 *   floor and it is already the right answer. Well clear of the hard 20sp
 *   floor in internal research notes on Fire TV / Fire OS platform facts.
 * - **38** what you press. Rows, keys, section headings, buttons.
 * - **50** where you are. The screen title and the app's own name, and
 *   nothing else.
 * - **66** the one thing this screen is about, used on the two screens that
 *   have exactly one thing on them.
 *
 * Line heights run 1.36 at the reading size down to 1.21 at the largest, which
 * is the usual shape: long text needs help finding the start of the next line,
 * a two-word title does not.
 */
private fun simpleModeTypography(scale: Float) = androidx.compose.material3.Typography(
    displayLarge = TextStyle(fontSize = (66 * scale).sp, lineHeight = (80 * scale).sp, fontWeight = FontWeight.Bold, fontFamily = AtkinsonHyperlegible),
    headlineLarge = TextStyle(fontSize = (66 * scale).sp, lineHeight = (80 * scale).sp, fontWeight = FontWeight.Bold, fontFamily = AtkinsonHyperlegible),
    titleLarge = TextStyle(fontSize = (50 * scale).sp, lineHeight = (62 * scale).sp, fontWeight = FontWeight.Bold, fontFamily = AtkinsonHyperlegible),
    headlineSmall = TextStyle(fontSize = (38 * scale).sp, lineHeight = (48 * scale).sp, fontWeight = FontWeight.Bold, fontFamily = AtkinsonHyperlegible),
    bodyLarge = TextStyle(fontSize = (28 * scale).sp, lineHeight = (38 * scale).sp, fontWeight = FontWeight.Bold, fontFamily = AtkinsonHyperlegible),
    labelLarge = TextStyle(fontSize = (28 * scale).sp, lineHeight = (38 * scale).sp, fontWeight = FontWeight.Bold, fontFamily = AtkinsonHyperlegible),
)
