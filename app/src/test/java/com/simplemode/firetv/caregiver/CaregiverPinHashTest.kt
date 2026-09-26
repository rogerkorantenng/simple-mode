package com.simplemode.firetv.caregiver

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class CaregiverPinHashTest {

    @Test
    fun `the same PIN always hashes the same way`() {
        assertEquals(CaregiverPin.hash("1234"), CaregiverPin.hash("1234"))
    }

    @Test
    fun `different PINs hash differently`() {
        assertNotEquals(CaregiverPin.hash("1234"), CaregiverPin.hash("4321"))
    }

    @Test
    fun `the PIN is never stored as itself`() {
        assertNotEquals("1234", CaregiverPin.hash("1234"))
    }
}
