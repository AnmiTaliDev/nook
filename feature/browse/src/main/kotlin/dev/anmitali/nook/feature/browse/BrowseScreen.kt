package dev.anmitali.nook.feature.browse

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.anmitali.nook.core.model.FileItem
import dev.anmitali.nook.core.model.Volume
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BrowseScreen(
    onOpenFile: (FileItem) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BrowseViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedPaths by viewModel.selectedPaths.collectAsState()
    val clipboard by viewModel.clipboard.collectAsState()
    val dialog by viewModel.dialog.collectAsState()
    val operationProgress by viewModel.operationProgress.collectAsState()
    val pendingUndo by viewModel.pendingUndo.collectAsState()
    val volumes by viewModel.volumes.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddChooser by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    BackHandler(enabled = selectedPaths.isNotEmpty() || viewModel.canNavigateUp) {
        if (selectedPaths.isNotEmpty()) {
            viewModel.clearSelection()
        } else {
            viewModel.onNavigateUp()
        }
    }

    val undoMessage = stringResource(R.string.browse_undo_delete_message)
    val undoAction = stringResource(R.string.browse_undo_action)
    LaunchedEffect(pendingUndo) {
        if (pendingUndo != null) {
            val result = snackbarHostState.showSnackbar(undoMessage, actionLabel = undoAction)
            if (result == SnackbarResult.ActionPerformed) {
                viewModel.onUndoDelete()
            } else {
                viewModel.onDismissUndo()
            }
        }
    }

    ModalNavigationDrawer(
        modifier = modifier,
        drawerState = drawerState,
        drawerContent = {
            BrowseDrawerContent(
                volumes = volumes,
                onVolumeClick = { volume ->
                    viewModel.onDirectoryOpened(volume.rootPath)
                    coroutineScope.launch { drawerState.close() }
                },
            )
        },
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                if (selectedPaths.isNotEmpty()) {
                    SelectionTopBar(
                        selectedCount = selectedPaths.size,
                        onClear = viewModel::clearSelection,
                        onCopy = viewModel::onCopySelected,
                        onCut = viewModel::onCutSelected,
                        onDelete = viewModel::onRequestDelete,
                        onRename = {
                            val state = uiState as? BrowseUiState.Content ?: return@SelectionTopBar
                            val item = state.items.find { it.path == selectedPaths.first() } ?: return@SelectionTopBar
                            viewModel.onRequestRename(item)
                        },
                        canRename = selectedPaths.size == 1,
                    )
                } else {
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
                            } else {
                                IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                                    Icon(
                                        imageVector = Icons.Filled.Menu,
                                        contentDescription = stringResource(R.string.browse_open_menu),
                                    )
                                }
                            }
                        },
                    )
                }
            },
            floatingActionButton = {
                if (selectedPaths.isEmpty() && uiState is BrowseUiState.Content) {
                    ExtendedFloatingActionButton(
                        text = { Text(stringResource(R.string.browse_add_content_description)) },
                        icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                        onClick = { showAddChooser = true },
                    )
                }
            },
        ) { paddingValues ->
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                if (clipboard != null) {
                    PasteBar(
                        itemCount = clipboard!!.paths.size,
                        onPaste = viewModel::onPasteRequested,
                        onClear = viewModel::onClearClipboard,
                    )
                }
                Box(modifier = Modifier.fillMaxSize()) {
                    when (val state = uiState) {
                        is BrowseUiState.Loading -> LoadingContent()
                        is BrowseUiState.PermissionRequired -> PermissionRequiredContent(
                            onGrantAccess = { viewModel.onPermissionGranted() },
                        )
                        is BrowseUiState.Error -> ErrorContent(message = state.message)
                        is BrowseUiState.Content -> FileListContent(
                            items = state.items,
                            selectedPaths = selectedPaths,
                            onItemClick = { item ->
                                when {
                                    selectedPaths.isNotEmpty() -> viewModel.toggleSelection(item.path)
                                    item.isDirectory -> viewModel.onDirectoryOpened(item.path)
                                    else -> onOpenFile(item)
                                }
                            },
                            onItemLongClick = { item -> viewModel.toggleSelection(item.path) },
                        )
                    }
                }
            }
        }
    }

    if (showAddChooser) {
        AddEntryChooserDialog(
            onDismiss = { showAddChooser = false },
            onChooseFolder = {
                showAddChooser = false
                viewModel.onRequestCreateEntry(CreateEntryType.FOLDER)
            },
            onChooseFile = {
                showAddChooser = false
                viewModel.onRequestCreateEntry(CreateEntryType.FILE)
            },
        )
    }

    operationProgress?.let { progress ->
        OperationProgressDialog(progress = progress, onCancel = viewModel::onCancelOperation)
    }

    when (val currentDialog = dialog) {
        is BrowseDialog.CreateEntry -> CreateEntryDialog(
            type = currentDialog.type,
            onDismiss = viewModel::onDismissDialog,
            onConfirm = viewModel::onConfirmCreateEntry,
        )
        is BrowseDialog.Rename -> RenameDialog(
            currentName = currentDialog.item.name,
            onDismiss = viewModel::onDismissDialog,
            onConfirm = viewModel::onConfirmRename,
        )
        is BrowseDialog.DeleteConfirmation -> DeleteConfirmationDialog(
            itemCount = currentDialog.paths.size,
            onDismiss = viewModel::onDismissDialog,
            onConfirm = viewModel::onConfirmDelete,
        )
        is BrowseDialog.Conflict -> ConflictDialog(
            conflictingNames = currentDialog.conflictingNames,
            onDismiss = viewModel::onDismissDialog,
            onResolve = viewModel::onConflictResolved,
        )
        is BrowseDialog.OperationError -> OperationErrorDialog(
            message = currentDialog.message,
            onDismiss = viewModel::onDismissDialog,
        )
        null -> Unit
    }
}

