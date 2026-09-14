package com.mail2dev.upperdot.util

import kotlinx.coroutines.flow.StateFlow

interface AudioHandler {
    val isRecording: StateFlow<Boolean>
    val isPlaying: StateFlow<Boolean>
    val recordingDurationMs: StateFlow<Long>
    val playbackPositionMs: StateFlow<Long>
    val audioDurationMs: StateFlow<Long>
    val amplitudes: StateFlow<List<Float>>

    fun startRecording(filePath: String)
    fun stopRecording()
    fun startPlayback(filePath: String)
    fun pausePlayback()
    fun seekTo(positionMs: Long)
    fun stopPlayback()
    fun release()
}
