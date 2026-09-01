package dev.anmitali.nook.core.domain

import dev.anmitali.nook.core.model.FileItem
import dev.anmitali.nook.core.model.Volume
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeFileSource(
    private val directories: Map<String, List<FileItem>> = emptyMap(),
    private val volumes: List<Volume> = emptyList(),
) : FileSource {

    override fun listDirectory(path: String): Flow<List<FileItem>> =
        flowOf(directories[path].orEmpty())

    override fun listVolumes(): Flow<List<Volume>> = flowOf(volumes)
}
