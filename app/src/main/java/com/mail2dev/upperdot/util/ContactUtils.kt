package com.mail2dev.upperdot.util

object ContactUtils {
    /**
     * Smart Number Matcher: Compares numbers by stripping symbols (+, -, spaces) and country codes.
     */
    fun smartSanitize(number: String): String {
        val digits = number.replace(Regex("[^0-9]"), "")
        // Strip common country codes and leading zero
        // Malaysia: 60... or 0...
        // Let's remove leading 60, 6, 0.
        var s = digits
        if (s.startsWith("60")) s = s.substring(2)
        else if (s.startsWith("6")) s = s.substring(1)
        else if (s.startsWith("0")) s = s.substring(1)
        return s
    }

    fun isSamePhoneNumber(num1: String, num2: String): Boolean {
        val s1 = smartSanitize(num1)
        val s2 = smartSanitize(num2)
        return s1.isNotEmpty() && s2.isNotEmpty() && s1 == s2
    }
}
