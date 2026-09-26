package com.simplemode.firetv.accessibility

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.view.accessibility.CaptioningManager
import androidx.core.content.getSystemService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Reads, and subscribes to changes in, the viewer's own declared
 * accessibility needs. This is the mechanic the whole app is built around:
 * no setup wizard, because Fire OS already knows.
 *
 * https://developer.amazon.com/docs/fire-tv/implement-reading-and-subscribing-to-adcc.html
 *
 * - Closed captions: `CaptioningManager.isEnabled()`, watched with a
 *   `CaptioningChangeListener` whose `onEnabledChanged` fires on change.
 * - Audio description: the secure setting
 *   `accessibility_audio_descriptions_enabled`, read with
 *   `Settings.Secure.getInt` and watched with a `ContentObserver`. Amazon's
 *   own page notes this "only applies to Fire OS 8 devices and earlier," so
 *   on Fire OS 14/16 hardware the read may come back absent. This class
 *   still reads and watches it -- it costs nothing to ask -- but the result
 *   is nullable and nothing downstream depends on it answering.
 */
class AccessibilityNeedsObserver(private val context: Context) {

    private val captioningManager: CaptioningManager? =
        context.getSystemService<CaptioningManager>()

    private val _needs = MutableStateFlow(currentNeeds())
    val needs: StateFlow<AccessibilityNeeds> = _needs

    private val captioningListener = object : CaptioningManager.CaptioningChangeListener() {
        override fun onEnabledChanged(enabled: Boolean) {
            _needs.value = _needs.value.copy(captionsEnabled = enabled)
        }
    }

    private val audioDescriptionObserver = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean) {
            _needs.value = _needs.value.copy(audioDescriptionsEnabled = readAudioDescriptionSetting())
        }
    }

    fun start() {
        captioningManager?.addCaptioningChangeListener(captioningListener)
        try {
            context.contentResolver.registerContentObserver(
                Settings.Secure.getUriFor(AUDIO_DESCRIPTION_KEY),
                false,
                audioDescriptionObserver,
            )
        } catch (_: SecurityException) {
            // Some OEM images restrict observing Settings.Secure URIs outside
            // the owning UID. Captions alone still drive the adaptation.
        }
        _needs.value = currentNeeds()
    }

    fun stop() {
        captioningManager?.removeCaptioningChangeListener(captioningListener)
        context.contentResolver.unregisterContentObserver(audioDescriptionObserver)
    }

    private fun currentNeeds(): AccessibilityNeeds = AccessibilityNeeds(
        captionsEnabled = captioningManager?.isEnabled ?: false,
        audioDescriptionsEnabled = readAudioDescriptionSetting(),
    )

    private fun readAudioDescriptionSetting(): Boolean? = try {
        Settings.Secure.getInt(context.contentResolver, AUDIO_DESCRIPTION_KEY) == 1
    } catch (_: Settings.SettingNotFoundException) {
        null
    }

    companion object {
        const val AUDIO_DESCRIPTION_KEY = "accessibility_audio_descriptions_enabled"
    }
}
