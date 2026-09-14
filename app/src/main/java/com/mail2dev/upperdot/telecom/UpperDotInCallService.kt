package com.mail2dev.upperdot.telecom

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.provider.Settings
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.InCallService
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import com.mail2dev.upperdot.R
import com.mail2dev.upperdot.UpperDotApp
import kotlinx.coroutines.*

class UpperDotInCallService : InCallService() {

    companion object {
        var instance: UpperDotInCallService? = null
        var activeCall: Call? = null

        const val CHANNEL_ID = "call_notifications"
        const val NOTIFICATION_ID = 101

        const val ACTION_ANSWER = "com.mail2dev.upperdot.ACTION_ANSWER"
        const val ACTION_DECLINE = "com.mail2dev.upperdot.ACTION_DECLINE"
        const val ACTION_HANGUP = "com.mail2dev.upperdot.ACTION_HANGUP"
        const val ACTION_TOGGLE_MUTE = "com.mail2dev.upperdot.ACTION_TOGGLE_MUTE"

        const val MISSED_CALL_CHANNEL_ID = "missed_calls"
        const val MISSED_CALL_NOTIFICATION_ID = 102
    }

    private var wasCallAnswered = false
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private var resolvedCallerName: String? = null
    private var ringtone: Ringtone? = null

    private val callCallback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            if (state == Call.STATE_ACTIVE) {
                wasCallAnswered = true
                stopRingtone()
            }
            updateNotification(call)
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannel()
        createMissedCallChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_ANSWER -> {
                activeCall?.answer(android.telecom.VideoProfile.STATE_AUDIO_ONLY)
                stopRingtone()
                showInCallActivity()
            }
            ACTION_DECLINE -> {
                stopRingtone()
                activeCall?.disconnect()
            }
            ACTION_HANGUP -> {
                stopRingtone()
                activeCall?.disconnect()
            }
            ACTION_TOGGLE_MUTE -> toggleMute()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startRingtone() {
        if (ringtone != null) return
        try {
            val uri = Settings.System.DEFAULT_RINGTONE_URI
            ringtone = RingtoneManager.getRingtone(applicationContext, uri)
            ringtone?.audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            ringtone?.play()
        } catch (e: Exception) {
            Log.e("InCallService", "Error playing ringtone", e)
        }
    }

    private fun stopRingtone() {
        ringtone?.stop()
        ringtone = null
    }

    fun toggleMute() {
        @Suppress("DEPRECATION")
        val currentMute = callAudioState?.isMuted ?: false
        setMuted(!currentMute)
        activeCall?.let { updateNotification(it) }
    }

    fun playDtmf(digit: Char) {
        activeCall?.playDtmfTone(digit)
    }

    fun stopDtmf() {
        activeCall?.stopDtmfTone()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRingtone()
        serviceJob.cancel()
        instance = null
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        val handle = call.details.handle?.schemeSpecificPart ?: "Unknown"
        Log.d("UpperDotInCallService", "Call added: $handle")
        instance = this
        activeCall = call
        wasCallAnswered = false
        resolvedCallerName = null
        call.registerCallback(callCallback)

        // Handle Ringing Logic
        if (call.state == Call.STATE_RINGING) {
            startRingtone()
            updateNotification(call)
        } else {
            updateNotification(call)
            showInCallActivity()
        }

        // Asynchronous Name Resolution
        if (!com.mail2dev.upperdot.util.ContactUtils.isUssdCode(handle)) {
            serviceScope.launch {
                val app = applicationContext as UpperDotApp
                val contact = app.contactRepository.findContactByPhone(handle)
                if (contact != null) {
                    resolvedCallerName = contact.fullName
                    updateNotification(call)
                }
            }
        }
    }

