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
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import dev.anmitali.nook.core.model.Bookmark
import dev.anmitali.nook.core.model.DateBucket
import dev.anmitali.nook.core.model.FileDetails
import dev.anmitali.nook.core.model.FileGroup
import dev.anmitali.nook.core.model.FileGroupLabel
import dev.anmitali.nook.core.model.FileItem
import dev.anmitali.nook.core.model.FileType
import dev.anmitali.nook.core.model.GroupBy
import dev.anmitali.nook.core.model.SortBy
import dev.anmitali.nook.core.model.SortOrder
import dev.anmitali.nook.core.model.Volume
import java.text.DateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BrowseScreen(
    onOpenFile: (FileItem) -> Unit,
    onOpenArchive: (String) -> Unit,
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
    val sortOrder by viewModel.sortOrder.collectAsState()
    val groupBy by viewModel.groupBy.collectAsState()
    val showHiddenFiles by viewModel.showHiddenFiles.collectAsState()
    val isSearchActive by viewModel.isSearchActive.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()
    val fileDetails by viewModel.fileDetails.collectAsState()
    val isLoadingDetails by viewModel.isLoadingDetails.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddChooser by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val lowStorageVolume = (uiState as? BrowseUiState.Content)?.currentPath?.let { path ->
        volumes.filter { path.startsWith(it.rootPath) }.maxByOrNull { it.rootPath.length }
    }?.takeIf { it.totalBytes > 0 && it.availableBytes.toDouble() / it.totalBytes < LOW_STORAGE_THRESHOLD }

    BackHandler(enabled = isSearchActive || selectedPaths.isNotEmpty() || viewModel.canNavigateUp) {
        when {
            isSearchActive -> viewModel.onSearchDismissed()
            selectedPaths.isNotEmpty() -> viewModel.clearSelection()
            else -> viewModel.onNavigateUp()
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
                bookmarks = bookmarks,
                onVolumeClick = { volume ->
                    viewModel.onDirectoryOpened(volume.rootPath)
                    coroutineScope.launch { drawerState.close() }
                },
                onBookmarkClick = { bookmark ->
                    viewModel.onDirectoryOpened(bookmark.path)
                    coroutineScope.launch { drawerState.close() }
                },
                onBookmarkRemove = { bookmark -> viewModel.onRemoveBookmark(bookmark.path) },
            )
        },
    ) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                when {
                    isSearchActive -> SearchTopBar(
                        query = searchQuery,
                        onQueryChange = viewModel::onSearchQueryChanged,
                        onClose = viewModel::onSearchDismissed,
                    )
                    selectedPaths.isNotEmpty() -> SelectionTopBar(
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
                        onInfo = {
                            val state = uiState as? BrowseUiState.Content ?: return@SelectionTopBar
                            val item = state.items.find { it.path == selectedPaths.first() } ?: return@SelectionTopBar
                            viewModel.onRequestInfo(item)
                        },
                        onCompress = viewModel::onRequestCompress,
                        onExtract = {
                            val state = uiState as? BrowseUiState.Content ?: return@SelectionTopBar
                            val item = state.items.find { it.path == selectedPaths.first() } ?: return@SelectionTopBar
                            viewModel.onRequestExtract(item)
                        },
                        canRename = selectedPaths.size == 1,
                        canExtract = selectedPaths.size == 1 &&
                            (uiState as? BrowseUiState.Content)?.items
                                ?.find { it.path == selectedPaths.first() }
                                ?.let { isZipFile(it.name) } == true,
                    )
                    else -> TopAppBar(
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
                        actions = {
                            val contentState = uiState as? BrowseUiState.Content
                            if (contentState != null) {
                                val isBookmarked = bookmarks.any { it.path == contentState.currentPath }
                                IconButton(onClick = viewModel::onToggleBookmark) {
                                    Icon(
                                        imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                        contentDescription = stringResource(
                                            if (isBookmarked) R.string.browse_bookmark_remove else R.string.browse_bookmark_add,
                                        ),
                                    )
                                }
                            }
                            IconButton(onClick = viewModel::onSearchActivated) {
                                Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.browse_search_action))
                            }
                            Box {
                                IconButton(onClick = { showSortMenu = true }) {
                                    Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = stringResource(R.string.browse_sort_action))
                                }
                                SortMenu(
                                    expanded = showSortMenu,
                                    onDismiss = { showSortMenu = false },
                                    sortOrder = sortOrder,
                                    groupBy = groupBy,
                                    showHiddenFiles = showHiddenFiles,
                                    onSortByChanged = viewModel::onSortByChanged,
                                    onDirectionToggle = viewModel::onSortDirectionToggled,
                                    onGroupByChanged = viewModel::onGroupByChanged,
                                    onToggleShowHiddenFiles = viewModel::onToggleShowHiddenFiles,
                                )
                            }
                        },
                    )
                }
            },
            floatingActionButton = {
                if (!isSearchActive && selectedPaths.isEmpty() && uiState is BrowseUiState.Content) {
                    ExtendedFloatingActionButton(
                        text = { Text(stringResource(R.string.browse_add_content_description)) },
                        icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                        onClick = { showAddChooser = true },
                    )
                }
            },
        ) { paddingValues ->
            Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                if (!isSearchActive && lowStorageVolume != null) {
                    LowStorageBanner(volume = lowStorageVolume)
                }
                if (!isSearchActive && clipboard != null) {
                    PasteBar(
                        itemCount = clipboard!!.paths.size,
                        onPaste = viewModel::onPasteRequested,
                        onClear = viewModel::onClearClipboard,
                    )
                }
                Box(modifier = Modifier.fillMaxSize()) {
                    if (isSearchActive) {
                        SearchResultsContent(
                            results = searchResults,
                            isSearching = isSearching,
                            onItemClick = { item ->
                                when {
                                    item.isDirectory -> {
                                        viewModel.onSearchDismissed()
                                        viewModel.onDirectoryOpened(item.path)
                                    }
                                    isArchiveFile(item.name) -> onOpenArchive(item.path)
                                    else -> onOpenFile(item)
                                }
                            },
                        )
                    } else {
                        when (val state = uiState) {
                            is BrowseUiState.Loading -> LoadingContent()
                            is BrowseUiState.PermissionRequired -> PermissionRequiredContent(
                                onGrantAccess = { viewModel.onPermissionGranted() },
                            )
                            is BrowseUiState.Error -> ErrorContent(message = state.message)
                            is BrowseUiState.Content -> FileListContent(
                                groups = state.groups,
                                selectedPaths = selectedPaths,
                                onItemClick = { item ->
                                    when {
                                        selectedPaths.isNotEmpty() -> viewModel.toggleSelection(item.path)
                                        item.isDirectory -> viewModel.onDirectoryOpened(item.path)
                                        isArchiveFile(item.name) -> onOpenArchive(item.path)
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
        is BrowseDialog.Info -> InfoDialog(
            itemName = currentDialog.item.name,
            details = fileDetails,
            isLoading = isLoadingDetails,
            onDismiss = viewModel::onDismissDialog,
        )
        is BrowseDialog.CreateArchive -> CreateArchiveDialog(
            onDismiss = viewModel::onDismissDialog,
            onConfirm = viewModel::onConfirmCompress,
        )
        is BrowseDialog.PasswordPrompt -> PasswordPromptDialog(
            errorMessage = currentDialog.errorMessage,
            onDismiss = viewModel::onDismissDialog,
            onConfirm = viewModel::onConfirmPassword,
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
    bookmarks: List<Bookmark>,
    onVolumeClick: (Volume) -> Unit,
    onBookmarkClick: (Bookmark) -> Unit,
    onBookmarkRemove: (Bookmark) -> Unit,
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
        if (bookmarks.isNotEmpty()) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text(
                text = stringResource(R.string.browse_bookmarks_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            bookmarks.forEach { bookmark ->
                NavigationDrawerItem(
                    label = { Text(bookmark.label) },
                    selected = false,
                    icon = { Icon(Icons.Filled.Bookmark, contentDescription = null) },
                    badge = {
                        IconButton(onClick = { onBookmarkRemove(bookmark) }) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.browse_bookmark_remove),
                            )
                        }
                    },
                    onClick = { onBookmarkClick(bookmark) },
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
) {
    TopAppBar(
        title = {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text(stringResource(R.string.browse_search_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.browse_search_close),
                )
            }
        },
    )
}

@Composable
private fun SortMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    sortOrder: SortOrder,
    groupBy: GroupBy,
    showHiddenFiles: Boolean,
    onSortByChanged: (SortBy) -> Unit,
    onDirectionToggle: () -> Unit,
    onGroupByChanged: (GroupBy) -> Unit,
    onToggleShowHiddenFiles: () -> Unit,
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        listOf(
            SortBy.NAME to R.string.browse_sort_by_name,
            SortBy.DATE_MODIFIED to R.string.browse_sort_by_date,
            SortBy.SIZE to R.string.browse_sort_by_size,
            SortBy.TYPE to R.string.browse_sort_by_type,
        ).forEach { (sortBy, labelRes) ->
            DropdownMenuItem(
                text = { Text(stringResource(labelRes)) },
                leadingIcon = { RadioButton(selected = sortOrder.sortBy == sortBy, onClick = null) },
                onClick = { onSortByChanged(sortBy) },
            )
        }
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text(stringResource(R.string.browse_sort_direction_toggle)) },
            onClick = onDirectionToggle,
        )
        HorizontalDivider()
        listOf(
            GroupBy.NONE to R.string.browse_group_none,
            GroupBy.TYPE to R.string.browse_group_by_type,
            GroupBy.DATE to R.string.browse_group_by_date,
        ).forEach { (group, labelRes) ->
            DropdownMenuItem(
                text = { Text(stringResource(labelRes)) },
                leadingIcon = { RadioButton(selected = groupBy == group, onClick = null) },
                onClick = { onGroupByChanged(group) },
            )
        }
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text(stringResource(R.string.browse_show_hidden_files)) },
            leadingIcon = {
                Checkbox(checked = showHiddenFiles, onCheckedChange = null)
            },
            onClick = onToggleShowHiddenFiles,
        )
    }
}

@Composable
private fun SearchResultsContent(
    results: List<FileItem>,
    isSearching: Boolean,
    onItemClick: (FileItem) -> Unit,
) {
    if (results.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (isSearching) {
                CircularProgressIndicator()
            } else {
                Text(stringResource(R.string.browse_search_no_results))
            }
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (isSearching) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
        LazyColumn {
            items(results, key = { it.path }) { item ->
                FileRow(item = item, isSelected = false, onClick = { onItemClick(item) }, onLongClick = {})
            }
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
    onInfo: () -> Unit,
    onCompress: () -> Unit,
    onExtract: () -> Unit,
    canRename: Boolean,
    canExtract: Boolean,
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
                IconButton(onClick = onInfo) {
                    Icon(Icons.Filled.Info, contentDescription = stringResource(R.string.browse_action_info))
                }
                IconButton(onClick = onRename) {
                    Icon(Icons.Filled.DriveFileRenameOutline, contentDescription = stringResource(R.string.browse_action_rename))
                }
            }
            if (canExtract) {
                IconButton(onClick = onExtract) {
                    Icon(Icons.Filled.FolderZip, contentDescription = stringResource(R.string.browse_action_extract))
                }
            }
            IconButton(onClick = onCompress) {
                Icon(Icons.Filled.Archive, contentDescription = stringResource(R.string.browse_action_compress))
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

private const val LOW_STORAGE_THRESHOLD = 0.1

@Composable
private fun LowStorageBanner(volume: Volume) {
    Surface(color = MaterialTheme.colorScheme.errorContainer) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
            )
            Text(
                text = stringResource(
                    R.string.browse_low_storage_warning,
                    volume.label,
                    formatFileSize(volume.availableBytes),
                ),
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
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
    groups: List<FileGroup>,
    selectedPaths: Set<String>,
    onItemClick: (FileItem) -> Unit,
    onItemLongClick: (FileItem) -> Unit,
) {
    if (groups.all { it.items.isEmpty() }) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = stringResource(R.string.browse_empty_directory))
        }
        return
    }

    LazyColumn {
        groups.forEach { group ->
            if (group.label != FileGroupLabel.None) {
                item(key = "header-${group.label}") {
                    Text(
                        text = groupLabelText(group.label),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }
            items(group.items, key = { it.path }) { item ->
                FileRow(
                    item = item,
                    isSelected = item.path in selectedPaths,
                    onClick = { onItemClick(item) },
                    onLongClick = { onItemLongClick(item) },
                )
            }
        }
    }
}

@Composable
private fun groupLabelText(label: FileGroupLabel): String = when (label) {
    FileGroupLabel.None -> ""
    is FileGroupLabel.Type -> stringResource(fileTypeLabelRes(label.fileType))
    is FileGroupLabel.Date -> stringResource(dateBucketLabelRes(label.bucket))
}

private fun fileTypeLabelRes(type: FileType): Int = when (type) {
    FileType.DIRECTORY -> R.string.browse_group_type_directory
    FileType.IMAGE -> R.string.browse_group_type_image
    FileType.VIDEO -> R.string.browse_group_type_video
    FileType.AUDIO -> R.string.browse_group_type_audio
    FileType.DOCUMENT -> R.string.browse_group_type_document
    FileType.ARCHIVE -> R.string.browse_group_type_archive
    FileType.APK -> R.string.browse_group_type_apk
    FileType.TEXT -> R.string.browse_group_type_text
    FileType.OTHER -> R.string.browse_group_type_other
}

private fun dateBucketLabelRes(bucket: DateBucket): Int = when (bucket) {
    DateBucket.TODAY -> R.string.browse_group_date_today
    DateBucket.YESTERDAY -> R.string.browse_group_date_yesterday
    DateBucket.THIS_WEEK -> R.string.browse_group_date_this_week
    DateBucket.OLDER -> R.string.browse_group_date_older
}

@Composable
private fun FileRow(item: FileItem, isSelected: Boolean, onClick: () -> Unit, onLongClick: () -> Unit) {
    val folderDescription = stringResource(R.string.browse_folder_content_description)
    val fileDescription = stringResource(R.string.browse_file_content_description)
    ListItem(
        modifier = Modifier
            .semantics { selected = isSelected }
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

internal fun formatFileSize(bytes: Long): String {
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
