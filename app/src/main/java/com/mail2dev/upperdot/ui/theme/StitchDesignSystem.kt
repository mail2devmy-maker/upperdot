package com.mail2dev.upperdot.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Stitch Design System - Global Component Library
 * Ensures 100% uniformity across all UpperDot screens for Play Store readiness.
 */
object StitchDesignSystem {

    /**
     * Standardized Left-Aligned Top Bar
     */
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun TopBar(
        title: String,
        navigationIcon: @Composable (() -> Unit)? = null,
        actions: @Composable RowScope.() -> Unit = {},
        leadingIcon: ImageVector? = null
    ) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (leadingIcon != null) {
                        Icon(
                            imageVector = leadingIcon,
                            contentDescription = null,
                            tint = AccentCyan,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Text(
                        text = title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            navigationIcon = navigationIcon ?: {},
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Black,
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White,
                actionIconContentColor = Color.White
            )
        )
    }

    /**
     * Standardized Primary Action FAB
     * Always Solid Cyan Circle with Black Icon.
     */
    @Composable
    fun FAB(
        icon: ImageVector,
        contentDescription: String,
        onClick: () -> Unit,
        modifier: Modifier = Modifier
    ) {
        FloatingActionButton(
            onClick = onClick,
            containerColor = AccentCyan,
            contentColor = Color.Black,
            shape = CircleShape,
            modifier = modifier
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp)
            )
        }
    }

    /**
     * Standardized Container Card
     */
    @Composable
    fun Card(
        modifier: Modifier = Modifier,
        onClick: (() -> Unit)? = null,
        content: @Composable ColumnScope.() -> Unit
    ) {
        val cardModifier = modifier.fillMaxWidth()
        val shape = RoundedCornerShape(24.dp)
        val border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        val colors = CardDefaults.cardColors(containerColor = Surface)

        if (onClick != null) {
            androidx.compose.material3.Card(
                onClick = onClick,
                modifier = cardModifier,
                shape = shape,
                border = border,
                colors = colors,
                content = content
            )
        } else {
            androidx.compose.material3.Card(
                modifier = cardModifier,
                shape = shape,
                border = border,
                colors = colors,
                content = content
            )
        }
    }
}
