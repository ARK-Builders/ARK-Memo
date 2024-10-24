package dev.arkbuilders.arkmemo.ui.views.presentation.edit

import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ConfirmClearDialog(
    show: MutableState<Boolean>,
    onConfirm: () -> Unit
) {
    if (!show.value) return

    AlertDialog(
        onDismissRequest = {
            show.value = false
        },
        title = {
            Text(
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                text = "Are you sure to clear all edits?",
                fontSize = 16.sp
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    show.value = false
                    onConfirm()
                }
            ) {
                Text("Clear")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    show.value = false
                }
            ) {
                Text("Cancel")
            }
        }
    )
}
