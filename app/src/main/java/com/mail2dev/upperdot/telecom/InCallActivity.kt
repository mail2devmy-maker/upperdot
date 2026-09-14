package com.mail2dev.upperdot.telecom

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.telecom.Call
import android.telecom.CallAudioState
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.mail2dev.upperdot.data.local.entity.ContactEntity
import com.mail2dev.upperdot.data.local.entity.NoteEntity
import com.mail2dev.upperdot.ui.theme.UpperDotTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InCallActivity : ComponentActivity() {

    private var callState by mutableIntStateOf(Call.STATE_NEW)
    private var isMuted by mutableStateOf(false)
    private var isSpeakerOn by mutableStateOf(false)
    private var showDialpad by mutableStateOf(false)
    private var callerName by mutableStateOf<String?>(null)
    private var contactId by mutableStateOf<Long?>(null)

    private lateinit var audioManager: AudioManager

    private val callback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            callState = state
            if (state == Call.STATE_DISCONNECTED) {
                cleanupAndFinish()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        wakeUpDisplay()

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as android.app.KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                syncCallState()
            }
        }

        renderUi()
    }

    private fun syncCallState() {
        UpperDotInCallService.instance?.callAudioState?.let {
            isMuted = it.isMuted
            isSpeakerOn = it.route == CallAudioState.ROUTE_SPEAKER
        }

        val call = UpperDotInCallService.activeCall
        if (call == null) {
            finish()
            return
        }

        call.unregisterCallback(callback)
        call.registerCallback(callback)
        @Suppress("DEPRECATION")
        callState = call.state

        val handle = call.details.handle?.schemeSpecificPart
        if (handle != null && !com.mail2dev.upperdot.util.ContactUtils.isUssdCode(handle)) {
            val app = applicationContext as com.mail2dev.upperdot.UpperDotApp
            lifecycleScope.launch {
                val contact = app.contactRepository.findContactByPhone(handle)
                callerName = contact?.fullName
                contactId = contact?.id
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        syncCallState()
    }

    private fun saveQuickNote(content: String) {
        val app = applicationContext as com.mail2dev.upperdot.UpperDotApp
        val handle = UpperDotInCallService.activeCall?.details?.handle?.schemeSpecificPart ?: return
        
        lifecycleScope.launch(Dispatchers.IO) {
            var targetContactId = contactId
            
            // If contact is unknown, create a minimal "Quick Contact"
            if (targetContactId == null) {
                val newContact = ContactEntity(
                    fullName = "Unknown ($handle)",
                    nicknames = emptyList(),
                    phoneNumbers = listOf(handle),
                    sanitizedPrimaryPhone = com.mail2dev.upperdot.util.ContactUtils.smartSanitize(handle),
                    emails = emptyList(),
                    socialProfiles = emptyList(),
                    bankAccounts = emptyList()
                )
                app.contactRepository.insertContact(newContact)
                val resolved = app.contactRepository.findContactByPhone(handle)
                targetContactId = resolved?.id
            }
            
            if (targetContactId != null) {
                val note = NoteEntity(
                    contactId = targetContactId,
                    title = "In-Call Note",
                    content = content
                )
                app.noteRepository.insertNote(note)
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@InCallActivity, "Note saved", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun renderUi() {
        setContent {
            UpperDotTheme {
                val call = UpperDotInCallService.activeCall
                if (call != null) {
                    InCallScreen(
                        call = call,
                        state = callState,
                        isMuted = isMuted,
                        isSpeakerOn = isSpeakerOn,
                        showDialpad = showDialpad,
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
                        onKeypadClick = {
                            showDialpad = !showDialpad
                        },
                        onDtmfPress = { digit ->
                            UpperDotInCallService.instance?.playDtmf(digit)
                        },
                        onDtmfRelease = {
                            UpperDotInCallService.instance?.stopDtmf()
                        },
                        onAnswer = {
                            call.answer(android.telecom.VideoProfile.STATE_AUDIO_ONLY)
                        },
                        onHangup = {
                            call.disconnect()
                            cleanupAndFinish()
                        },
                        onSaveNote = { content ->
                            saveQuickNote(content)
                        }
                    )
                } else {
                    SideEffect { finish() }
                }
            }
        }
    }

    private fun wakeUpDisplay() {
        try {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            @Suppress("DEPRECATION")
            val wakeLock = powerManager.newWakeLock(
                PowerManager.FULL_WAKE_LOCK or
                        PowerManager.ACQUIRE_CAUSES_WAKEUP or
                        PowerManager.ON_AFTER_RELEASE,
                "UpperDot:InCallWakeLock"
            )
            wakeLock.acquire(3000)
        } catch (e: Exception) {
            Log.e("CallDebug", "Failed to acquire WakeLock", e)
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
