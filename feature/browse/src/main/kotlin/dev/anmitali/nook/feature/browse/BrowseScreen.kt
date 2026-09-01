package dev.anmitali.nook.feature.browse

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.anmitali.nook.core.model.FileItem
import java.text.DateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BrowseScreen(
    onOpenFile: (FileItem) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BrowseViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    BackHandler(enabled = viewModel.canNavigateUp) {
        viewModel.onNavigateUp()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    val breadcrumbs = (uiState as? BrowseUiState.Content)?.breadcrumbs.orEmpty()
                    BreadcrumbBar(
                        segments = breadcrumbs,
                        onSegmentClick = { index ->
                            val path = "/" + breadcrumbs.take(index + 1).joinToString("/")
                            viewModel.onDirectoryOpened(path)
                        },
                    )
                },
                navigationIcon = {
                    if (viewModel.canNavigateUp) {
                        IconButton(onClick = { viewModel.onNavigateUp() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.browse_navigate_up),
                            )
                        }
                    }
                },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            when (val state = uiState) {
                is BrowseUiState.Loading -> LoadingContent()
                is BrowseUiState.PermissionRequired -> PermissionRequiredContent(
                    onGrantAccess = { viewModel.onPermissionGranted() },
                )
                is BrowseUiState.Error -> ErrorContent(message = state.message)
                is BrowseUiState.Content -> FileListContent(
                    items = state.items,
                    onItemClick = { item ->
                        if (item.isDirectory) {
                            viewModel.onDirectoryOpened(item.path)
                        } else {
                            onOpenFile(item)
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun BreadcrumbBar(
    segments: List<String>,
    onSegmentClick: (Int) -> Unit,
) {
    LazyRow(verticalAlignment = Alignment.CenterVertically) {
        items(segments.size) { index ->
            val description = stringResource(R.string.browse_breadcrumb_content_description, segments[index])
            TextButton(
                onClick = { onSegmentClick(index) },
                modifier = Modifier.semantics { contentDescription = description },
            ) {
                Text(text = segments[index])
            }
            if (index != segments.lastIndex) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                    contentDescription = null,
                )
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = stringResource(R.string.browse_error_title), style = MaterialTheme.typography.titleMedium)
            Text(text = message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun PermissionRequiredContent(onGrantAccess: () -> Unit) {
    val context = LocalContext.current
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(24.dp),
        ) {
            Icon(imageVector = Icons.Filled.LockOpen, contentDescription = null)
            Text(text = stringResource(R.string.browse_permission_title), style = MaterialTheme.typography.titleLarge)
            Text(text = stringResource(R.string.browse_permission_message), style = MaterialTheme.typography.bodyMedium)
            Button(onClick = {
                val intent = Intent(
                    Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                    Uri.parse("package:" + context.packageName),
                )
                context.startActivity(intent)
                onGrantAccess()
            }) {
                Text(text = stringResource(R.string.browse_permission_grant_action))
            }
        }
    }
}

@Composable
private fun FileListContent(
    items: List<FileItem>,
    onItemClick: (FileItem) -> Unit,
) {
    if (items.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = stringResource(R.string.browse_empty_directory))
        }
        return
    }

    LazyColumn {
        items(items, key = { it.path }) { item ->
            FileRow(item = item, onClick = { onItemClick(item) })
        }
    }
}

@Composable
private fun FileRow(item: FileItem, onClick: () -> Unit) {
    val folderDescription = stringResource(R.string.browse_folder_content_description)
    val fileDescription = stringResource(R.string.browse_file_content_description)
    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(text = item.name) },
        supportingContent = { Text(text = formatSubtitle(item)) },
        leadingContent = {
            Icon(
                imageVector = if (item.isDirectory) Icons.Filled.Folder else Icons.AutoMirrored.Filled.InsertDriveFile,
                contentDescription = if (item.isDirectory) folderDescription else fileDescription,
            )
        },
    )
}

private fun formatSubtitle(item: FileItem): String {
    val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
    val date = dateFormat.format(Date(item.lastModifiedEpochMillis))
    return if (item.isDirectory) {
        date
    } else {
        "$date · ${formatFileSize(item.sizeBytes)}"
    }
}

private fun formatFileSize(bytes: Long): String {
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
