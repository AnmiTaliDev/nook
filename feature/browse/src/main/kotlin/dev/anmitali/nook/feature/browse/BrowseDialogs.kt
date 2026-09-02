package dev.anmitali.nook.feature.browse

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.anmitali.nook.core.model.FileConflictPolicy
import dev.anmitali.nook.core.model.FileDetails
import dev.anmitali.nook.core.model.FileOperationProgress
import java.text.DateFormat
import java.util.Date
import java.util.Locale

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
fun CreateArchiveDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var name by remember { mutableStateOf("Archive.zip") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.browse_compress_title)) },
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
fun PasswordPromptDialog(
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var password by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.browse_password_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.browse_password_hint)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                )
                if (errorMessage != null) {
                    Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(password) }, enabled = password.isNotBlank()) {
                Text(stringResource(R.string.browse_password_confirm))
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
fun InfoDialog(
    itemName: String,
    details: FileDetails?,
    isLoading: Boolean,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(itemName) },
        text = {
            if (isLoading || details == null) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Column {
                    InfoRow(stringResource(R.string.browse_info_path), details.path)
                    InfoRow(stringResource(R.string.browse_info_size), formatFileSize(details.sizeBytes))
                    details.itemCount?.let { InfoRow(stringResource(R.string.browse_info_item_count), it.toString()) }
                    details.createdEpochMillis?.let {
                        InfoRow(stringResource(R.string.browse_info_created), formatDateTime(it))
                    }
                    InfoRow(stringResource(R.string.browse_info_modified), formatDateTime(details.modifiedEpochMillis))
                    details.mimeType?.let { InfoRow(stringResource(R.string.browse_info_mime_type), it) }
                    InfoRow(stringResource(R.string.browse_info_permissions), permissionsText(details))
                    details.owner?.let { InfoRow(stringResource(R.string.browse_info_owner), it) }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.browse_cancel)) } },
    )
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(text = label, style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(1f))
        Text(text = value, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(2f))
    }
}

private fun formatDateTime(epochMillis: Long): String {
    val format = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault())
    return format.format(Date(epochMillis))
}

private fun permissionsText(details: FileDetails): String {
    val parts = mutableListOf<String>()
    if (details.canRead) parts += "r"
    if (details.canWrite) parts += "w"
    if (details.canExecute) parts += "x"
    return parts.joinToString("")
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
