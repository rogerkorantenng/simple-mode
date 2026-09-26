# Simple Mode

A Fire TV app for someone who cannot work a television: six to eight huge,
voice-friendly targets, a screen that sizes itself to the viewer's own
accessibility settings without a setup wizard, a "Tell Me" screen for saying
what you want in your own words, a PIN-gated way for the family member who
set it up to change what she sees, and one button that gets back to it from
almost anywhere the TV has wandered.

Full product spec, the design rationale, and an honest account of what this
does not and cannot do: [`SPEC.md`](./SPEC.md).

## What is real here, and what is a stand-in

- **The accessibility adaptation is real.** `CaptioningManager.isEnabled()` /
  `CaptioningChangeListener`, and the `accessibility_audio_descriptions_enabled`
  secure setting / `ContentObserver`, are the actual Fire OS APIs, called
  directly, no mock layer between the app and the platform.
- **The recovery overlay is real.** A genuine `TYPE_APPLICATION_OVERLAY`
  window drawn by a foreground service, using the standard
  `SYSTEM_ALERT_WINDOW` permission flow. It appears only when the viewer is
  somewhere other than Simple Mode, which the app reads from its own activity
  lifecycle rather than from any permission-gated system API.
- **Tell Me calls the real Bedrock Converse API**, through the local proxy in
  `server/bedrock_proxy.py` (see below). It is not a canned response.
- **"Live TV" and "Norah's Shows" are sample content**, hosted inside the app
  itself, clearly marked as such in `ui/SampleContent.kt`. There is no real
  channel lineup or watch history behind them; that would need a backend
  this build does not have. What Tell Me has been asked and what has been
  picked from these two screens *are* real, persisted records, readable on
  the "For the Family" screen.
- **Netflix, Prime Video and YouTube tiles launch by real package name**
  through `PackageManager.getLaunchIntentForPackage`. On a bare emulator
  with none of them installed, tapping one shows the same "not set up on
  this TV yet" screen a caregiver's actual, partially-configured TV would
  show, instead of crashing.
- **The caregiver catalogue editor is real.** Reordering, removing and
  adding a tile writes to the same store `HomeScreen` reads from; there is
  no separate "preview" state that silently fails to apply.

## Requirements

- JDK 21 (the build pins `org.gradle.java.home` to
  `/usr/lib/jvm/java-21-openjdk-amd64`; adjust `gradle.properties` if that
  path differs on your machine).
- Android SDK with `platform;android-34` and `build-tools;34.x` or newer
  installed, `ANDROID_HOME`/`local.properties` pointing at it.
- Python 3 and the AWS CLI, configured with Bedrock access in `us-east-1`,
  only if you want Tell Me's real-model path rather than its offline
  fallback (see below). Nothing else in the app needs either.

## Build and test

```bash
echo "sdk.dir=$ANDROID_HOME" > local.properties   # or your SDK path; gitignored, not in the clone
./gradlew :app:testDebugUnitTest :app:assembleDebug
```

This runs 108 unit tests (accessibility adaptation, tile navigation, the
caregiver catalogue's pure list operations, the intent resolver's offline
keyword matching and its Bedrock-then-fallback wiring, and the PIN's
hashing) and produces `app/build/outputs/apk/debug/app-debug.apk`.

## Run it

An Android TV emulator is strongly preferred over a phone-shaped one: this
app's whole design is for a ten-foot canvas, and a phone emulator will not
show that honestly. To create one:

```bash
sdkmanager --install "system-images;android-28;android-tv;x86"
avdmanager create avd -n FireTV_API28 -k "system-images;android-28;android-tv;x86" -d tv_1080p
~/Android/Sdk/emulator/emulator -avd FireTV_API28 -no-audio -no-boot-anim -gpu swiftshader_indirect &
adb wait-for-device
```

Then, on any running emulator or connected device, API 28+:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.simplemode.firetv/.MainActivity
```

Drive it with the D-pad, not a click -- that is what actually exercises the
focus system a TV remote depends on:

```
adb shell input keyevent 19   # up
adb shell input keyevent 20   # down
adb shell input keyevent 21   # left
adb shell input keyevent 22   # right
adb shell input keyevent 23   # centre / select
adb shell input keyevent 4    # back
```

To see the adaptation live, without touching Simple Mode at all:

```bash
# Turn captions on; the home screen's type, spacing and focus border grow.
adb shell settings put secure accessibility_captioning_enabled 1
# Turn them off again.
adb shell settings put secure accessibility_captioning_enabled 0
```

To see the recovery overlay: select "Always Home" on the home screen
(grants the draw-over-other-apps permission the first time), then open
anything else. The green button appears in the bottom-left corner once you
leave Simple Mode, and selecting it brings Simple Mode back. It is not on the
home screen, deliberately — a button whose job is to return you to Simple Mode
has no business covering a tile while you are already there. `adb shell dumpsys
window windows` is the quick check: a bare `Window{… u0 com.simplemode.firetv}`
is the overlay, and it is absent while the app's own activity has focus.

To see the caregiver catalogue: select "Family setup" on the home screen,
then "Edit what Norah sees". First run asks you to set a four-digit PIN;
after that it asks for it. Inside, move, remove or add a tile and back out
to Home to see the grid itself change.

## Running Tell Me against the real model

Tell Me works with no setup at all -- it falls back to a offline keyword
matcher if the proxy below is not running, which is also the path this app
is designed to be used through by default; see `SPEC.md`. To see the real
Bedrock path instead:

```bash
cd server
python3 bedrock_proxy.py
```

It listens on `127.0.0.1:8798`; the Android emulator reaches host loopback
at `10.0.2.2`, which is what `BedrockIntentResolver.kt` is pointed at. This
needs AWS credentials with Bedrock access in `us-east-1` already configured
in your shell (`aws configure`, or an assumed role); the app and the proxy
never see or store a credential themselves.

## What this was tested against

Developed and driven day to day against `FireTV_API28`, a real Android TV system
image at API 28, Fire OS 16's own API level, at 1920x1080 / density 320, matching
Fire TV's reported display configuration exactly, with every interaction driven by
`adb shell input keyevent`, never a click. Built for Fire OS and tested on a real Fire
TV as well: the overlay recovery button and the HOME registration were both confirmed
working there, not just inferred from the manifest. See `SPEC.md`'s "Testing note"
and "Ten-foot craft" sections for the full account of an internal design
review and what of it was and was not addressed in the time left.

## Licence

MIT. Third-party image credits: see [`ATTRIBUTION.md`](ATTRIBUTION.md).