@Composable
private fun BrowseDrawerContent(
    volumes: List<Volume>,
    onVolumeClick: (Volume) -> Unit,
) {
    ModalDrawerSheet {
        Text(
            text = stringResource(R.string.browse_drawer_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(16.dp),
        )
        volumes.forEach { volume ->
            NavigationDrawerItem(
                label = { Text(volume.label) },
                selected = false,
                icon = {
                    Icon(
                        imageVector = if (volume.isRemovable) Icons.Filled.SdStorage else Icons.Filled.Storage,
                        contentDescription = null,
                    )
                },
                badge = {
                    Text(
                        stringResource(
                            R.string.browse_volume_usage,
                            formatFileSize(volume.totalBytes - volume.availableBytes),
                            formatFileSize(volume.totalBytes),
                        ),
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
                onClick = { onVolumeClick(volume) },
                modifier = Modifier.padding(horizontal = 12.dp),
            )
        }
    }
}

@Composable
private fun SelectionTopBar(
    selectedCount: Int,
    onClear: () -> Unit,
    onCopy: () -> Unit,
    onCut: () -> Unit,
    onDelete: () -> Unit,
    onRename: () -> Unit,
    canRename: Boolean,
) {
    TopAppBar(
        title = { Text(stringResource(R.string.browse_selected_count, selectedCount)) },
        navigationIcon = {
            IconButton(onClick = onClear) {
                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.browse_clear_selection))
            }
        },
        actions = {
            if (canRename) {
                IconButton(onClick = onRename) {
                    Icon(Icons.Filled.DriveFileRenameOutline, contentDescription = stringResource(R.string.browse_action_rename))
                }
            }
            IconButton(onClick = onCopy) {
                Icon(Icons.Filled.ContentCopy, contentDescription = stringResource(R.string.browse_action_copy))
            }
            IconButton(onClick = onCut) {
                Icon(Icons.Filled.ContentCut, contentDescription = stringResource(R.string.browse_action_cut))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.browse_action_delete))
            }
        },
    )
}

@Composable
private fun PasteBar(itemCount: Int, onPaste: () -> Unit, onClear: () -> Unit) {
    Surface(tonalElevation = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onPaste) {
                Icon(Icons.Filled.ContentPaste, contentDescription = null)
                Text(stringResource(R.string.browse_action_paste, itemCount))
            }
            IconButton(onClick = onClear) {
                Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.browse_clear_selection))
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
    selectedPaths: Set<String>,
    onItemClick: (FileItem) -> Unit,
    onItemLongClick: (FileItem) -> Unit,
) {
    if (items.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = stringResource(R.string.browse_empty_directory))
        }
        return
    }

    LazyColumn {
        items(items, key = { it.path }) { item ->
            FileRow(
                item = item,
                isSelected = item.path in selectedPaths,
                onClick = { onItemClick(item) },
                onLongClick = { onItemLongClick(item) },
            )
        }
    }
}

@Composable
private fun FileRow(item: FileItem, isSelected: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) {
    val folderDescription = stringResource(R.string.browse_folder_content_description)
    val fileDescription = stringResource(R.string.browse_file_content_description)
    ListItem(
        modifier = Modifier
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .background(
                if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
            ),
        headlineContent = { Text(text = item.name) },
        supportingContent = { Text(text = formatSubtitle(item)) },
        leadingContent = {
            if (isSelected) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null)
            } else {
                Icon(
                    imageVector = if (item.isDirectory) Icons.Filled.Folder else Icons.AutoMirrored.Filled.InsertDriveFile,
                    contentDescription = if (item.isDirectory) folderDescription else fileDescription,
                )
            }
        },
        trailingContent = {
            if (isSelected) {
                Icon(Icons.Filled.Check, contentDescription = null)
            }
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
