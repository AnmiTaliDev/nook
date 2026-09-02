package dev.anmitali.nook.feature.browse

import dev.anmitali.nook.core.model.FileItem

enum class ClipboardOperation { COPY, CUT }

data class FileClipboard(
    val paths: List<String>,
    val operation: ClipboardOperation,
)

enum class CreateEntryType { FOLDER, FILE }

data class PendingTransfer(
    val sourcePaths: List<String>,
    val destinationDirectory: String,
    val operation: ClipboardOperation,
)

data class UndoableDelete(val trashMapping: Map<String, String>)

sealed interface BrowseDialog {
    data class Conflict(val conflictingNames: List<String>, val pending: PendingTransfer) : BrowseDialog
    data class CreateEntry(val type: CreateEntryType) : BrowseDialog
    data class Rename(val item: FileItem) : BrowseDialog
    data class DeleteConfirmation(val paths: List<String>) : BrowseDialog
    data class Info(val item: FileItem) : BrowseDialog
    data class CreateArchive(val sourcePaths: List<String>) : BrowseDialog
    data class PasswordPrompt(val archivePath: String, val errorMessage: String? = null) : BrowseDialog
    data class OperationError(val message: String) : BrowseDialog
}

private val ARCHIVE_EXTENSIONS = setOf("zip", "tar", "gz", "tgz", "7z")

fun isArchiveFile(name: String): Boolean {
    val lower = name.lowercase()
    return ARCHIVE_EXTENSIONS.any { lower.endsWith(".$it") }
}

fun isZipFile(name: String): Boolean = name.lowercase().endsWith(".zip")
