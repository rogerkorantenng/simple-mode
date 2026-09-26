# Friction log — Simple Mode

Written as it happened, not reconstructed afterward. Entries run in the order they were
found, so the toolchain papercuts come first and the platform findings come last. If you
are reading this to learn something about Fire TV rather than about us, start at
"The route we were told to film on runs a different operating system", near the bottom.
That one changed what we could enter.

## Kotlin Gradle plugin 2.4.20 rejects the old `kotlinOptions { jvmTarget = "17" }` DSL

**Task**: first build of the app module (`:app:testDebugUnitTest :app:assembleDebug`).

**Steps**: wrote `app/build.gradle.kts` with the conventional
`android { kotlinOptions { jvmTarget = "17" } }` block, the form used in nearly every
Android tutorial and in another project's own file layout, checked for version guidance.

**Expected**: it compiles.

**Actual**: hard failure at script-compilation time, before a single source file
built: `Using 'jvmTarget: String' is an error. Please migrate to the compilerOptions
DSL.` Kotlin Gradle Plugin 2.4.20 has turned a long-standing deprecation warning into
a build-breaking error.

**Severity**: medium. Cost about ten minutes: one failed build, one doc lookup, one
rewrite.

**Workaround**: replaced the `kotlinOptions` block with the new top-level `kotlin {
compilerOptions { jvmTarget.set(JvmTarget.JVM_17) } }` block, importing
`org.jetbrains.kotlin.gradle.dsl.JvmTarget` in `build.gradle.kts`.

**Suggestion**: nothing here is Amazon's fault, it is the Kotlin Gradle Plugin, but it
is exactly the kind of toolchain drift that eats a hackathon's clock. Any starter
template Amazon publishes for Fire TV Kotlin/Compose apps should be pinned to a
`build.gradle.kts` that already uses the current `compilerOptions` DSL, not the form
every existing tutorial still shows.

## No working Android TV emulator image was available locally

**Task**: get a ten-foot, D-pad-navigable runtime to test Simple Mode's focus
handling and layout against, at API 28. (We believed at the time that API 28 was
current Fire OS. It is Fire OS 7. See the entry on Amazon's two answers to that
question, below.)

**Steps**: `avdmanager list target` and a directory listing of
`$ANDROID_HOME/system-images` both show `android-28;android-tv;x86` present on disk.
Ran `avdmanager create avd -k "system-images;android-28;android-tv;x86"`.

**Expected**: an Android TV AVD at API 28, matching this build's own engineering brief's target almost exactly.

**Actual**: `avdmanager` rejected the package path as invalid, listing only two
`google_apis_playstore` images (36 and 36.1) as valid. The `android-28/android-tv/x86`
directory on disk turned out to contain only a 12KB `.installer/.installData` stub,
not the actual system image; a previous, unrelated download had been started and never
finished, and `avdmanager`'s local repository index does not know it exists.

**Severity**: medium. It meant the one runtime that would have matched the target
platform exactly was not available, and re-downloading it competes for the same
disk/network/memory budget this build's own engineering brief asks to ration carefully across the whole
hackathon.

**Workaround**: built and ran against a phone-shaped API 28 (`google_apis`, x86_64)
AVD instead, which validates the Kotlin/Compose/Android logic, the accessibility
listener wiring, and the navigation graph, but not true D-pad focus traversal on a
ten-foot layout. SPEC.md and this log say so plainly rather than claiming TV hardware
verification that did not happen.

**Suggestion**: check any pre-provisioned Android SDK image cache for completeness
(`source.properties` / `package.xml` present, not just a directory) before relying on
`avdmanager list target`, which only trusts its own repository index and will not
surface a partially-downloaded package as either available or broken -- it just
silently excludes it.

**Update, later in the session**: tried downloading `system-images;android-28;android-tv;x86`
properly with `sdkmanager --install`, in the background, off the critical path. After
27 minutes it had reached 16% of the zip. At that rate the download alone would have
taken close to two hours, against a hackathon clock and a machine several other agents
are actively building on. Killed it and finished on the phone-shaped emulator instead.
Google's own system-image mirror was simply slow from this network on this day; nothing
to fix on the app side, but worth recording as the reason the TV-exact runtime was
never reached, not "did not try."

## The obvious tile-height bug a screenshot caught in thirty seconds

**Task**: verify the home screen actually looks right, not just that it compiles.

**Steps**: first on-device screenshot showed the "Turn On: Always Get Home" tile's own
label sliced off mid-word, hidden by the tile's rounded-corner clip. The label was
long, the `Column` holding glyph + title had no line limit, and the tile's `Box` used
`.clip(RoundedCornerShape(...))`, so overflow was silently cut rather than visibly
truncated or wrapped.

