package com.mail2dev.upperdot.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mail2dev.upperdot.ui.theme.AccentCyan
import com.mail2dev.upperdot.ui.theme.Surface
import com.mail2dev.upperdot.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun RecordingIndicator(
    durationMs: Long,
    amplitudes: List<Float>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(Color.Red, CircleShape)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = formatVoiceDuration(durationMs),
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(12.dp))
            
            // Simple dynamic amplitude visualization
            Box(modifier = Modifier.weight(1f).height(40.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val barWidth = 4.dp.toPx()
                    val gap = 2.dp.toPx()
                    val maxBars = (width / (barWidth + gap)).toInt()
                    
                    val visibleAmplitudes = amplitudes.takeLast(maxBars)
                    visibleAmplitudes.forEachIndexed { index, amplitude ->
                        val barHeight = (amplitude / 32767f) * height * 2f // Scale factor
                        val x = index * (barWidth + gap)
                        drawRect(
                            color = Color.Red,
                            topLeft = Offset(x, (height - barHeight) / 2),
                            size = Size(barWidth, barHeight.coerceAtLeast(4f))
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VoiceMemoPlayerCard(
    isPlaying: Boolean,
    positionMs: Long,
    durationMs: Long,
    onTogglePlayback: () -> Unit,
    onSeek: (Long) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, AccentCyan.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onTogglePlayback,
                    modifier = Modifier
                        .size(48.dp)
                        .background(AccentCyan.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = AccentCyan
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Slider(
                        value = if (durationMs > 0) positionMs.toFloat() / durationMs else 0f,
                        onValueChange = { onSeek((it * durationMs).toLong()) },
                        colors = SliderDefaults.colors(
                            thumbColor = AccentCyan,
                            activeTrackColor = AccentCyan,
                            inactiveTrackColor = Color.DarkGray
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(formatVoiceDuration(positionMs), color = TextSecondary, fontSize = 10.sp)
                        Text(formatVoiceDuration(durationMs), color = TextSecondary, fontSize = 10.sp)
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

fun formatVoiceDuration(ms: Long): String {
    val seconds = (ms / 1000) % 60
    val minutes = (ms / (1000 * 60)) % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}
