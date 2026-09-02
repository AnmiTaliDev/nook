package dev.anmitali.nook.feature.archive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.anmitali.nook.core.model.ArchiveEntry
import java.io.File
import java.util.Locale

@Composable
fun ArchiveContentsScreen(
    archivePath: String,
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ArchiveContentsViewModel = hiltViewModel(),
) {
    LaunchedEffect(archivePath) { viewModel.load(archivePath) }
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(File(archivePath).name) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.archive_navigate_up),
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val state = uiState) {
                is ArchiveContentsUiState.Loading ->
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is ArchiveContentsUiState.Error ->
                    Text(
                        text = state.message,
                        modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    )
                is ArchiveContentsUiState.Content -> ArchiveEntryList(entries = state.entries)
            }
        }
    }
}

@Composable
private fun ArchiveEntryList(entries: List<ArchiveEntry>) {
    if (entries.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(text = stringResource(R.string.archive_empty), modifier = Modifier.align(Alignment.Center))
        }
        return
    }

    val folderDescription = stringResource(R.string.archive_folder_content_description)
    val fileDescription = stringResource(R.string.archive_file_content_description)

    LazyColumn {
        items(entries, key = { it.path }) { entry ->
            ListItem(
                headlineContent = { Text(entry.name) },
                supportingContent = { Text(entry.path) },
                leadingContent = {
                    Icon(
                        imageVector = if (entry.isDirectory) Icons.Filled.Folder else Icons.AutoMirrored.Filled.InsertDriveFile,
                        contentDescription = if (entry.isDirectory) folderDescription else fileDescription,
                    )
                },
                trailingContent = if (!entry.isDirectory) {
                    { Text(formatEntrySize(entry.sizeBytes)) }
                } else {
                    null
                },
            )
        }
    }
}

private fun formatEntrySize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val units = listOf("KB", "MB", "GB", "TB")
    var value = bytes.toDouble()
    var unitIndex = -1
    while (value >= 1024 && unitIndex < units.lastIndex) {
        value /= 1024
        unitIndex++
    }
    return String.format(Locale.getDefault(), "%.1f %s", value, units[unitIndex])
}
