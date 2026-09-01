package dev.anmitali.nook.feature.browse

import dev.anmitali.nook.core.model.FileGroup

sealed interface BrowseUiState {
    data object Loading : BrowseUiState

    data object PermissionRequired : BrowseUiState

    data class Content(
        val currentPath: String,
        val breadcrumbs: List<String>,
        val groups: List<FileGroup>,
    ) : BrowseUiState {
        val items get() = groups.flatMap { it.items }
    }

    data class Error(val message: String) : BrowseUiState
}
