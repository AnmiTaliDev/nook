package dev.anmitali.nook.core.model

data class FileItem(
    val name: String,
    val path: String,
    val type: FileType,
    val isDirectory: Boolean,
    val sizeBytes: Long,
    val lastModifiedEpochMillis: Long,
    val isHidden: Boolean,
    val canRead: Boolean,
    val canWrite: Boolean,
)