**Expected**: nothing -- this is the kind of bug that should never have shipped to a
screenshot in the first place.

**Severity**: high, had it gone unnoticed: on a ten-foot interface for someone who
cannot read past a first glance, an unreadable button label is not cosmetic, it is the
whole product failing at its one job.

**Workaround/fix**: shortened the label to match the length of every other tile ("Her
Shows", "Netflix"), added `maxLines`/`overflow` as a safety net on every tile's title,
and separately found that `LazyVerticalGrid`'s items were not receiving a bounded
height from their parent `Column` at all, so a second bug (captions-on's 1.25x text
boost pushing row-two's labels below the visible, non-scrolling area) was hiding behind
the first one. Replaced the `LazyVerticalGrid` with a plain, non-lazy `Column` of `Row`s
inside a `verticalScroll` container -- there are only eight tiles, nothing here needed
virtualisation, and the simpler layout does not have a nested-scrollable-sizing puzzle
to get wrong. Tile height is now computed from the same `UiScale` that drives the text
and spacing boost, so the box grows with its own contents instead of clipping them.

**Suggestion**: this is a self-inflicted process note, not a platform complaint: screen
a build against its own screenshots before calling a UI "done," especially for an
accessibility-first product where "the text is there, just cut off" is worse than an
error message.

## `Gravity.END` inverts the x-offset sign, and it will put your overlay off-screen silently

**Task**: get the recovery overlay button to actually appear on screen.

**Steps**: first version used `WindowManager.LayoutParams` with
`gravity = Gravity.BOTTOM or Gravity.END` and a positive `x = 48` inset, plus
`FLAG_LAYOUT_NO_LIMITS`. The service started cleanly, `dumpsys activity services`
showed it running, no exception anywhere in logcat -- and the button was nowhere on
screen.

**Expected**: a button inset 48px from the bottom-right corner.

**Actual**: `Gravity.END`'s x-offset moves the window further past the anchored edge,
not inward from it, so a positive `x` pushes an END-anchored window off the right edge
of the display entirely. Combined with `FLAG_LAYOUT_NO_LIMITS` (which stops the system
clamping the window back on screen), the button rendered at a real but fully
off-display position. There is no error for this: the window exists, is not obscured,
and `dumpsys window windows` reports it as visible the whole time, which is exactly
what makes it hard to catch without a screenshot.

**Severity**: high. This is the core mechanic the coordinator specifically asked to be
established early and designed around; a silently-invisible recovery button is worse
than an honest failure, because nothing in the running app says it is broken.

**Workaround**: switched to `Gravity.BOTTOM or Gravity.START`, where a positive x/y
inset is unambiguous ("further onto the screen" in both cases), and dropped
`FLAG_LAYOUT_NO_LIMITS` since nothing here needs the window to extend past the display.
Verified by screenshotting a real different app (a sibling hackathon project's own
onboarding screen, mid-test on the same shared emulator) with the green "Simple Mode"
button floating over it, tapping it, and confirming via
`dumpsys activity activities | grep topResumedActivity` that focus returned to
`MainActivity`.

**Suggestion**: for any TYPE_APPLICATION_OVERLAY window, prefer `START`/`TOP` gravity
over `END`/`BOTTOM`-with-positive-offset, or test the actual on-screen position with a
screenshot before trusting `dumpsys`'s "visible" flag -- it answers "does this window
exist in the compositor," not "can a person see it."

## A force-stop kills the foreground service, but the UI's own memory of "it's on" survives the process

**Task**: make the "Always Home" toggle state trustworthy after Android kills the app's
process (a real event on any device, not just a test artefact -- background app
reclaim happens on real Fire TV hardware too).

**Steps**: toggled the overlay on, then `am force-stop`'d the app and relaunched
`MainActivity`. The home screen still showed "Always Home" as on (checkmark glyph),
but `dumpsys activity services` showed no `RecoveryOverlayService` running, and no
button was on screen anywhere.

**Expected**: either the service restarts, or the UI honestly shows "off."

**Actual**: the in-memory `overlayEnabledState` was seeded once from
`OverlayPermission.isGranted(this)` in `onResume` -- which only checks whether the
*permission* is granted, not whether the *service* is actually running. Permission
grants survive a force-stop; the foreground service does not. The UI was reporting a
promise the app was no longer keeping.

**Severity**: high, for the same reason as the gravity bug: this is the one feature the
whole app is judged on, and it was lying about its own state.

**Fix**: added `RecoveryPreference`, a small `SharedPreferences`-backed flag for
"the caregiver turned this on," persisted independently of the process. `onResume` now
derives the displayed state from `permission granted AND preference says on`, every
time, and proactively restarts the service under that same condition rather than
trusting whatever was true when the toggle was last touched. `BootReceiver` was updated
to check the same flag, so it no longer restarts an overlay nobody asked for just
because the permission happens to still be granted.

**Suggestion**: for any "is this background thing running" UI state backed by a
service, treat the permission check and the running-service check as two different
questions, and re-derive both on every resume rather than caching either.

## Sharing an emulator with a sibling agent produces confusing, silent test results

**Task**: verify the recovery button across an app switch.

**Steps**: mid-testing, `adb -s emulator-5554 shell dumpsys activity activities` started
reporting a different app's own main screen as the top activity, and a screenshot showed
another Fire TV app's own onboarding screen instead of anything this session had
launched. A second AVD (`Pixel_6a_rec2`) also appeared under `adb
devices` partway through, launched by whichever agent owns that project.

**Severity**: low-to-medium. No data was lost or corrupted -- the two apps have
different package names -- but it cost real time chasing what looked like a rendering
bug (an invisible overlay button) that was actually just `adb`'s default device
targeting picking up a device state a different agent had changed.

**Workaround**: always pass `-s emulator-5554` explicitly from that point on, and
treat any screenshot showing unfamiliar UI as a signal to check `adb devices` for a
second attached device before assuming the app under test is broken.

**Suggestion**: worth calling out in `this build's own engineering brief` alongside the Gradle
one-at-a-time rule: if several agents may be building Android apps in parallel, each
should be told to create and target its own uniquely-named AVD, not the first
`emulator-NNNN` that answers.

## an internal survey of this batch's other apps' screens arrived mid-build, after the first palette was already coded into resources

**Task**: pick a visual identity that does not collide with the fourteen other
projects already built in this hackathon set.

**Steps**: this build's own engineering brief says "the UI has to be genuinely good ... pick a visual
direction ... and write it down in the spec before you write components," but does
not point at an internal survey of this batch's other apps' screens by name or say it exists. Colours were chosen
(a warm amber accent) and written into `colors.xml` and a launcher icon drawable
before the coordinator's message surfaced the inventory file and its existing amber-
and-orange-family entries (an apricot and an orange already claimed elsewhere in that survey).

**Severity**: low. Caught before any screen was actually laid out, so the fix was a
five-file find-and-replace, not a redesign. Would have been more expensive an hour
later.

**Workaround**: re-picked a fern-green accent against the same warm-charcoal ground,
checked the new pair against the inventory, and wrote the rationale into SPEC.md.

**Suggestion**: this build's own engineering brief's design section should name an internal survey of this batch's other apps' screens
directly, the same way it names internal research notes on Fire TV / Fire OS platform facts, since it is exactly the
kind of file this build's own engineering brief says not to re-derive.

## `ListFoundationModels` lists models this account cannot actually invoke

**Task**: pick a Bedrock model id for the "Tell Me" intent resolver, following the
coordinator's first brief, which named `anthropic.claude-sonnet-5` as "live and
verified on this machine."

**Steps**: called `aws bedrock-runtime converse --model-id anthropic.claude-sonnet-5`
directly, before writing any server code, on the general principle of proving the
external dependency works before building against it.

**Expected**: a response, matching this build's own engineering brief's claim.

**Actual**: `AccessDeniedException: anthropic.claude-sonnet-5 is not available for
this account.` The model id is real and appears in `list-foundation-models`; it is
simply not callable by this account. `anthropic.claude-opus-5` fails the same way.
Two inference-profile ids, `us.anthropic.claude-sonnet-4-6` and
`us.anthropic.claude-sonnet-4-5-20250929-v1:0`, both work, verified the same way.

**Severity**: low, because it was caught before any code was written against the
wrong id, purely by testing the dependency first instead of trusting this build's own engineering brief. Had
`bedrock_client.py` been written against `anthropic.claude-sonnet-5` untested, the
whole Tell Me feature would have silently always fallen back to the offline keyword
matcher, with the demo looking identical either way and the AI feature simply never
running -- a failure mode with no visible symptom, which is the worst kind.

**Suggestion**: for this specific mistake, the fix already happened -- the coordinator
independently caught it and corrected this build's own engineering brief before I reported it, and named a
model preference chain (newest first, fall back down the list) as the right shape,
which `bedrock_client.py` now implements. The general lesson stands for the next
round: `ListFoundationModels`/`ListInferenceProfiles` answer "does this account know
this model exists," not "can this account call it," and nothing in either response
distinguishes the two. Verify with a real, cheap invocation before writing anything
against a model id, not after.

## A committed file nearly shipped an AWS account number

**Task**: document `server/bedrock_proxy.py`'s reasoning for existing (a thin proxy
instead of embedding AWS keys in the APK) in its own doc comment.

**Steps**: named the specific AWS account number in that comment, for concreteness,
since it was already visible in the conversation's own context.

**Actual**: this `apps/` directory is a public repository once submitted, and a
secrets scan the coordinator ran found the account number sitting in a comment in
committed code. Caught and removed before submission, not after.

**Severity**: medium. An AWS account number alone is not a credential, but it is
exactly the kind of thing a secrets scanner is built to catch and a judge would
notice, and it should never have been typed into a file meant for a public repository
in the first place, regardless of sensitivity.

**Suggestion**: this is a reflex to build now, not a one-time fix: before any hackathon
submission, grep every file that will ship for anything that looks like an account
number, an ARN, or an id, on the assumption that the directory becomes public. Do not
rely on remembering not to type it.

## `TimeoutCancellationException`'s constructor is internal, which breaks the obvious unit test

**Task**: write a unit test proving `FallbackIntentResolver` falls back to the offline
matcher when Bedrock's own timeout fires, not just when it throws a generic exception.

**Steps**: wrote `throw TimeoutCancellationException("timed out")` directly inside a
fake resolver, the obvious way to simulate the failure.

**Expected**: it compiles; `TimeoutCancellationException` is a public class in
`kotlinx.coroutines`.

**Actual**: `Cannot access 'constructor(message: String): TimeoutCancellationException':
it is internal in 'kotlinx.coroutines.TimeoutCancellationException'.` The class is
public API, its constructor is not -- by design, since the library wants callers to get
one only from a real `withTimeout` call, never to fabricate one.

**Severity**: low. A five-minute fix once the actual cause was clear, but the error
message alone (a plain "cannot access" with no explanation of *why* a public class has
an inaccessible constructor) cost more than five minutes to place.

**Workaround**: the test now triggers a *real* `TimeoutCancellationException` by
calling `withTimeout(1) { delay(100); ... }` inside the fake resolver, which is
arguably the more honest test anyway -- it exercises the exact exception type the real
`BedrockIntentResolver` would throw, rather than a hand-built stand-in.

**Suggestion**: when a coroutines-library exception won't construct directly, look for
the real code path that throws it before reaching for a different exception type as a
substitute; the substitute is usually less honest, not just less convenient.

## Sharing an emulator with sibling agents got considerably worse under sustained load

**Task**: verify the caregiver catalogue and focus fixes end to end, by D-pad, on
`FireTV_API28`.

**Steps**: the single-collision friction entry above (earlier in this file) undersold
what this became once several sibling agents' Fire TV apps were all testing
concurrently. Across roughly twenty `am start` / `input keyevent` / `screencap`
sequences in one stretch, a different sibling app's own main screen came to the
foreground, uninvited, more often than not -- sometimes between two commands issued
under half a second apart.

**Severity**: medium-high for testing throughput specifically: several planned
verification screenshots (a clean shot of the home grid immediately after a catalogue
edit, in particular) were never obtained cleanly despite repeated attempts, because
the foreground app kept changing out from under the test between the edit and the
screenshot. No data was corrupted and no app crashed; this cost time, not
correctness.

**Workaround**: `am start -n com.simplemode.firetv/.MainActivity` immediately before
each verification step, always re-checking `dumpsys activity activities | grep
mResumedActivity` before trusting a screenshot, and accepting indirect verification
(unit tests plus a partial screenshot sequence establishing the mechanism, e.g. a
confirmed PIN screen plus a confirmed reordered list, even when the single shot
proving "and now the home grid reflects it" kept getting stolen) where a clean direct
shot could not be obtained inside a reasonable number of retries.

**Suggestion**: repeating the earlier entry's point because the problem got worse, not
better: if several agents may be driving Android emulators in the same hackathon round,
each needs its own uniquely-named, exclusively-owned AVD from the start, and ideally a
convention (a lock file, a claimed-serial list) so one agent's `am start` cannot steal
another's foreground mid-test. `adb devices` and `dumpsys activity activities` are the
right tools to detect it after the fact; nothing here detects it in advance.

## The first two-thirds of this build shipped with zero focus modifiers

**Task**: none -- this is a finding from the shared Fire TV craft reference's review of the
code as it stood before this session's focus pass, worth recording because it explains
a real gap in how "it works" was judged earlier in the build.

**What happened**: every screen up to that point relied on Compose's default focus
behaviour, reached through `clickable()`'s implicit `focusable()`. That default does
move focus and does render *some* indication, which is exactly why earlier screenshots
in this same session (see the phone-emulator entries above) looked like working D-pad
navigation and were reported as such. What was missing was invisible in a screenshot
taken after a press had already landed focus somewhere reasonable: no screen ever
requested *initial* focus, so a cold launch rendered with nothing focused at all until
the first press, which is the single most-cited focus failure in the reference
document, and would have read as a dead remote to anyone testing without already
knowing where to press first.

**Severity**: high as a judging risk, low as an engineering fix: the actual repair
(`TileFocusState.kt`'s three channels, `initialFocus` on the first tile of every
screen, `focusGroup()` on each row) took under two hours once the gap was named,
because the animation and interaction-source plumbing Compose needs was already
mostly in place from the existing focus-ring code -- it was pointed at the wrong
thing (reacting to focus rather than requesting it) rather than absent.

**Suggestion**: a screenshot after a press proves focus can move. It does not prove a
screen is usable from a cold, untouched remote. Test every screen's very first frame,
with no input yet sent, before trusting that a TV app's focus system works.

## Fire TV has no system text-size setting, so the signal this app most needs does not exist

**Task**: size the whole interface to the viewer's own declared needs without a setup
wizard. The product is for someone who cannot work a television, so asking her to pick a
text size in a settings screen is asking her to do the thing the app exists to avoid. The
design depends on Fire OS already knowing something about her.

**Steps**: read
`developer.amazon.com/docs/fire-tv/implement-reading-and-subscribing-to-adcc.html`, the
page Fire TV publishes on reading a viewer's accessibility settings, and looked for a
text-size or display-scale signal to subscribe to.

**Expected**: the TV equivalent of Android's `fontScale`, or of iOS's Dynamic Type. A
number, or at least a flag, saying this viewer wants larger text.

**Actual**: there is none. Fire TV has no system text-size setting at all, so there is
nothing to read. The page documents two signals and neither is the one we wanted:
closed captions (`CaptioningManager.isEnabled()`, with a `CaptioningChangeListener`) and
audio description (the secure setting `accessibility_audio_descriptions_enabled`). The
second comes with its own caveat, in Amazon's own words: it "only applies to Fire OS 8
devices and earlier." So on current hardware, the newer of the two documented signals is
the one that may simply not answer.

**Severity**: high as a design constraint, and it is the constraint that shaped the app.
Every app on this platform has to ship its own text-size control, which is exactly the
setup step this product could not have.

**Workaround, and what it cost.** `accessibility/AccessibilityNeedsObserver.kt` treats
captions-on as a legibility signal rather than a captions signal: somebody who has turned
captions on has told the television they want words to be easier to read, which is not the
same statement as "I want larger buttons," but it is the closest one Fire OS offers. The
app reads it, subscribes to changes, and scales type and tile height together from it
through `UiScale`. The audio-description setting is read and watched too, because it costs
nothing to ask, but it is nullable and nothing downstream depends on it answering. The
observer also catches `SecurityException` around the `Settings.Secure` URI registration,
because some images restrict observing settings outside the owning UID, and captions alone
still drive the adaptation when that happens.

The cost is precision. We are inferring a preference from a different preference. A viewer
who wants bigger text but has never turned captions on gets the default size, and there is
no way for this app to know she exists. The 1.25x boost that captions trigger also caused
two real layout bugs in this build, a clipped tile label and row-two labels pushed below
the fold, both recorded above. So the workaround is not free at the layout level either.

**Suggestion**: publish a text-size or display-scale setting on Fire TV and expose it the
way the ADCC page already exposes captions. Until then, say plainly on that page that no
text-size signal exists, because a developer reading a page titled "reading and
subscribing to accessibility settings" reasonably assumes the list is the whole list.
Second, mark the Fire-OS-8-and-earlier scope next to the setting itself rather than in a
note further down, since a signal that does not answer on current hardware is worth
knowing about before you write a `ContentObserver` for it. Related, and covered at length
elsewhere in this batch's own friction logs: the screen reader on this platform is
VoiceView, not TalkBack, it rides the focus system, and neither of the two can be
exercised on the Android TV emulator that stands in for a device.

## Two Amazon pages give Fire OS 16 two different Android versions

**Task**: choose `minSdk` and `targetSdk`, and know which framework APIs exist on the
device.

**Steps**: read `developer.amazon.com/apps-and-games/fire-tv`, which is the page the
track's own resources link to first. Fetched 22 September, it says:

> Our operating system, **Fire OS 16, is based on Android 9 (Pie) and API level 28**,
> making it compatible with existing Android apps.

So, API 28. We set `minSdk` and `targetSdk` to 28 and reasoned outward from there,
including a long piece of work on whether a third-party app can inspect another app's
video, which turns partly on `AccessibilityService.takeScreenshot` arriving after API 28.

**Actual**: `developer.amazon.com/docs/device-specs/identify-fire-tv-devices.html`,
fetched the same day, says something else. It opens by enumerating the versions:
"Fire OS 16: Based on Android 16... Fire OS 8: Based on Android 11 (API level 30), and
Android 10 (API Level 29)... Fire OS 5: Based on Android 5.1 (Lollipop, API level 22)."
Its device table gives every Fire OS 8 stick Android level 30, and pins Android level 28
to Fire OS 7 devices, the 2024 Fire TV Stick HD among them.

Both pages are Amazon's. They cannot both be right, and we could not work out which one
is wrong.

**Severity**: low for the binary, medium for everything we decided with it. `minSdk 28`
is a floor, so the APK installs and runs on Fire OS 7 and Fire OS 8 either way. The cost
landed on the reasoning. We treated API 28 as the ceiling of what Fire TV offers when the
shipping fleet is mostly API 30, and we wrote "matching Fire OS 16's own API level" into
three documents. It matches Fire OS 7.

**Workaround**: none available. We kept `minSdk 28`, which is correct under either
reading, and struck the claim that it matches the current Fire OS wherever it appeared.

**Suggestion**: one of those two pages needs an edit, and the landing page is the one a
developer reads first. The version numbering is doing damage on its own, too: Fire OS
went 5, 6, 7, 8, then 16, and "Fire OS 16 is Android 9" reads as plausible precisely
because the numbers have stopped tracking anything. Print the Android API level next to
the Fire OS version every time the Fire OS version appears.

## Amazon's Fire TV design guidance is dated October 2020, and its numbers are for a different problem

**Task**: set type sizes, focus treatment and edge padding for a ten-foot interface whose
entire audience is people who cannot work a television.

**Steps**: read Amazon's *Design and User Experience Guidelines* for Fire TV, then
Google's Android TV design guidance, then Apple's tvOS HIG, and compare.

**Expected**: a minimum body text size, a contrast ratio, and a focus specification.

**Actual**: Amazon's page is stamped 29 October 2020. It recommends Helvetica Neue as the
system font, a 14sp body minimum, and desaturated cool colours over warm ones. We ignored
all three. 14sp is a floor for chrome, not for body text read across a room: Fire TV is
always density 320, so 14sp is 28 physical pixels, which subtends roughly 13 arcminutes
of cap height on a 50-inch set at three metres, against the 16 to 22 ISO 9241-303 asks
for sustained reading. Our own derivation lands on 24sp for body text, 28sp for an older
viewer, hard floor 20sp.

Google is no help either. It publishes no minimum sp and no contrast ratio for TV
anywhere, and `androidx.tv:tv-material3` ships the unmodified phone type scale, bottoming
out at 11sp. The prose says go larger and the library hands you small.

**Severity**: high as a design risk. The first build of this app used 16sp, 14sp and 13sp
as its three most common text sizes, which are phone sizes, and it read as a wireframe.

**Workaround**: derive the numbers instead of inheriting them, and write the arithmetic
down so nobody has to redo it. That work is in the shared Fire TV craft reference.

**Suggestion**: two numbers and a date. Publish a minimum body text size for TV with the
viewing distance it assumes. Publish a contrast ratio, any figure at all, because none of
the three vendors gives one and every TV app is guessing. And note that the useful parts
of that 2020 page, the 5% overscan rule and the argument for low information density, are
still exactly right, and they are sitting beside a font recommendation old enough to make
a reader distrust the whole document.

## The route we were told to film on runs a different operating system

**Task**: work out how to record a demo video that satisfies the rule, which says the
video has to show the project running on an actual Fire TV device or the Fire TV/Vega
simulator.

**Steps**: the Vega Virtual Device looked like the answer. It is free, it runs on Ubuntu,
it needs no Amazon sign-in, and it is literally the artefact the rule names. Before
installing 20GB of SDK I checked whether Simple Mode would run on it. That check is this
entry.

Three pages, all fetched 22 September with
`curl -sL --compressed -A "Mozilla/5.0 (X11; Linux x86_64) Chrome/140.0"`:

- `developer.amazon.com/docs/device-specs/identify-fire-tv-devices.html` lists every Fire
  TV device with a build model, an Android level and an OS version. The two Vega sticks,
  Fire TV Stick HD (2026, AFTCL001) and Fire TV Stick 4K Select (2025, AFTCA002), are the
  only rows in the whole table whose Android level reads `N/A`.
- `developer.amazon.com/docs/vega/0.24/run-apps.html` gives the install command:
  `vega run-app <vpkg-path> <app-id> -d VirtualDevice`. A `.vpkg`, addressed by app id,
  loaded by the Vega CLI. There is no `adb` anywhere in the Vega documentation, and the
  Hello World page reports success as "kepler ktbuild exited with code 0", not Gradle.
- `developer.amazon.com/docs/vega/0.24/vega-rn-arch.html` says React Native for Vega is
  "an out-of-tree fork of React Native framework for Vega devices". The tell is two
  sentences later: RNV "includes specific APIs exclusive to a specific RN platform, such
  as **BackHandler (Android)**". Android is a foreign platform it borrows an API name
  from.

I grepped every Vega page I pulled (overview, get started, build, run, hello world,
architecture, app manifest, troubleshooting, developer mode) for `android`, `apk`,
`kotlin` and `java`. That BackHandler note is the only hit in the lot.

**Expected**: a simulator I could sideload an APK into, the way the Android TV emulator
takes one.

**Actual**: Simple Mode is a Kotlin/Compose APK. Vega runs `.vpkg` bundles built by
`kepler ktbuild` from React Native sources. There is no porting tool, no compatibility
layer, no APK loader. Getting this app onto Vega means rewriting it in React Native
against RNV's API subset and learning a build toolchain nobody here has opened.

The emulator route is shut on the other side too.
`developer.amazon.com/docs/fire-tv/differences-from-android-tv-development.html` has a
section headed **Emulators**, and it reads in full: "When testing out your Amazon Fire TV
app code, you use an **actual Fire TV device (either the set-top box or stick) instead of
a virtual emulator**." The legacy Amazon add-on feed that used to serve Fire system
images to the Android SDK Manager, `s3.amazonaws.com/android-sdk-manager/redist/addon.xml`,
403s over both HTTP and HTTPS.

**Severity**: the highest in this log, and it is not a time cost. Everything else here
cost minutes. This one decides whether we enter the track at all. After the check, the
only compliant way to film a Kotlin Fire TV app is a physical Fire TV Stick on Fire OS 7
or 8.

**Workaround**: buy one, which turned out to be its own problem. amazon.com will not ship
any Amazon-branded device to Ghana, where this was built. The Fire TV Stick 4K Plus page,
ASIN B0F7Z4QZTT, returns "This item cannot be shipped to your selected delivery location"
with no price and no buy box, and so does every Amazon-branded item in the carousel that
page offers as consolation, down to Amazon's own ethernet adapter. Third-party
accessories on the same marketplace have no such message, so this is an export
restriction on Amazon hardware rather than a country-wide block. The route that would
have worked is a used Fire TV Stick 4K Max 2nd Gen, build model AFTKRT, findable for cash
in Accra at GH₵999, about $86, with `Settings > My Fire TV > About` checked before the
money moves, because the retail box does not say Vega and the two newest, cheapest sticks
on the shelf are the two that cannot run our software.

**Update**: that route is now closed by decision, not just by inconvenience. The
decision has been made not to buy a Fire TV Stick, which means this is not a
workaround with a cost attached — it is the final, real state of the entry. This app, and two sibling Fire TV apps in the same batch, go into the track with no compliant way to
film them, and every screenshot and screen recording any of the three can submit was
taken on an emulator that does not satisfy the rule's own wording.

**Suggestion**: one sentence on the onboarding page, before anyone installs the SDK.
"The Vega Virtual Device runs Vega OS apps built with React Native. It cannot install an
Android APK. If your app is an APK, you need Fire OS hardware." Amazon nowhere prints
that sentence; the case above is three documentation facts pointing the same way, which
is as close to conclusive as absence of a denial gets, and I would rather have been told
than have derived it.

The wider point, and the reason this is not a footnote. Take the two rules together: no
emulator, use a device, and the free simulator runs a different operating system. The
practical effect is that the Fire TV track is open to developers who already own Fire TV
hardware and closed to the ones who do not, and a developer outside the US cannot fix
that by buying their way in, because Amazon will not sell it to them. I do not think that
was anybody's intention. It is what the current arrangement does.

## Nothing on Fire TV will tell a third-party app what is in front, and it did not need to

**Task**: make the recovery overlay conditional. The floating "back to Simple Mode"
button was being added when the foreground service started and removed only when it
stopped, so once a caregiver turned Always Home on it was on screen permanently. Our
own captures caught it: in an early screenshot of this app's own home screen, the green pill sits over the Films tile and hides the label, on Simple Mode's own home
screen. On a real television it would have sat over the film.

**Expected**: reach for the ordinary Android answer, ask the system which package is
foreground, hide the button when that package is ours.

**Actual**: there is no such answer available to a third party, and both of the usual
candidates fail for different reasons.

`ActivityManager.getRunningTasks` has been restricted since Lollipop; a normal app now
gets back its own tasks and nothing else, so it can never see the app it is drawn over.

`UsageStatsManager` does work, but `android.permission.PACKAGE_USAGE_STATS` is a
special-access permission: it is not granted by a runtime prompt, the caregiver has to
find a usage-access screen in Settings and turn the app on there. On a television that
is a worse problem than on a phone, because the caregiver is doing it with a remote,
from a sofa, on behalf of someone else, and because there is no guarantee Fire OS
surfaces that screen at all. Amazon documents neither the presence nor the absence of
it. We were not willing to make the app's central promise depend on a permission we
could not confirm a Fire TV would even offer.

**What the question actually was.** The overlay does not need to know which app is in
front. It needs to know whether *Simple Mode* is. Those are different questions, and the
second one an app can always answer about itself, for free, from
`Application.ActivityLifecycleCallbacks`. `recovery/ForegroundWatcher.kt` counts resumed
activities from process start; `recovery/OverlayVisibility.kt` is the one line that
follows from it. No permission, no system API, no Fire-OS-specific behaviour to verify
on hardware we do not have. The fix was smaller than the investigation.

One detail worth passing on, because getting it wrong produces a bug that only shows up
as a flicker: it has to be a count, not a boolean. Android overlaps the handoff between
two activities, running the incoming one's `onResume` before the outgoing one's
`onPause`. A boolean cleared in `onPause` reports "not in front" for a frame on every
internal screen change, and the overlay blinks up over the app's own UI.
`ForegroundTally` counts, and the count never dips to zero unless the app really left.

**The limitation, stated plainly.** A `TYPE_APPLICATION_OVERLAY` window carrying
`FLAG_NOT_FOCUSABLE` is on top of the screen but never holds input focus, so it receives
no key events at all. `dumpsys window` on our own emulator shows exactly that: with the
overlay attached as the topmost window,

```
  Window #1 Window{7e0a8d u0 com.simplemode.firetv}:
  ...
  mCurrentFocus=Window{b19aff0 u0 com.android.tv.settings/com.android.tv.settings.MainSettings}
```

the focus belongs to the app underneath. Two things follow, and we would rather write
them down than let a judge find them.

First, the button cannot restore itself on a key press. We wanted "shrink after a few
seconds, come back when the viewer touches the remote", and the second half is not
available: the overlay cannot see the remote. Dropping `FLAG_NOT_FOCUSABLE` would let it
see the remote and would also take the remote away from the app behind it, turning a
rescue into a trap. So the button holds full size for six seconds, then shrinks to 62%
at 45% opacity, and returns to full size on the one event we can genuinely observe --
the viewer leaving Simple Mode again, which is the moment it matters.

Second, and this is the bigger one: on a D-pad-only Fire TV remote, a non-focusable
window cannot be reached *at all*. Every route back that this build can stand behind --
the overlay, the `HOME` intent-filter, the foreground-service notification's content
intent -- depends on something Amazon has not documented for third parties, and the
overlay's own input model is the one we can prove is limited. We are not claiming the
button is remote-operable on hardware we have never held.

**Severity**: medium as a time cost, maybe ninety minutes including the research that
ruled out the two obvious mechanisms. High as a finding, because the instinct here is to
reach for `UsageStatsManager` and ship a special-access permission prompt at a caregiver
who will not know what it is.

**Suggestion**: Fire TV's developer documentation says nothing about `SYSTEM_ALERT_WINDOW`
overlays, about whether the usage-access Settings screen exists on Fire OS, or about how
a non-focusable overlay is meant to be operated from a remote that has no pointer. An
app whose whole purpose is getting a confused viewer unstuck is not an exotic case on a
television, and right now the platform's position on it has to be inferred.
