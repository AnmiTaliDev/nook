package dev.anmitali.nook.core.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface NookRoute : NavKey

@Serializable
data class BrowseRoute(val directoryPath: String? = null) : NookRoute

@Serializable
data class ArchiveRoute(val archivePath: String) : NookRoute
