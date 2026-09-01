package dev.anmitali.nook.core.domain

import dev.anmitali.nook.core.model.FileItem
import dev.anmitali.nook.core.model.Volume
import kotlinx.coroutines.flow.Flow

interface FileSource {
    fun listDirectory(path: String): Flow<List<FileItem>>

    fun listVolumes(): Flow<List<Volume>>
}
