package com.mail2dev.upperdot.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.telecom.TelecomManager
import android.telephony.PhoneNumberUtils
import android.telephony.TelephonyManager
import android.util.Log
import java.util.Locale

object TelephonyUtils {

    /**
     * Places an outgoing call using the system TelecomManager.
     * Automatically formats the number to E.164 standard based on the device's SIM country or system locale.
     */
    fun placeOutgoingCall(context: Context, rawNumber: String) {
        val telecomManager = context.getSystemService(Context.TELECOM_SERVICE) as TelecomManager
        val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

        // 1. Detect device's active country ISO code (e.g., "MY", "SG", "US")
        val countryIso = try {
            telephonyManager.simCountryIso.ifEmpty {
                Locale.getDefault().country
            }.uppercase()
        } catch (e: Exception) {
            Locale.getDefault().country.uppercase()
        }

        // 2. Format to E.164 standard (+CountryCode + Subscriber Number)
        var formattedNumber: String? = null

        if (rawNumber.startsWith("+")) {
            // Number already has international format, just clean up spaces/dashes
            formattedNumber = rawNumber.replace("[^0-9+]".toRegex(), "")
        } else {
            // Let Android intelligently format local numbers based on current SIM country
            formattedNumber = PhoneNumberUtils.formatNumberToE164(rawNumber, countryIso)
        }

        // Fallback if Android couldn't format it automatically
        if (formattedNumber.isNullOrEmpty()) {
            val cleaned = rawNumber.replace("[^0-9+]".toRegex(), "")
            formattedNumber = if (cleaned.startsWith("+")) cleaned else "+$cleaned"
        }

        Log.d("CallDebug", "Placing call to URI: tel:$formattedNumber using CountryIso: $countryIso")
        val uri = Uri.parse("tel:$formattedNumber")

        if (context.checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            try {
                telecomManager.placeCall(uri, null)
            } catch (e: Exception) {
                Log.e("CallDebug", "Error placing call: ${e.message}")
            }
        }
    }
}
