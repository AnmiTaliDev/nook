package dev.anmitali.nook.feature.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.anmitali.nook.core.domain.ListArchiveEntriesUseCase
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ArchiveContentsViewModel @Inject constructor(
    private val listArchiveEntriesUseCase: ListArchiveEntriesUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<ArchiveContentsUiState>(ArchiveContentsUiState.Loading)
    val uiState: StateFlow<ArchiveContentsUiState> = _uiState.asStateFlow()

    private var loadedPath: String? = null

    fun load(archivePath: String) {
        if (loadedPath == archivePath) return
        loadedPath = archivePath
        viewModelScope.launch {
            _uiState.value = ArchiveContentsUiState.Loading
            listArchiveEntriesUseCase(archivePath)
                .onSuccess { entries -> _uiState.value = ArchiveContentsUiState.Content(entries) }
                .onFailure { throwable ->
                    _uiState.value = ArchiveContentsUiState.Error(throwable.message ?: "Unknown error")
                }
        }
    }
}
