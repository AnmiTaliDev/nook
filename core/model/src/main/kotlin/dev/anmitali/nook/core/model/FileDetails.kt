package dev.anmitali.nook.core.model

data class FileDetails(
    val path: String,
    val name: String,
    val isDirectory: Boolean,
    val sizeBytes: Long,
    val itemCount: Int?,
    val createdEpochMillis: Long?,
    val modifiedEpochMillis: Long,
    val canRead: Boolean,
    val canWrite: Boolean,
    val canExecute: Boolean,
    val owner: String?,
    val mimeType: String?,
)
