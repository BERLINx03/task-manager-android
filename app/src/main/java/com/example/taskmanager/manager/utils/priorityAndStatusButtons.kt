package com.example.taskmanager.manager.utils

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * @author Abdallah Elsokkary
 */

@Composable
fun PriorityButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier

) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.height(40.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) color else Color.Transparent,
            contentColor = if (isSelected) Color.White else color
        ),
        border = BorderStroke(1.dp, color),
    ) {
        Text(text = text, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun StatusButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    color: Color
) {
    val backgroundColor = if (isSelected) {
        color.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.surface
    }

    val borderColor = if (isSelected) {
        color
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }

    val textColor = if (isSelected) {
        color
    } else {
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.height(36.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
                color = textColor
            )
        }
    }
}