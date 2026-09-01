package dev.anmitali.nook.feature.browse

import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.anmitali.nook.core.domain.CopyFilesUseCase
import dev.anmitali.nook.core.domain.CreateDirectoryUseCase
import dev.anmitali.nook.core.domain.CreateFileUseCase
import dev.anmitali.nook.core.domain.DeleteFilesUseCase
import dev.anmitali.nook.core.domain.FindFileConflictsUseCase
import dev.anmitali.nook.core.domain.GetVolumesUseCase
import dev.anmitali.nook.core.domain.GroupFilesUseCase
import dev.anmitali.nook.core.domain.ListFilesUseCase
import dev.anmitali.nook.core.domain.MoveFilesUseCase
import dev.anmitali.nook.core.domain.RenameFileUseCase
import dev.anmitali.nook.core.domain.RestoreFromTrashUseCase
import dev.anmitali.nook.core.domain.SearchFilesUseCase
import dev.anmitali.nook.core.domain.SortFilesUseCase
import dev.anmitali.nook.core.model.FileConflictPolicy
import dev.anmitali.nook.core.model.FileItem
import dev.anmitali.nook.core.model.FileOperationProgress
import dev.anmitali.nook.core.model.GroupBy
import dev.anmitali.nook.core.model.SortBy
import dev.anmitali.nook.core.model.SortDirection
import dev.anmitali.nook.core.model.SortOrder
import dev.anmitali.nook.core.model.Volume
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class BrowseViewModel @Inject constructor(
    private val listFilesUseCase: ListFilesUseCase,
    private val getVolumesUseCase: GetVolumesUseCase,
    private val sortFilesUseCase: SortFilesUseCase,
    private val groupFilesUseCase: GroupFilesUseCase,
    private val searchFilesUseCase: SearchFilesUseCase,
    private val findFileConflictsUseCase: FindFileConflictsUseCase,
    private val copyFilesUseCase: CopyFilesUseCase,
    private val moveFilesUseCase: MoveFilesUseCase,
    private val deleteFilesUseCase: DeleteFilesUseCase,
    private val restoreFromTrashUseCase: RestoreFromTrashUseCase,
    private val renameFileUseCase: RenameFileUseCase,
    private val createDirectoryUseCase: CreateDirectoryUseCase,
    private val createFileUseCase: CreateFileUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<BrowseUiState>(BrowseUiState.Loading)
    val uiState: StateFlow<BrowseUiState> = _uiState.asStateFlow()

    private val _selectedPaths = MutableStateFlow<Set<String>>(emptySet())
    val selectedPaths: StateFlow<Set<String>> = _selectedPaths.asStateFlow()

    private val _clipboard = MutableStateFlow<FileClipboard?>(null)
    val clipboard: StateFlow<FileClipboard?> = _clipboard.asStateFlow()

    private val _dialog = MutableStateFlow<BrowseDialog?>(null)
    val dialog: StateFlow<BrowseDialog?> = _dialog.asStateFlow()

    private val _operationProgress = MutableStateFlow<FileOperationProgress?>(null)
    val operationProgress: StateFlow<FileOperationProgress?> = _operationProgress.asStateFlow()

    private val _pendingUndo = MutableStateFlow<UndoableDelete?>(null)
    val pendingUndo: StateFlow<UndoableDelete?> = _pendingUndo.asStateFlow()

    private val _volumes = MutableStateFlow<List<Volume>>(emptyList())
    val volumes: StateFlow<List<Volume>> = _volumes.asStateFlow()

    private val _sortOrder = MutableStateFlow(SortOrder())
    val sortOrder: StateFlow<SortOrder> = _sortOrder.asStateFlow()

    private val _groupBy = MutableStateFlow(GroupBy.NONE)
    val groupBy: StateFlow<GroupBy> = _groupBy.asStateFlow()

    private val _isSearchActive = MutableStateFlow(false)
    val isSearchActive: StateFlow<Boolean> = _isSearchActive.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<FileItem>>(emptyList())
    val searchResults: StateFlow<List<FileItem>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private var currentPath: String = Environment.getExternalStorageDirectory().absolutePath
    private var rawItems: List<FileItem> = emptyList()
    private var operationJob: Job? = null
    private var searchJob: Job? = null

    init {
        checkPermissionAndLoad(currentPath)
        getVolumesUseCase()
            .onEach { _volumes.value = it }
            .launchIn(viewModelScope)
    }

    fun onDirectoryOpened(path: String) {
        checkPermissionAndLoad(path)
    }

    fun onNavigateUp(): Boolean {
        val parent = File(currentPath).parentFile
        if (parent == null || !currentPath.startsWith(Environment.getExternalStorageDirectory().absolutePath)) {
            return false
        }
        checkPermissionAndLoad(parent.absolutePath)
        return true
    }

    fun onPermissionGranted() {
        checkPermissionAndLoad(currentPath)
    }

    val canNavigateUp: Boolean
        get() = currentPath != Environment.getExternalStorageDirectory().absolutePath

    fun toggleSelection(path: String) {
        _selectedPaths.update { current -> if (path in current) current - path else current + path }
    }

    fun clearSelection() {
        _selectedPaths.value = emptySet()
    }

    fun onSortByChanged(sortBy: SortBy) {
        _sortOrder.update { it.copy(sortBy = sortBy) }
        applySortAndGroup()
    }

    fun onSortDirectionToggled() {
        _sortOrder.update {
            it.copy(direction = if (it.direction == SortDirection.ASCENDING) SortDirection.DESCENDING else SortDirection.ASCENDING)
        }
        applySortAndGroup()
    }

    fun onGroupByChanged(groupBy: GroupBy) {
        _groupBy.value = groupBy
        applySortAndGroup()
    }

    fun onSearchActivated() {
        _isSearchActive.value = true
    }

    fun onSearchDismissed() {
        _isSearchActive.value = false
        _searchQuery.value = ""
        _searchResults.value = emptyList()
        _isSearching.value = false
        searchJob?.cancel()
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        searchJob?.cancel()
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            _isSearching.value = false
            return
        }
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_MILLIS)
            _isSearching.value = true
            searchFilesUseCase(currentPath, query)
                .onEach { results -> _searchResults.value = results }
                .onCompletion { _isSearching.value = false }
                .catch { _isSearching.value = false }
                .launchIn(this)
        }
    }

    fun onCancelSearch() {
        searchJob?.cancel()
        _isSearching.value = false
    }

    fun onCopySelected() {
        _clipboard.value = FileClipboard(_selectedPaths.value.toList(), ClipboardOperation.COPY)
        clearSelection()
    }

    fun onCutSelected() {
        _clipboard.value = FileClipboard(_selectedPaths.value.toList(), ClipboardOperation.CUT)
        clearSelection()
    }

    fun onClearClipboard() {
        _clipboard.value = null
    }

    fun onPasteRequested() {
        val clip = _clipboard.value ?: return
        val destination = currentPath
        viewModelScope.launch {
            val conflicts = findFileConflictsUseCase(clip.paths, destination)
            val pending = PendingTransfer(clip.paths, destination, clip.operation)
            if (conflicts.isEmpty()) {
                performTransfer(pending, FileConflictPolicy.SKIP)
            } else {
                _dialog.value = BrowseDialog.Conflict(conflicts, pending)
            }
        }
    }

    fun onConflictResolved(policy: FileConflictPolicy) {
        val conflict = _dialog.value as? BrowseDialog.Conflict ?: return
        _dialog.value = null
        performTransfer(conflict.pending, policy)
    }

    private fun performTransfer(pending: PendingTransfer, policy: FileConflictPolicy) {
        operationJob?.cancel()
        operationJob = viewModelScope.launch {
            val progressFlow = when (pending.operation) {
                ClipboardOperation.COPY ->
                    copyFilesUseCase(pending.sourcePaths, pending.destinationDirectory, policy)
                ClipboardOperation.CUT ->
                    moveFilesUseCase(pending.sourcePaths, pending.destinationDirectory, policy)
            }
            progressFlow
                .onEach { progress -> _operationProgress.value = progress }
                .onCompletion {
                    _operationProgress.value = null
                    if (pending.operation == ClipboardOperation.CUT) {
                        _clipboard.value = null
                    }
                    refresh()
                }
                .catch { throwable ->
                    _operationProgress.value = null
                    _dialog.value = BrowseDialog.OperationError(throwable.message ?: "Operation failed")
                }
                .launchIn(this)
        }
    }

    fun onCancelOperation() {
        operationJob?.cancel()
        operationJob = null
        _operationProgress.value = null
    }

    fun onRequestDelete() {
        val paths = _selectedPaths.value.toList()
        if (paths.isEmpty()) return
        _dialog.value = BrowseDialog.DeleteConfirmation(paths)
    }

    fun onConfirmDelete() {
        val confirmation = _dialog.value as? BrowseDialog.DeleteConfirmation ?: return
        _dialog.value = null
        viewModelScope.launch {
            deleteFilesUseCase(confirmation.paths)
                .onSuccess { mapping ->
                    _pendingUndo.value = UndoableDelete(mapping)
                    clearSelection()
                    refresh()
                }
                .onFailure { throwable ->
                    _dialog.value = BrowseDialog.OperationError(throwable.message ?: "Delete failed")
                }
        }
    }

    fun onUndoDelete() {
        val undo = _pendingUndo.value ?: return
        _pendingUndo.value = null
        viewModelScope.launch {
            restoreFromTrashUseCase(undo.trashMapping)
            refresh()
        }
    }

    fun onDismissUndo() {
        _pendingUndo.value = null
    }

    fun onRequestRename(item: FileItem) {
        _dialog.value = BrowseDialog.Rename(item)
    }

    fun onConfirmRename(newName: String) {
        val rename = _dialog.value as? BrowseDialog.Rename ?: return
        _dialog.value = null
        viewModelScope.launch {
            renameFileUseCase(rename.item.path, newName)
                .onSuccess {
                    clearSelection()
                    refresh()
                }
                .onFailure { throwable ->
                    _dialog.value = BrowseDialog.OperationError(throwable.message ?: "Rename failed")
                }
        }
    }

    fun onRequestCreateEntry(type: CreateEntryType) {
        _dialog.value = BrowseDialog.CreateEntry(type)
    }

    fun onConfirmCreateEntry(name: String) {
        val createEntry = _dialog.value as? BrowseDialog.CreateEntry ?: return
        _dialog.value = null
        viewModelScope.launch {
            val result = when (createEntry.type) {
                CreateEntryType.FOLDER -> createDirectoryUseCase(currentPath, name)
                CreateEntryType.FILE -> createFileUseCase(currentPath, name)
            }
            result
                .onSuccess { refresh() }
                .onFailure { throwable ->
                    _dialog.value = BrowseDialog.OperationError(throwable.message ?: "Could not create \"$name\"")
                }
        }
    }

    fun onDismissDialog() {
        _dialog.value = null
    }

    private fun refresh() = checkPermissionAndLoad(currentPath)

    private fun applySortAndGroup() {
        val state = _uiState.value as? BrowseUiState.Content ?: return
        val sorted = sortFilesUseCase(rawItems, _sortOrder.value)
        _uiState.value = state.copy(groups = groupFilesUseCase(sorted, _groupBy.value))
    }

    private fun checkPermissionAndLoad(path: String) {
        if (!Environment.isExternalStorageManager()) {
            _uiState.value = BrowseUiState.PermissionRequired
            return
        }
        currentPath = path
        _selectedPaths.value = emptySet()
        _uiState.value = BrowseUiState.Loading
        listFilesUseCase(path)
            .onEach { items ->
                rawItems = items
                val sorted = sortFilesUseCase(items, _sortOrder.value)
                _uiState.value = BrowseUiState.Content(
                    currentPath = path,
                    breadcrumbs = path.split(File.separatorChar).filter { it.isNotBlank() },
                    groups = groupFilesUseCase(sorted, _groupBy.value),
                )
            }
            .catch { throwable ->
                _uiState.value = BrowseUiState.Error(throwable.message ?: "Unknown error")
            }
            .launchIn(viewModelScope)
    }
}

private const val SEARCH_DEBOUNCE_MILLIS = 300L
