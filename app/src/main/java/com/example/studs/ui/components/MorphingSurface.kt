package com.example.studs.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun MorphingSurface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    initialCornerRadius: Dp = 28.dp, // Default ExtraLarge-ish
    color: Color = MaterialTheme.colorScheme.surface,
    morphOnPress: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Animate to capsule (large radius) when pressed if enabled
    val cornerRadius by animateDpAsState(
        targetValue = if (morphOnPress && isPressed) 100.dp else initialCornerRadius,
        label = "CornerRadiusAnimation"
    )

    // Slightly increase size on press
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.03f else 1f,
        label = "ScaleAnimation"
    )

    Surface(
        onClick = onClick,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        shape = RoundedCornerShape(cornerRadius),
        color = color,
        interactionSource = interactionSource,
        content = content
    )
}
