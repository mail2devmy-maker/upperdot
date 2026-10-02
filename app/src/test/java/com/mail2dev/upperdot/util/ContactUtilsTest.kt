package com.mail2dev.upperdot.util

import org.junit.Test
import org.junit.Assert.*

class ContactUtilsTest {

    @Test
    fun smartSanitize_malaysianNumbers() {
        // All these should match to "123456789"
        val expected = "123456789"
        assertEquals(expected, ContactUtils.smartSanitize("0123456789"))
        assertEquals(expected, ContactUtils.smartSanitize("+60123456789"))
        assertEquals(expected, ContactUtils.smartSanitize("60123456789"))
        assertEquals(expected, ContactUtils.smartSanitize("012-345 6789"))
        assertEquals(expected, ContactUtils.smartSanitize("+60 12-345 6789"))
    }

    @Test
    fun smartSanitize_internationalNumbers() {
        // US Number: +1 555 123 4567 -> "15551234567"
        assertEquals("15551234567", ContactUtils.smartSanitize("+1-555-123-4567"))
        
        // Singapore Number: +65 9123 4567 -> "6591234567"
        assertEquals("6591234567", ContactUtils.smartSanitize("+65 9123 4567"))
    }

    @Test
    fun isSamePhoneNumber_matchesCorrectly() {
        assertTrue(ContactUtils.isSamePhoneNumber("0123456789", "+60123456789"))
        assertTrue(ContactUtils.isSamePhoneNumber("012-3456789", "60123456789"))
        assertFalse(ContactUtils.isSamePhoneNumber("0123456789", "0123456788"))
    }

    @Test
    fun formatForDisplay_malaysianStandards() {
        assertEquals("+60123456789", ContactUtils.formatForDisplay("0123456789"))
        assertEquals("+60123456789", ContactUtils.formatForDisplay("+60123456789"))
        assertEquals("+60123456789", ContactUtils.formatForDisplay("60123456789"))
    }
}
