package dev.anmitali.nook.core.domain

import dev.anmitali.nook.core.model.ArchiveEntry
import dev.anmitali.nook.core.model.FileOperationProgress
import kotlinx.coroutines.flow.Flow

class WrongPasswordException(archivePath: String) : Exception("Wrong password for \"$archivePath\"")

interface ArchiveRepository {
    fun createZipArchive(sourcePaths: List<String>, destinationZipPath: String): Flow<FileOperationProgress>

    fun extractZipArchive(
        zipPath: String,
        destinationDirectory: String,
        password: String?,
    ): Flow<FileOperationProgress>

    suspend fun isPasswordProtected(zipPath: String): Boolean

    suspend fun listArchiveEntries(archivePath: String): Result<List<ArchiveEntry>>
}
