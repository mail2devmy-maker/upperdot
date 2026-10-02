package com.mail2dev.upperdot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mail2dev.upperdot.ui.theme.Surface

data class KeypadDigit(val digit: Char, val subLabel: String)

@Composable
fun TelephonyKeypad(
    modifier: Modifier = Modifier,
    onDigitClick: ((Char) -> Unit)? = null,
    onLongClickZero: (() -> Unit)? = null,
    onPressDown: ((Char) -> Unit)? = null,
    onPressUp: (() -> Unit)? = null
) {
    val keys = listOf(
        KeypadDigit('1', ""),
        KeypadDigit('2', "A B C"),
        KeypadDigit('3', "D E F"),
        KeypadDigit('4', "G H I"),
        KeypadDigit('5', "J K L"),
        KeypadDigit('6', "M N O"),
        KeypadDigit('7', "P Q R S"),
        KeypadDigit('8', "T U V"),
        KeypadDigit('9', "W X Y Z"),
        KeypadDigit('*', ""),
        KeypadDigit('0', "+"),
        KeypadDigit('#', "")
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(horizontal = 32.dp)
    ) {
        items(keys) { key ->
            KeypadButton(
                key = key,
                onClick = { onDigitClick?.invoke(key.digit) },
                onLongClick = { if (key.digit == '0') onLongClickZero?.invoke() },
                onPressDown = { onPressDown?.invoke(key.digit) },
                onPressUp = { onPressUp?.invoke() }
            )
        }
    }
}

@Composable
private fun KeypadButton(
    key: KeypadDigit,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onPressDown: () -> Unit,
    onPressUp: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .aspectRatio(1.2f) // More compact rectangular hit zone
            .clip(CircleShape)
            .background(if (isPressed) Color.White.copy(alpha = 0.1f) else Color.Transparent)
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    isPressed = true
                    onPressDown()
                    
                    val up = waitForUpOrCancellation()
                    isPressed = false
                    onPressUp()
                    
                    if (up != null) {
                        val duration = up.uptimeMillis - down.uptimeMillis
                        if (duration > 500) {
                            onLongClick()
                        } else {
                            onClick()
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = key.digit.toString(),
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Normal,
                lineHeight = 30.sp
            )
            if (key.subLabel.isNotEmpty()) {
                Text(
                    text = key.subLabel,
                    color = Color.White.copy(alpha = 0.35f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            } else if (key.digit == '1' || key.digit == '*' || key.digit == '#') {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
