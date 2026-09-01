package dev.anmitali.nook.core.model

data class Volume(
    val id: String,
    val label: String,
    val rootPath: String,
    val isPrimary: Boolean,
    val isRemovable: Boolean,
    val totalBytes: Long,
    val availableBytes: Long,
)
