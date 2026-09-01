package dev.anmitali.nook.core.model

sealed interface FileGroupLabel {
    data object None : FileGroupLabel
    data class Type(val fileType: FileType) : FileGroupLabel
    data class Date(val bucket: DateBucket) : FileGroupLabel
}

data class FileGroup(
    val label: FileGroupLabel,
    val items: List<FileItem>,
)
