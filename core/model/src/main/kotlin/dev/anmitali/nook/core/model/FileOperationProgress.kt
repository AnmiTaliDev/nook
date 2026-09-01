package dev.anmitali.nook.core.model

data class FileOperationProgress(
    val completedItems: Int,
    val totalItems: Int,
    val currentItemName: String,
)
