package dev.anmitali.nook.core.model

data class ArchiveEntry(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val sizeBytes: Long,
)
