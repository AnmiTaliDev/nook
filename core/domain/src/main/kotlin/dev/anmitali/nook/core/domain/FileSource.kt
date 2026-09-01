package dev.anmitali.nook.core.domain

import dev.anmitali.nook.core.model.FileConflictPolicy
import dev.anmitali.nook.core.model.FileItem
import dev.anmitali.nook.core.model.FileOperationProgress
import dev.anmitali.nook.core.model.Volume
import kotlinx.coroutines.flow.Flow

interface FileSource {
    fun listDirectory(path: String): Flow<List<FileItem>>

    fun listVolumes(): Flow<List<Volume>>

    suspend fun findConflicts(sourcePaths: List<String>, destinationDirectory: String): List<String>

    fun copy(
        sourcePaths: List<String>,
        destinationDirectory: String,
        policy: FileConflictPolicy,
    ): Flow<FileOperationProgress>

    fun move(
        sourcePaths: List<String>,
        destinationDirectory: String,
        policy: FileConflictPolicy,
    ): Flow<FileOperationProgress>

    suspend fun moveToTrash(paths: List<String>): Result<Map<String, String>>

    suspend fun restoreFromTrash(trashMapping: Map<String, String>): Result<Unit>

    suspend fun rename(path: String, newName: String): Result<String>

    suspend fun createDirectory(parentPath: String, name: String): Result<Unit>

    suspend fun createFile(parentPath: String, name: String): Result<Unit>
}
