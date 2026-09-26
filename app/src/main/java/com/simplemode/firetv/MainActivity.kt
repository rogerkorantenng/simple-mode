package com.simplemode.firetv

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import com.simplemode.firetv.accessibility.AccessibilityNeedsObserver
import com.simplemode.firetv.accessibility.UiAdaptation
import com.simplemode.firetv.intent.BedrockIntentResolver
import com.simplemode.firetv.intent.FallbackIntentResolver
import com.simplemode.firetv.intent.KeywordIntentResolver
import com.simplemode.firetv.launch.AndroidPackageResolver
import com.simplemode.firetv.recovery.OverlayController
import com.simplemode.firetv.recovery.RecoveryOverlayService
import com.simplemode.firetv.ui.AppRoot
import com.simplemode.firetv.ui.FullScreenSurface
import com.simplemode.firetv.ui.theme.SimpleModeTheme

/**
 * The whole app lives in this one Activity. Fire TV documents no way for a
 * third party to replace the system launcher or guarantee it appears after
 * boot (internal research into Fire TV's own documentation, section 2), so "always
 * gets you back here" is delivered by [RecoveryOverlayService] and owned by
 * [OverlayController], not by this Activity claiming powers Fire OS never
 * granted it. This class is deliberately just lifecycle wiring: the
 * accessibility reading lives in [AccessibilityNeedsObserver], the overlay
 * state machine lives in [OverlayController], and the intent-launching
 * lives in `AppIntents.kt`.
 */
class MainActivity : ComponentActivity() {

    private lateinit var accessibilityObserver: AccessibilityNeedsObserver
    private lateinit var overlayController: OverlayController
    private val goHomeSignalState = mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        accessibilityObserver = AccessibilityNeedsObserver(applicationContext)
        overlayController = OverlayController(this)
        val resolver = AndroidPackageResolver(applicationContext)
        val intentResolver = FallbackIntentResolver(BedrockIntentResolver(), KeywordIntentResolver())

        setContent {
            val needs by accessibilityObserver.needs.collectAsState()
            val overlayEnabled by overlayController.enabled
            val goHomeSignal by goHomeSignalState

            SimpleModeTheme(uiScale = UiAdaptation.scaleFor(needs)) {
                FullScreenSurface {
                    AppRoot(
                        needs = needs,
                        overlayEnabled = overlayEnabled,
                        resolver = resolver,
                        intentResolver = intentResolver,
                        onToggleOverlay = { overlayController.toggle() },
                        onLaunchExternal = { packageName -> launchExternalApp(packageName) },
                        onOpenSystemSetting = { action -> openSystemSetting(action) },
                        goHomeSignal = goHomeSignal,
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        overlayController.refresh()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra(EXTRA_RECOVERED, false)) {
            goHomeSignalState.value += 1
        }
    }

    override fun onStart() {
        super.onStart()
        accessibilityObserver.start()
    }

    override fun onStop() {
        accessibilityObserver.stop()
        super.onStop()
    }

    companion object {
        const val EXTRA_RECOVERED = "recovered"
    }
}
