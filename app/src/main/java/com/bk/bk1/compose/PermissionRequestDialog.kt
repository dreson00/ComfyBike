package com.bk.bk1.compose

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.bk.bk1.R

@Composable
fun PermissionRequestDialog(
    messageText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(stringResource(R.string.dialog_perm_req)) },
        text = { Text(messageText) },
        confirmButton = {
            Button(onClick = {
                onConfirm()
                onDismiss()
            }) {
                Text(stringResource(R.string.btn_confirm))
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text(stringResource(R.string.btn_cancel))
            }
        }
    )
}