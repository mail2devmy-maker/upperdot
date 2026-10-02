package com.mail2dev.upperdot.telecom

import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log
import com.mail2dev.upperdot.UpperDotApp
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first

class UpperDotCallScreeningService : CallScreeningService() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onScreenCall(callDetails: Call.Details) {
        val handle = callDetails.handle?.schemeSpecificPart ?: return
        
        serviceScope.launch {
            val app = applicationContext as UpperDotApp
            val prefs = app.preferenceRepository.preferences.first()
            val contact = app.contactRepository.findContactByPhone(handle)
            
            var shouldBlock = false
            
            if (prefs.blockUnknownNumbers && contact == null) {
                Log.d("CallScreening", "Blocking unknown caller: $handle")
                shouldBlock = true
            }
            
            if (!shouldBlock && prefs.strictPrivacyMode) {
                // In strict privacy mode, only allow whitelisted contacts
                if (contact == null || !contact.isWhitelisted) {
                    Log.d("CallScreening", "Blocking non-whitelisted caller: $handle")
                    shouldBlock = true
                }
            }
            
            val response = if (shouldBlock) {
                CallResponse.Builder()
                    .setDisallowCall(true)
                    .setRejectCall(true)
                    .setSkipCallLog(false)
                    .setSkipNotification(true)
                    .build()
            } else {
                CallResponse.Builder()
                    .setDisallowCall(false)
                    .setRejectCall(false)
                    .setSkipCallLog(false)
                    .setSkipNotification(false)
                    .build()
            }
            
            respondToCall(callDetails, response)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }
}
