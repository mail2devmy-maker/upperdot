package com.mail2dev.upperdot.util

import com.mail2dev.upperdot.R

object ContactUtils {
    /**
     * Smart Number Matcher: Compares numbers by stripping symbols (+, -, spaces) and country codes.
     * Specifically handles Malaysian +60 mapping to leading 0.
     */
    fun smartSanitize(number: String): String {
        if (isUssdCode(number)) return ""
        
        // 1. Normalize (strips non-digits, preserves +)
        // Manual implementation to avoid Android dependency in unit tests
        val normalized = number.filter { it.isDigit() || it == '+' }
        
        // 2. Remove leading + and 0
        var clean = normalized.trimStart('+', '0')
        
        // 3. Strip Malaysian country code (60) if present to match local 0... format
        if (clean.startsWith("60")) {
            clean = clean.substring(2)
        }
        
        return clean
    }

    fun isUssdCode(number: String): Boolean {
        return number.startsWith("*") || number.endsWith("#")
    }

    fun isSamePhoneNumber(num1: String, num2: String): Boolean {
        val s1 = smartSanitize(num1)
        val s2 = smartSanitize(num2)
        return s1.isNotEmpty() && s2.isNotEmpty() && s1 == s2
    }

    /**
     * Formats a phone number for display, specifically handling Malaysian standards.
     * Priority: +60... format for clarity.
     */
    fun formatForDisplay(number: String): String {
        val digits = number.replace(Regex("[^0-9]"), "")
        if (digits.isEmpty()) return number

        // If it starts with 60, just add the +
        if (digits.startsWith("60") && digits.length >= 10) {
            return "+$digits"
        }
        
        // If it starts with 0 (local Malaysian), convert to +60
        if (digits.startsWith("0") && digits.length >= 10) {
            return "+60${digits.substring(1)}"
        }

        // If it's a raw Malaysian mobile number without 0 or 60 (e.g., 163179202)
        if (digits.startsWith("1") && digits.length >= 9 && digits.length <= 10) {
            return "+60$digits"
        }

        // Fallback for other formats or international
        return if (number.startsWith("+")) number else "+$digits"
    }

    fun getSocialPlatformDrawable(platform: String): Int {
        return when (platform.uppercase()) {
            "WHATSAPP" -> R.drawable.ic_whatsapp
            "FACEBOOK" -> R.drawable.ic_facebook
            "INSTAGRAM" -> R.drawable.ic_instagram
            "X", "TWITTER" -> R.drawable.ic_twitter
            "TIKTOK" -> R.drawable.ic_tiktok
            "YOUTUBE" -> R.drawable.ic_youtube
            "SHOPEE" -> R.drawable.ic_shopee
            "LAZADA" -> R.drawable.ic_lazada
            "TELEGRAM" -> R.drawable.ic_telegram
            else -> R.drawable.ic_web
        }
    }
}
