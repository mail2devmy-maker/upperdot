package com.mail2dev.upperdot.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mail2dev.upperdot.ui.theme.AccentCyan
import com.mail2dev.upperdot.ui.theme.Surface

@Composable
fun FilterCapsule(
    text: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    backgroundColor: Color = if (isSelected) AccentCyan else Surface,
    contentColor: Color = if (isSelected) Color.Black else Color.White,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        modifier = modifier.height(32.dp)
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = contentColor
            )
        }
    }
}
