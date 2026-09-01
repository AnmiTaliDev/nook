package dev.anmitali.nook.feature.browse

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.anmitali.nook.core.model.FileConflictPolicy
import dev.anmitali.nook.core.model.FileOperationProgress

@Composable
fun AddEntryChooserDialog(
    onDismiss: () -> Unit,
    onChooseFolder: () -> Unit,
    onChooseFile: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.browse_cancel)) } },
        text = {
            Column {
                TextButton(onClick = onChooseFolder) { Text(stringResource(R.string.browse_new_folder)) }
                TextButton(onClick = onChooseFile) { Text(stringResource(R.string.browse_new_file)) }
            }
        },
    )
}

@Composable
fun CreateEntryDialog(
    type: CreateEntryType,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                stringResource(
                    if (type == CreateEntryType.FOLDER) R.string.browse_create_folder_title else R.string.browse_create_file_title,
                ),
            )
        },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.browse_name_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(name) }, enabled = name.isNotBlank()) {
                Text(stringResource(R.string.browse_create_confirm))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.browse_cancel)) } },
    )
}

@Composable
fun RenameDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember { mutableStateOf(currentName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.browse_rename_title)) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            Button(onClick = { onConfirm(name) }, enabled = name.isNotBlank() && name != currentName) {
                Text(stringResource(R.string.browse_rename_confirm))
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.browse_cancel)) } },
    )
}

@Composable
fun DeleteConfirmationDialog(
    itemCount: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.browse_delete_confirm_title, itemCount)) },
        text = { Text(stringResource(R.string.browse_delete_confirm_message)) },
        confirmButton = {
            Button(onClick = onConfirm) { Text(stringResource(R.string.browse_delete_confirm_action)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.browse_cancel)) } },
    )
}

@Composable
fun ConflictDialog(
    conflictingNames: List<String>,
    onDismiss: () -> Unit,
    onResolve: (FileConflictPolicy) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.browse_conflict_title, conflictingNames.size)) },
        text = {
            Column {
                Text(stringResource(R.string.browse_conflict_message))
                TextButton(onClick = { onResolve(FileConflictPolicy.OVERWRITE) }) {
                    Text(stringResource(R.string.browse_conflict_overwrite))
                }
                TextButton(onClick = { onResolve(FileConflictPolicy.SKIP) }) {
                    Text(stringResource(R.string.browse_conflict_skip))
                }
                TextButton(onClick = { onResolve(FileConflictPolicy.KEEP_BOTH) }) {
                    Text(stringResource(R.string.browse_conflict_keep_both))
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.browse_cancel)) } },
    )
}

@Composable
fun OperationErrorDialog(message: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.browse_error_title)) },
        text = { Text(message) },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.browse_cancel)) } },
    )
}

@Composable
fun OperationProgressDialog(progress: FileOperationProgress, onCancel: () -> Unit) {
    Dialog(onDismissRequest = onCancel, properties = DialogProperties(dismissOnClickOutside = false)) {
        Column {
            Text(stringResource(R.string.browse_operation_progress_title))
            if (progress.totalItems > 0) {
                LinearProgressIndicator(
                    progress = { progress.completedItems.toFloat() / progress.totalItems },
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                CircularProgressIndicator()
            }
            Text(progress.currentItemName)
            TextButton(onClick = onCancel) { Text(stringResource(R.string.browse_operation_cancel)) }
        }
    }
}
