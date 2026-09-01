package dev.anmitali.nook.feature.browse

import dev.anmitali.nook.core.model.FileItem

sealed interface BrowseUiState {
    data object Loading : BrowseUiState

    data object PermissionRequired : BrowseUiState

    data class Content(
        val currentPath: String,
        val breadcrumbs: List<String>,
        val items: List<FileItem>,
    ) : BrowseUiState

    data class Error(val message: String) : BrowseUiState
}
