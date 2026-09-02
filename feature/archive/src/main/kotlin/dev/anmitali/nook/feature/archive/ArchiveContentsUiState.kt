package dev.anmitali.nook.feature.archive

import dev.anmitali.nook.core.model.ArchiveEntry

sealed interface ArchiveContentsUiState {
    data object Loading : ArchiveContentsUiState

    data class Content(val entries: List<ArchiveEntry>) : ArchiveContentsUiState

    data class Error(val message: String) : ArchiveContentsUiState
}
