package com.example.taskmanager.core.utils

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * @author Abdallah Elsokkary
 */
@Composable
fun DeleteManagerDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isLoading: Boolean,
    firstName: String,
    lastName: String
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = {
                Text(
                    text = "Delete User",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column {
                    Text(
                        text = "Are you sure you want to delete $firstName $lastName? This action cannot be undone.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (isLoading) {
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    enabled = !isLoading
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}