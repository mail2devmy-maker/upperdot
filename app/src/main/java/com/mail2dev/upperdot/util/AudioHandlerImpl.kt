package com.mail2dev.upperdot.util

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class AudioHandlerImpl(private val context: Context) : AudioHandler {

    private val _isRecording = MutableStateFlow(false)
    override val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _recordingDurationMs = MutableStateFlow(0L)
    override val recordingDurationMs: StateFlow<Long> = _recordingDurationMs.asStateFlow()

    private val _playbackPositionMs = MutableStateFlow(0L)
    override val playbackPositionMs: StateFlow<Long> = _playbackPositionMs.asStateFlow()

    private val _audioDurationMs = MutableStateFlow(0L)
    override val audioDurationMs: StateFlow<Long> = _audioDurationMs.asStateFlow()

    private val _amplitudes = MutableStateFlow<List<Float>>(emptyList())
    override val amplitudes: StateFlow<List<Float>> = _amplitudes.asStateFlow()

    private var recorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    
    private val handlerScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var recordingJob: Job? = null
    private var playbackJob: Job? = null

    override fun startRecording(filePath: String) {
        if (_isRecording.value) return
        stopPlayback()

        try {
            val file = File(filePath)
            val parent = file.parentFile
            if (parent != null && !parent.exists()) {
                parent.mkdirs()
            }

            @Suppress("DEPRECATION")
            val newRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(filePath)
                prepare()
                start()
            }

            recorder = newRecorder
            _isRecording.value = true
            _recordingDurationMs.value = 0L
            _amplitudes.value = emptyList()

            val startTime = System.currentTimeMillis()
            recordingJob = handlerScope.launch {
                val currentAmplitudes = mutableListOf<Float>()
                while (isActive && _isRecording.value) {
                    delay(100)
                    _recordingDurationMs.value = System.currentTimeMillis() - startTime
                    val amplitude = try {
                        recorder?.maxAmplitude?.toFloat() ?: 0f
                    } catch (_: Exception) { 0f }
                    
                    currentAmplitudes.add(amplitude)
                    if (currentAmplitudes.size > 100) {
                        currentAmplitudes.removeAt(0)
                    }
                    _amplitudes.value = currentAmplitudes.toList()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _isRecording.value = false
            recorder?.release()
            recorder = null
        }
    }

    override fun stopRecording() {
        if (!_isRecording.value) return
        recordingJob?.cancel()
        recordingJob = null

        try {
            recorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            recorder = null
            _isRecording.value = false
            _audioDurationMs.value = _recordingDurationMs.value
        }
    }

    override fun startPlayback(filePath: String) {
        if (_isRecording.value) return
        
        if (mediaPlayer != null && !_isPlaying.value) {
            // Resume playback
            try {
                mediaPlayer?.start()
                _isPlaying.value = true
                startPlaybackTimer()
                return
            } catch (e: Exception) {
                e.printStackTrace()
                stopPlayback()
            }
        }

        stopPlayback()

        try {
            val file = File(filePath)
            if (!file.exists()) return

            // Extract duration safely
            try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(filePath)
                val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                retriever.release()
                val duration = time?.toLongOrNull() ?: 0L
                if (duration > 0) {
                    _audioDurationMs.value = duration
                }
            } catch (_: Exception) {}

            val player = MediaPlayer().apply {
                setDataSource(filePath)
                prepare()
                if (_audioDurationMs.value == 0L) {
                    _audioDurationMs.value = duration.toLong()
                }
                setOnCompletionListener {
                    _isPlaying.value = false
                    _playbackPositionMs.value = _audioDurationMs.value
                    playbackJob?.cancel()
                }
                start()
            }

            mediaPlayer = player
            _isPlaying.value = true
            _playbackPositionMs.value = 0L
            startPlaybackTimer()

        } catch (e: Exception) {
            e.printStackTrace()
            stopPlayback()
        }
    }

    private fun startPlaybackTimer() {
        playbackJob?.cancel()
        playbackJob = handlerScope.launch {
            while (isActive && _isPlaying.value) {
                _playbackPositionMs.value = mediaPlayer?.currentPosition?.toLong() ?: 0L
                delay(100)
            }
        }
    }

    override fun pausePlayback() {
        if (!_isPlaying.value) return
        try {
            mediaPlayer?.pause()
            _isPlaying.value = false
            playbackJob?.cancel()
            playbackJob = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun seekTo(positionMs: Long) {
        try {
            mediaPlayer?.seekTo(positionMs.toInt())
            _playbackPositionMs.value = positionMs
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun stopPlayback() {
        playbackJob?.cancel()
        playbackJob = null
        _isPlaying.value = false
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            mediaPlayer = null
            _playbackPositionMs.value = 0L
        }
    }

    override fun release() {
        stopRecording()
        stopPlayback()
        handlerScope.cancel()
    }
}
