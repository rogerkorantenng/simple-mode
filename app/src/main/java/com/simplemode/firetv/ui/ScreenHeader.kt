package com.simplemode.firetv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.simplemode.firetv.ui.theme.CreamMuted
import com.simplemode.firetv.ui.theme.FernEdge
import com.simplemode.firetv.ui.theme.FernTitle
import com.simplemode.firetv.ui.theme.HeroPlane

/**
 * The band across the top of every screen: a title, an optional second line,
 * and a fern rule underneath.
 *
 * There used to be a photograph of a front room in here, darkened to about a
 * tenth and running off the right edge. It is gone, and deleting it is the
 * single biggest thing this app has done to stop looking like the other two
 * Fire TV apps in this repository. an internal review of this batch's three Fire TV apps together put all
 * three home screens side by side and found the same six moves in the same
 * order, starting with a darkened full-bleed photograph across the top. The
 * photograph carried no information in any of them. Its only real effect here
 * was to give Simple Mode the same *shape* as an app about a parental gate and
 * an app about falls prevention.
 *
 * What is left is two flat planes and the rule between them, which is what
 * the depth was always coming from: an eight-bit gradient across a 1920-wide
 * television bands visibly (the shared Fire TV craft reference's section 3), and two dark surfaces that
 * differ only in luminance are one flat grey in a lit room, so the 3 dp fern
 * rule does the separating and clears 3:1 against both sides of itself.
 *
 * The rule is this app's own signature -- the set review names it as one of
 * the few things that is genuinely Simple Mode's -- so it stays, and the home
 * screen has now been given one too.
 *
 * With the photograph gone the artwork in this app lives only in its tiles.
 * That is what a set of reference photographs of a real Fire TV asks of this app in
 * particular: "its structure should stay simpler than this: everything visible
 * at once, no sideways scrolling, few destinations."
 *
 * The band sizes itself to whatever it is given rather than taking a height in
 * dp. Three call sites used to pass a number chosen by eye, and every one of
 * them had already been nudged once because a subtitle was clipping.
 *
 * The band ignores the side margins and runs to the screen edge, which is the
 * one case where crossing the overscan line is correct: "Don't adjust or clip
 * background screen elements to the overscan safe area." The text inside it
 * does not.
 */
@Composable
fun ScreenHeader(
    title: String,
    subtitle: String? = null,
    minHeightDp: Int = 96,
    textScale: Float = 1f,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = (minHeightDp * textScale).dp)
            .background(HeroPlane),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(horizontal = 58.dp)
                .padding(top = 10.dp, bottom = 13.dp),
        ) {
            Text(text = title, color = FernTitle, style = MaterialTheme.typography.titleLarge)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    color = CreamMuted,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 2,
                    modifier = Modifier.padding(top = 4.dp, end = 300.dp),
                )
            }
        }
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .height(3.dp)
                .background(FernEdge),
        )
    }
}
