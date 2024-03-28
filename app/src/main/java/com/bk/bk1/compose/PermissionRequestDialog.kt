package com.bk.bk1.compose

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun PermissionRequestDialog(
    messageText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Potřebné oprávnění") },
        text = { Text(messageText) },
        confirmButton = {
            Button(onClick = {
                onConfirm()
                onDismiss()
            }) {
                Text("Potvrdit")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text("Zrušit")
            }
        }
    )
}