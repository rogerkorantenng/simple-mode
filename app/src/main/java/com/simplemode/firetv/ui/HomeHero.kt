package com.simplemode.firetv.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.simplemode.firetv.ui.theme.Charcoal
import com.simplemode.firetv.ui.theme.CreamMuted
import com.simplemode.firetv.ui.theme.FernTitle
import com.simplemode.firetv.ui.theme.HeroPlane

/**
 * The top band of the home screen: the app's name, what it has worked out
 * about the viewer's television, and the handful of controls that are not
 * places to watch something.
 *
 * It used to be a photograph of a front room running edge to edge, darkened
 * to about a tenth, with all of that sitting on top of it. The photograph is
 * gone. an internal review of this batch's three Fire TV apps together put this screen next to Profile
 * Gate's and Steady's and found the same six moves in the same order in all
 * three, beginning with a darkened full-bleed photograph across the top; the
 * one in this app carried no information and its real effect was to make an
 * app for somebody who cannot work a television the same shape as an app
 * about a parental gate.
 *
 * What replaced it first was the flat warm-charcoal plane the inner screens
 * use, closed with their same 3 dp fern rule. That fixed the photograph but
 * traded it for a new hard edge rather than an inherited one --
 * a set of reference photographs of a real Fire TV names this directly: "the hero
 * bleeds into the rows beneath it... no hard edge and no card boundary." So
 * the rule is gone and [HeroPlane] now fades down into [Charcoal], the
 * screen's own ground colour, over the band's full height -- the gradient
 * reaches [Charcoal] exactly at the bottom edge, so there is nothing left to
 * be a hard edge against. The delta between the two colours is small (an R
 * channel step of 7 in 255) precisely so this stays a flat-colour reading up
 * close and a bleed from three metres: the shared Fire TV craft reference's section 3's
 * warning about eight-bit banding is about *large* gradient ranges across a
 * wide panel, not this one. The only pictures left anywhere in Simple Mode
 * are the six tiles the viewer presses.
 *
 * Still not a carousel, for the reason it never was one: Simple Mode is for
 * somebody who cannot work a television, and a rotating advertisement is the
 * thing such a viewer is least able to deal with.
 */
@Composable
fun HomeHero(
    title: String,
    status: String,
    textScale: Float,
    marginDp: Int,
    modifier: Modifier = Modifier,
    navItems: @Composable RowScope.() -> Unit,
) {
    val stripDp = (NAV_STRIP_DP * textScale).dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(HeroPlane, Charcoal)))
            .clipToBounds(),
    ) {
        Column(modifier = Modifier.padding(top = 16.dp, bottom = 13.dp)) {
            Text(
                text = title,
                color = FernTitle,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = marginDp.dp),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(stripDp)
                    .padding(horizontal = marginDp.dp, vertical = 3.dp)
                    .focusGroup(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = navItems,
            )
            Text(
                text = status,
                color = CreamMuted,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 2,
                modifier = Modifier.padding(horizontal = marginDp.dp, vertical = 2.dp),
            )
        }
    }
}

// The status line sits under the controls, not under the wordmark.
//
// It used to be one or two grey lines immediately beneath the app's name,
// which is the third of the six moves an internal review of this batch's three Fire TV apps together found
// in the same order on all three of this repository's Fire TV home screens. It
// also put a sentence about captions a long way from the Captions control it
// describes. Down here, hard against the fern rule, it reads as a footnote to
// the band -- which is what it is: the state of the things directly above it.
//
// The band no longer takes a fixed height. It used to be 190 dp of a 540 dp
// canvas, a number with arithmetic behind it that broke the moment the screen
// title grew from 38sp to 50sp. It now measures its own content, and the wall
// beneath it scrolls if a large text setting ever pushes it past the fold --
// which at that setting the wall already does, because it drops to two columns.
private const val NAV_STRIP_DP = 56f
