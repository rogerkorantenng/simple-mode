# Simple Mode

A Fire TV app for a person who cannot work a television. Not a filter on top of
another app. Not a smarter remote. A front door: six to eight huge, voice-friendly
targets, a screen that sizes itself to the viewer without being told, a way to say
what you want in your own words when the grid is not enough, one button that gets
back to it from almost anywhere, and a PIN-gated way for the family member who set it
up to change what she sees, from the television itself.

## Who it is for

Someone who did not grow up with a five-button remote and a home screen full of app
icons: low vision, low dexterity, or simply unfamiliar with the device, being handed a
Fire TV Stick by an adult child or another family member who set it up for them. The
person who configures the device and the person who uses it are different people, and
the design assumes that split throughout.

## The one screen that carries the demo

The home screen: a status line reading the household's own declared captioning and
audio description settings straight off the platform, and a grid of large tiles below
it. Turn on captions in the TV's own Settings while the app is running, with no
interaction with Simple Mode at all, and the screen visibly grows: bigger type, a
thicker focus border. That is the whole pitch in one unbroken fifteen seconds: it
adapts to the person, not the other way around, and nobody had to tell it to.

The second half of the demo: open something else (the sample "Now Playing" screen
inside this app, or a real installed app), then press the small recovery button that
stays on screen, and land back on Simple Mode's home screen from wherever the TV had
wandered.

## What it does

- **Reads and subscribes to the viewer's own declared accessibility needs.**
  `CaptioningManager.isEnabled()` with a `CaptioningChangeListener`, and the
  `accessibility_audio_descriptions_enabled` secure setting with a `ContentObserver`,
  per Amazon's own documentation at
  `developer.amazon.com/docs/fire-tv/implement-reading-and-subscribing-to-adcc.html`.
  No setup wizard asks the viewer anything; the app finds out on launch and again the
  moment either setting changes.
- **Scales itself from that reading.** Captions on means larger type, larger spacing,
  and a heavier focus border throughout, not just inside a captions track. Audio
  description on means every tile's spoken label gets a short second sentence, on the
  theory that a household that asked for more said aloud wants more said everywhere,
  not just during playback.
- **Puts six to eight huge, D-pad-focusable targets on one screen**: Always Home, Tell
  Me, Live TV and Norah's Shows (hosted inside this app, so the demo never depends on what
  else happens to be installed), three real external-app launches by package name
  (Netflix, Prime Video, YouTube), and a shortcut into the TV's own accessibility
  settings. Call for Help is a real destination but does not take a grid slot -- it is
  reached by saying "help" to Tell Me, which is what let a new flagship tile fit
  without adding one; see `caregiver/TileRepository.kt`.
- **Gets back to itself from almost anywhere**, using a small persistent overlay
  button rather than a launcher-replacement or boot trick it cannot verify. See
  "The launch-on-boot question," below.
- **Fails a launch quietly.** An app that is not installed shows a plain "not set up
  on this TV yet" screen instead of crashing, because a caregiver's real Fire TV will
  not have everything installed either.
- **Lets her say what she wants, in her own words.** The "Tell Me" tile opens a text
  field; whatever she types (or dictates, if the remote's own voice input feeds the
  field the way Fire TV search boxes normally do) is matched against the tile
  catalogue and, on a match, the app acts exactly as if she had pressed that tile
  herself. See "Where the model is the product," below.
- **Lets the family member who set it up change what she sees, from the television,
  behind a PIN.** "Family setup" on the home screen leads to a transparency screen
  (what Tell Me has been asked, what she has watched, what the TV currently thinks her
  accessibility needs are) and, behind a four-digit PIN so it cannot be changed by
  accident, a plain list editor: move a tile up or down, remove one, add one back.
  This directly answers the gap an internal feature-depth review named as the single
  largest one in the first build: "the seven tiles are fixed in code," while the
  person who configures the device and the person who uses it are different people.

## What it deliberately does not do

- **It cannot fix another app's accessibility.** When VoiceView goes silent inside a
  third-party streaming app, Simple Mode cannot see that happen and cannot intervene.
  Nothing here inspects another app's video, audio, or caption track: DRM-protected
  frames never reach app-readable memory (`Display.FLAG_SECURE`), and the permission
  that would let an app capture another app's output
  (`CAPTURE_SECURE_VIDEO_OUTPUT`) is signature-level, granted only to apps signed
  with the platform key. There is no consent dialog and no Appstore review path that
  changes that. Simple Mode is an interface, never a filter, and the design never
  pretends otherwise.
- **It does not build a remote caregiver dashboard.** The idea this app is drawn from
  imagines the adult child checking, from their phone, what state the TV is in. That
  needs a backend, an account system and a pairing flow this hackathon window does not
  have room for. Out of scope; "Call for Help" is a static placeholder screen that says
  so. The catalogue editor built instead is deliberately *on the television*, not on a
  phone: it needs no backend, no account and no pairing, and it is reachable the same
  day the stick is unboxed.
