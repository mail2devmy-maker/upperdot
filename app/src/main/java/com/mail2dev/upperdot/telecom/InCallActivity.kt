package com.mail2dev.upperdot.telecom

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.telecom.Call
import android.telecom.CallAudioState
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.mail2dev.upperdot.ui.theme.UpperDotTheme
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

import android.util.Log

class InCallActivity : ComponentActivity() {

    private var callState by mutableIntStateOf(Call.STATE_NEW)
    private var isMuted by mutableStateOf(false)
    private var isSpeakerOn by mutableStateOf(false)
    private var callerName by mutableStateOf<String?>(null)
    
    private lateinit var audioManager: AudioManager

    private val callback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            callState = state
            if (state == Call.STATE_DISCONNECTED) {
                val cause = call.details?.disconnectCause
                val extras = call.details?.extras
                
                Log.d("CallDebug", "Reason: ${cause?.reason}, Code: ${cause?.code}")
                
                // Log extra telecom state details if available
                extras?.keySet()?.forEach { key ->
                    Log.d("CallDebug", "Extra -> $key : ${extras.get(key)}")
                }

                // OEM Specific Extras (Oppo, Realme, Samsung, etc.)
                val oemExtras = extras?.getBundle("android.telephony.ims.extra.OEM_EXTRAS")
                oemExtras?.keySet()?.forEach { key ->
                    Log.d("CallDebug", "OEM Extra -> $key : ${oemExtras.get(key)}")
                }
                
                cleanupAndFinish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        
        // Show over lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }

        val call = UpperDotInCallService.activeCall
        if (call == null) {
            finish()
            return
        }

        call.registerCallback(callback)
        @Suppress("DEPRECATION")
        callState = call.state

        // Resolve Caller Name
        val handle = call.details.handle?.schemeSpecificPart
        if (handle != null) {
            val app = applicationContext as com.mail2dev.upperdot.UpperDotApp
            lifecycleScope.launch {
                val contact = app.contactRepository.findContactByPhone(handle)
                callerName = contact?.fullName
            }
        }

        setContent {
            UpperDotTheme {
                InCallScreen(
                    call = call,
                    state = callState,
                    isMuted = isMuted,
                    isSpeakerOn = isSpeakerOn,
                    displayName = callerName,
                    onMuteClick = {
                        isMuted = !isMuted
                        UpperDotInCallService.instance?.setMuted(isMuted)
                    },
                    onSpeakerClick = {
                        isSpeakerOn = !isSpeakerOn
                        val route = if (isSpeakerOn) CallAudioState.ROUTE_SPEAKER else CallAudioState.ROUTE_EARPIECE
                        UpperDotInCallService.instance?.setAudioRoute(route)
                    },
                    onAnswer = {
                        call.answer(android.telecom.VideoProfile.STATE_AUDIO_ONLY)
                    },
                    onHangup = {
                        call.disconnect()
                        cleanupAndFinish()
                    },
                    onAddNote = { phone ->
                        val intent = Intent(Intent.ACTION_VIEW).apply {
                            data = android.net.Uri.parse("upperdot://create_note?phone=$phone")
                            setClass(this@InCallActivity, com.mail2dev.upperdot.MainActivity::class.java)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        startActivity(intent)
                    }
                )
            }
        }
    }

    private fun cleanupAndFinish() {
        audioManager.mode = AudioManager.MODE_NORMAL
        UpperDotInCallService.activeCall?.unregisterCallback(callback)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanupAndFinish()
    }
}
