package dev.anmitali.nook.core.domain

import dev.anmitali.nook.core.model.FileConflictPolicy
import dev.anmitali.nook.core.model.FileDetails
import dev.anmitali.nook.core.model.FileItem
import dev.anmitali.nook.core.model.FileOperationProgress
import dev.anmitali.nook.core.model.Volume
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeFileSource(
    private val directories: Map<String, List<FileItem>> = emptyMap(),
    private val volumes: List<Volume> = emptyList(),
    private val existingNames: Set<String> = emptySet(),
    private val searchResults: List<FileItem> = emptyList(),
    private val fileDetails: FileDetails? = null,
) : FileSource {

    var lastCopyCall: Triple<List<String>, String, FileConflictPolicy>? = null
        private set
    var lastMoveCall: Triple<List<String>, String, FileConflictPolicy>? = null
        private set
    var lastTrashedPaths: List<String>? = null
        private set
    var lastRestoredMapping: Map<String, String>? = null
        private set

    override fun listDirectory(path: String): Flow<List<FileItem>> =
        flowOf(directories[path].orEmpty())

    override fun listVolumes(): Flow<List<Volume>> = flowOf(volumes)

    override fun search(rootPath: String, query: String): Flow<List<FileItem>> = flowOf(searchResults)

    override suspend fun getFileDetails(path: String): FileDetails =
        fileDetails ?: error("No fake FileDetails configured")

    override suspend fun findConflicts(sourcePaths: List<String>, destinationDirectory: String): List<String> =
        sourcePaths.map { it.substringAfterLast('/') }.filter { it in existingNames }

    override fun copy(
        sourcePaths: List<String>,
        destinationDirectory: String,
        policy: FileConflictPolicy,
    ): Flow<FileOperationProgress> {
        lastCopyCall = Triple(sourcePaths, destinationDirectory, policy)
        return flowOf(FileOperationProgress(sourcePaths.size, sourcePaths.size, ""))
    }

    override fun move(
        sourcePaths: List<String>,
        destinationDirectory: String,
        policy: FileConflictPolicy,
    ): Flow<FileOperationProgress> {
        lastMoveCall = Triple(sourcePaths, destinationDirectory, policy)
        return flowOf(FileOperationProgress(sourcePaths.size, sourcePaths.size, ""))
    }

    override suspend fun moveToTrash(paths: List<String>): Result<Map<String, String>> {
        lastTrashedPaths = paths
        return Result.success(paths.associateWith { "/trash/${it.substringAfterLast('/')}" })
    }

    override suspend fun restoreFromTrash(trashMapping: Map<String, String>): Result<Unit> {
        lastRestoredMapping = trashMapping
        return Result.success(Unit)
    }

    override suspend fun rename(path: String, newName: String): Result<String> =
        Result.success(path.substringBeforeLast('/') + "/" + newName)

    override suspend fun createDirectory(parentPath: String, name: String): Result<Unit> = Result.success(Unit)

    override suspend fun createFile(parentPath: String, name: String): Result<Unit> = Result.success(Unit)
}
