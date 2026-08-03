package com.mail2dev.upperdot.telecom

import android.content.Intent
import android.telecom.Call
import android.telecom.InCallService
import android.util.Log

class UpperDotInCallService : InCallService() {
    
    companion object {
        var instance: UpperDotInCallService? = null
        var activeCall: Call? = null
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        Log.d("UpperDotInCallService", "Call added: ${call.details.handle}")
        activeCall = call
        
        // Start the In-Call Activity
        val intent = Intent(this, InCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or 
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
        }
        startActivity(intent)
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        Log.d("UpperDotInCallService", "Call removed")
        if (activeCall == call) {
            activeCall = null
        }
    }
}
