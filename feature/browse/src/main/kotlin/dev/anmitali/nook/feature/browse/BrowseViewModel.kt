package dev.anmitali.nook.feature.browse

import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.anmitali.nook.core.domain.ListFilesUseCase
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

@HiltViewModel
class BrowseViewModel @Inject constructor(
    private val listFilesUseCase: ListFilesUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<BrowseUiState>(BrowseUiState.Loading)
    val uiState: StateFlow<BrowseUiState> = _uiState.asStateFlow()

    private var currentPath: String = Environment.getExternalStorageDirectory().absolutePath

    init {
        checkPermissionAndLoad(currentPath)
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

    private fun checkPermissionAndLoad(path: String) {
        if (!Environment.isExternalStorageManager()) {
            _uiState.value = BrowseUiState.PermissionRequired
            return
        }
        currentPath = path
        _uiState.value = BrowseUiState.Loading
        listFilesUseCase(path)
            .onEach { items ->
                _uiState.value = BrowseUiState.Content(
                    currentPath = path,
                    breadcrumbs = path.split(File.separatorChar).filter { it.isNotBlank() },
                    items = items,
                )
            }
            .catch { throwable ->
                _uiState.value = BrowseUiState.Error(throwable.message ?: "Unknown error")
            }
            .launchIn(viewModelScope)
    }
}