- **It does not guarantee it is what appears when the television turns on.** See
  below.
- **Tell Me never sits on the path of pressing a tile.** The grid works exactly the
  same with Bedrock, the proxy, and the household's own network all absent. Natural
  language is the addition, never the only route, on purpose: a product for someone
  who cannot work a television cannot also require her to depend on a model answering
  in time.

## Where the model is the product

Tell Me is the one place in this app where a language model can change the outcome,
and per this build's own engineering brief's own rule ("add the model where it is the product, not as
decoration") it is the only place one appears. Matching "put on something funny with
her" to Norah's Shows, or "the sound is too quiet" to the caption settings, is a judgement
call between free text and a short, changing list of tile names -- exactly the kind of
matching a keyword search does badly and a model does well.

**Architecture.** The Android app never calls AWS directly -- shipping AWS secret keys
inside an APK is a real security problem, not a hypothetical one. Instead it POSTs the
utterance and the tile list to a small local proxy, `server/bedrock_proxy.py`, pure
Python standard library, which calls the real Bedrock Converse API. On the emulator
this is `10.0.2.2:8798`, the standard alias for host loopback; on a real Fire TV Stick
this would need to be a host on the household's own network, which is a real
limitation of this build, not solved here. The proxy is not mocked: every request is
a genuine call to Bedrock. What is mocked, and named as such in the tests, is the
Android-side unit test double behind the `IntentResolver` interface -- the interface
is the honest place to mock, matching this hackathon's "mock at the API boundary"
rule.

**Model preference chain.** `us.anthropic.claude-sonnet-4-6`, falling back to
`us.anthropic.claude-sonnet-4-5-20250929-v1:0`. Both were verified by an actual
`bedrock-runtime converse` invocation, not by `ListFoundationModels`, which is a real
distinction: `anthropic.claude-sonnet-5` and `anthropic.claude-opus-5` are listed by
that call but return `AccessDeniedException` on an actual invocation on this account.
Listed is not callable. Trying the newest model first and falling back down the list
means the build upgrades itself the day broader access lands, with no code change.

**Timeout and fallback, and which one is tested first.** `BedrockIntentResolver` times
out at 15 seconds; on any failure, `FallbackIntentResolver` drops to
`KeywordIntentResolver`, a pure, offline, word-overlap match with a small synonym list
for the phrasing this audience actually uses ("I can't hear" finds Captions even
though neither word is in that tile's own text). The grid itself never depends on any
of this: every tile is reachable exactly the same way whether Bedrock, the proxy, or
the household's network is present or absent, and that offline path was the first
thing tested, not the last, because a product for someone who cannot work a television
cannot also make her wait on a model to get home.

## The launch-on-boot question, settled honestly

The brief for this app flagged a real risk: no launch-on-boot or launcher-replacement
API appears anywhere in Fire TV's own documentation
(internal research into Fire TV's own documentation, section 2; a direct fetch of
`/docs/fire-tv/launch-on-boot.html` returns 404). That threatens the promise "one
button always gets you back here," because the obvious way to deliver it, replacing
the Fire TV launcher or auto-starting on boot, is not a capability Amazon documents
granting to a third party.

This build does not bet the core promise on either of those. Instead:

- **The primary mechanism is an on-screen overlay**, drawn with
  `TYPE_APPLICATION_OVERLAY` (`SYSTEM_ALERT_WINDOW`, granted once by the caregiver
  from Settings, exactly like a chat-head bubble). It sits on top of whatever is on
  screen, DRM-protected video included, because it never touches that app's window,
  only draws its own on top of the whole display. Pressing it always relaunches
  Simple Mode's home screen. This is a normal, non-signature Android mechanism, and it
  is the part of "always gets you back" this build can actually stand behind.
- **It is conditional, not permanent.** The button exists to bring a lost viewer
  back to Simple Mode, so it is absent while Simple Mode is already in front and
  present when it is not. The app learns which it is from its own activity
  lifecycle (`recovery/ForegroundWatcher.kt`), needing no permission: it never has
  to know *which* app is on screen, only whether it is itself, and
  `getRunningTasks` and `UsageStatsManager` are therefore both beside the point.
  Once up, the button holds full size for six seconds and then shrinks and fades
  towards the corner, so it stops covering someone else's picture. The limits of
  this — what a non-focusable overlay window can and cannot observe — are in
  `FRICTION.md`.
- **The manifest also registers a `BOOT_COMPLETED` receiver and a `HOME` /
  `DEFAULT` intent-filter**, both ordinary Android mechanisms, neither Fire-TV-specific
  and neither documented by Amazon one way or the other. They are shipped as a bonus
  layer, restarting the overlay after a reboot if the caregiver already granted the
  permission. Fire OS's own launcher does honour the third-party `HOME` filter —
  confirmed on a real Fire TV, not only inferred from the manifest. Whether Fire OS
  restricts background boot receivers the way several other OEM Android skins do —
  that is, whether the overlay actually restarts itself after a full power-cycle with
  no app opened first — has not specifically been put through that test yet; the
  real-hardware pass confirmed the HOME registration and the overlay button
  themselves, not a full reboot cycle. This build does not depend on the boot receiver
  working either way. A Reddit post in the research corpus asking whether
  launch-on-boot works at all on current Fire TV is corroboration, not proof, and is
  treated as such.
- **What a judge should watch for:** the demo shows the overlay button surviving a
  trip through a different screen and bringing the app back to Home. It does not show
  the app surviving a device reboot — that specific test, power-cycling a real Fire TV
  and confirming the overlay restarts unattended, has not been run. Real Fire TV
  hardware is now available, so this is the next thing to test, not a permanent gap.

## Visual direction

Ground: **warm charcoal**, near black but tinted brown rather than blue, so it sits
apart from the blue-black and purple-black grounds already used elsewhere in this
hackathon set. Accent: **fern green** at two depths, a deep fern for tiles and a
brighter fern for the one focus ring in the whole app. Text: **cream**, with a muted
cream-grey for secondary labels.

One hue family end to end, on purpose. A ten-foot interface for a low-vision viewer
three metres away, holding a five-button remote with no cursor anywhere, is not
well served by a busy palette: every extra hue is one more thing the eye has to sort
out before it can find the thing with focus. Fern green also reads as a "safe, get
me home" colour rather than an alarm or a brand colour, which fits a button whose
whole job is reassurance. Checked against an internal survey of this batch's other apps' screens: no other project in
this set uses green as an identity accent (the closest is a light cyan-teal mint, a
different hue entirely), and warm-charcoal-as-ground is unclaimed too (the two dark
grounds already in use elsewhere in the survey are both blue-purple tinted -- a dusk
purple and a plum-black -- not warm brown-black).

Typeface: **Atkinson Hyperlegible**, from the Braille Institute of America, under
the SIL Open Font License and bundled in the app. This build shipped in stock
Roboto until an internal review of this batch's three Fire TV apps together pointed out that two of the three
Fire TV apps here had no typeface at all, which is the flattest possible statement
that nobody chose one. Atkinson was drawn to be read by people with low vision --
I, l and 1 are three different shapes, b and d are not mirrored, the counters are
open -- so setting an app that claims to be for someone who cannot read her
television in it makes the claim checkable rather than asserted. One weight, Bold,
because the family has only Regular and Bold and the shared Fire TV craft reference's item 19
forbids Regular.

Type scale: **four sizes, 28 / 38 / 50 / 66**, at intervals of a perfect fourth.
There are four kinds of text on these screens -- what you read, what you press,
where you are, and the one thing a screen is about -- and there should be four
sizes. The other two Fire TV apps in this set ship six at intervals of about 1.25;
a viewer who cannot resolve the difference between two of them does not have a
hierarchy, she has noise. 28sp is the floor, which is the shared Fire TV craft reference's own
Simple-Mode body override and well clear of the hard 20sp floor in
internal research notes on Fire TV / Fire OS platform facts.

There is no photograph anywhere except inside a tile. The header band is a flat
warm-charcoal plane closed with a 3 dp fern rule, on the home screen and on every
screen under it. The darkened full-bleed photograph that used to run across the top
of both is the single thing that made this app the same *shape* as the other two,
and it carried no information; see an internal cleanup log for this app's screens.

Shell: a full-screen grid of large focusable tiles, the standard ten-foot TV pattern,
not any sibling's web dashboard shell (left sidebar, top nav, hero-with-cards) which
would be wrong for a five-button remote regardless of collision. Structure borrowed:
the well-proven "row/grid of big focusable targets" TV-launcher pattern. Palette is
this app's own.

Focus state is the one thing every TV app must get right and Simple Mode leans on
hardest: the brighter fern ring around a tile is the brightest, most saturated colour
anywhere in the app, used for nothing else, so "what has focus" never has to compete
with anything.

## Ten-foot craft

The first pass of this app was built and screenshotted against a phone emulator and
it showed: the shared Fire TV craft reference read the code afterward and found zero focus
modifiers anywhere in it -- no `focusable`, no `focusRequester`, no `focusProperties`
-- meaning every focus ring visible in those early screenshots was a Compose default
built for a fingertip, not a deliberate choice. That reference's own arithmetic (one
arcminute of visual angle at three metres covers 0.873 mm; a 2 dp ring is technically
visible and practically invisible) is why the fixes below are sized the way they are,
not a preference.

Addressed:

- **Three redundant focus channels on every tile**, per `TileFocusState.kt`: an 8 dp
  ring (above the reference's 6 dp floor), a 1.08x scale on `graphicsLayer` (so
  neighbours never reflow), and a brighter fill, all animating in 120 ms or less so
  they keep up with D-pad key repeat (~50 ms).
- **Every screen sets initial focus deliberately** via `FocusRequester` in a
  `LaunchedEffect(Unit)`, on the item the viewer most likely wants -- the render-with-
  nothing-focused state is what makes a remote feel dead, and VoiceView will choose
  badly if the app does not choose first.
- **Rows are `focusGroup()`s**, so a D-pad press cannot jump sideways out of the tile
  grid into a geometrically-closer but unrelated element.
- **Back always goes up one level**, via `BackHandler`, and every level up in this
  app's shallow hierarchy is Home -- matching the visible Home tile every screen
  already carries, rather than a breadcrumb stack a screen this simple does not need.
  From Home, Back is left to the platform default, so it exits to the Fire TV launcher
  instead of trapping the viewer.
- **Type sized against the reference's arcminute table**, including its stricter
  Simple-Mode-specific floor (body 28sp, meta 24sp, nothing below 20sp anywhere):
  see `ui/theme/Theme.kt`.
- **Content margins at 58 dp horizontal, 40 dp vertical**, the reference's checked
  grid arithmetic (58 + 12x52 + 11x20 + 58 = 960 dp exactly), well inside the 48/30 dp
  overscan floor rather than merely clearing it.
- The dead `isSystemInDarkTheme()` import the reference found is gone; this app was
  always unconditionally dark and now says why in the same file: Fire TV reports
  `uimode=television` and `night=notnight` permanently, so that qualifier never fires
  here regardless.

Not addressed, honestly: the reference's own `com.amazon.accessibility.*`
VoiceView extras (`describedBy`, `orientationText`, `usageHint.remote`) need
low-level `AccessibilityNodeInfo` access Compose does not expose directly, and were
judged lower-value than the focus and type fixes above given the time left; an
in-app manual text-size control (the reference's item 42) was designed but not built,
because the automatic adaptation from the viewer's own declared settings is this
app's actual mechanism and a second, manual one needs its own screen this pass did
not have room for; and the reference's "at most five focusable items on a primary
screen" is not met -- Home carries nine (eight tiles plus the Family setup link),
because an internal feature-depth review asked for the caregiver catalogue feature in
the same round that the shared Fire TV craft reference asked for a five-item ceiling on the same screen,
and resolving that tension by cutting a just-built, just-verified feature this late
was judged worse than naming the tradeoff here.

## Build target

Kotlin, Jetpack Compose, `minSdk`/`targetSdk` 28 (Fire OS 16 runs Android 9 / API 28,
per the brief). `compileSdk` 34 for current Compose tooling; this does not change the
API surface the app is allowed to call at runtime.

## Testing note

Built and run on `FireTV_API28`, a genuine Android TV system image at API 28 -- the
same API level Fire OS 16 runs -- at 1920x1080, density 320, matching Fire TV's own
reported display configuration exactly. Driven by `adb shell input keyevent` (19 up,
20 down, 21 left, 22 right, 23 centre, 4 back), never by a click, per
the shared Fire TV craft reference's own testing rule, because a mouse click hides exactly the
focus failures a D-pad exposes. Verified this way: initial focus landing on cold
start with no input at all, directional focus movement with all three channels
animating together, Back returning to Home from a nested screen and exiting the app
from Home, the overlay recovering across a real app boundary, live typed text via the
on-screen keyboard resolving through both the real Bedrock path and the offline
keyword fallback, and the caregiver PIN and catalogue-editing flow (set a PIN, reorder
two tiles, remove one, confirm it is gone) end to end.

Earlier in this build, before `FireTV_API28` existed, testing ran on a phone-shaped
emulator; that pass is why the ten-foot and focus problems the shared Fire TV craft reference found were
there to find, and `FRICTION.md` keeps that account rather than rewriting
it as if the real emulator had been available from the start.

Developed against the API 28 emulator and verified on real Fire TV hardware: the app
was built for Fire OS and later run on a real Fire TV, including the overlay recovery
button and the HOME registration described above. This machine hosts several other
agents' Android emulators concurrently, which is why day-to-day development stayed on
the emulator rather than the device -- a real, named limit, not a hidden one -- see
`FRICTION.md` for what that cost in testing time.

56 unit tests, all passing: accessibility adaptation, tile navigation, the caregiver
catalogue's pure list operations, the intent resolver's offline keyword matching and
its Bedrock-then-fallback wiring (with a real coroutine timeout, not a simulated one),
and the PIN's hashing.

Licence: MIT.
