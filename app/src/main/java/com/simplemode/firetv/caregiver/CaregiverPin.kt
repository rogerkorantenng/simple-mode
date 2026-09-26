package com.simplemode.firetv.caregiver

import android.content.Context
import java.security.MessageDigest

/**
 * A four-digit PIN, so the catalogue "cannot be changed by accident" (the
 * coordinator's own framing). Not a serious security boundary -- the
 * threat model is a curious viewer poking at buttons, not an attacker --
 * but the PIN is still hashed, never stored in the clear, on the general
 * principle that a stored secret should never be plainer than it needs to
 * be. [hash] is a pure function, directly unit-testable without a device.
 */
object CaregiverPin {
    private const val PREFS_NAME = "caregiver"
    private const val KEY_PIN_HASH = "pin_hash"

    fun isSet(context: Context): Boolean = prefs(context).contains(KEY_PIN_HASH)

    fun set(context: Context, pin: String) {
        prefs(context).edit().putString(KEY_PIN_HASH, hash(pin)).apply()
    }

    fun verify(context: Context, pin: String): Boolean =
        prefs(context).getString(KEY_PIN_HASH, null) == hash(pin)

    /** Pure: no Context, no Android framework, so it is directly testable. */
    fun hash(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