    private fun showInCallActivity() {
        val intent = Intent(this, InCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
        }

        try {
            startActivity(intent)
        } catch (e: Exception) {
            Log.e("UpperDotInCallService", "Failed to launch InCallActivity", e)
        }
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        Log.d("UpperDotInCallService", "Call removed")
        stopRingtone()

        if (!wasCallAnswered && call.state == Call.STATE_DISCONNECTED) {
            val cause = call.details.disconnectCause
            if (cause.code == android.telecom.DisconnectCause.MISSED ||
                cause.code == android.telecom.DisconnectCause.CANCELED) {
                showMissedCallNotification(call)
            }
        }

        call.unregisterCallback(callCallback)
        if (activeCall == call) {
            activeCall = null
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).cancel(NOTIFICATION_ID)
    }

    private fun createMissedCallChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                MISSED_CALL_CHANNEL_ID,
                "Missed Calls",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun showMissedCallNotification(call: Call) {
        val handle = call.details.handle?.schemeSpecificPart ?: "Unknown"
        val displayName = resolvedCallerName ?: handle

        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, com.mail2dev.upperdot.MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                data = android.net.Uri.parse("upperdot://call_history")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, MISSED_CALL_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_missed_call)
            .setContentTitle("Missed Call")
            .setContentText("Incoming call from $displayName")
            .setContentIntent(contentIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(MISSED_CALL_NOTIFICATION_ID, builder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "In-Call Screen"
            val descriptionText = "Active incoming and outgoing call notification"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                enableVibration(true)
                setBypassDnd(true)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    @Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")
    override fun onCallAudioStateChanged(audioState: CallAudioState?) {
        super.onCallAudioStateChanged(audioState)
        activeCall?.let { updateNotification(it) }
    }

    private fun updateNotification(call: Call) {
        val handle = call.details.handle?.schemeSpecificPart ?: "Unknown"
        val displayName = resolvedCallerName ?: handle
        val state = call.state
        @Suppress("DEPRECATION")
        val isMuted = callAudioState?.isMuted ?: false

        val contentIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, InCallActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setContentTitle("UpperDot Call")
            .setContentText(displayName)
            .setContentIntent(contentIntent)
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setAutoCancel(false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        if (state == Call.STATE_RINGING) {
            // HIGH PRIORITY: Triggers heads-up popup and lockscreen intent
            builder.setPriority(NotificationCompat.PRIORITY_MAX)
            builder.setFullScreenIntent(contentIntent, true)

            val answerIntent = PendingIntent.getService(
                this, 1,
                Intent(this, UpperDotInCallService::class.java).apply { action = ACTION_ANSWER },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val declineIntent = PendingIntent.getService(
                this, 2,
                Intent(this, UpperDotInCallService::class.java).apply { action = ACTION_DECLINE },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val style = NotificationCompat.CallStyle.forIncomingCall(
                Person.Builder().setName(displayName).build(),
                declineIntent,
                answerIntent
            )
            builder.setStyle(style)
        } else {
            // DEFAULT PRIORITY: Ongoing call stays in status bar without "popping"
            builder.setPriority(NotificationCompat.PRIORITY_DEFAULT)
            builder.setFullScreenIntent(null, false)

            val hangupIntent = PendingIntent.getService(
                this, 3,
                Intent(this, UpperDotInCallService::class.java).apply { action = ACTION_HANGUP },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val muteIntent = PendingIntent.getService(
                this, 4,
                Intent(this, UpperDotInCallService::class.java).apply { action = ACTION_TOGGLE_MUTE },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val style = NotificationCompat.CallStyle.forOngoingCall(
                Person.Builder().setName(displayName).build(),
                hangupIntent
            )
            builder.setStyle(style)

            // Accuracy fix: only show timer for actually active calls
            if (state == Call.STATE_ACTIVE) {
                builder.setUsesChronometer(true)
                builder.setWhen(call.details.connectTimeMillis)
            }

            // Custom Mute action in the notification body
            val muteLabel = if (isMuted) "Unmute" else "Mute"
            val muteIcon = if (isMuted) android.R.drawable.ic_lock_silent_mode else android.R.drawable.ic_lock_silent_mode_off
            builder.addAction(
                NotificationCompat.Action.Builder(
                    muteIcon,
                    muteLabel,
                    muteIntent
                ).build()
            )
        }

        startForeground(NOTIFICATION_ID, builder.build())
    }
}
