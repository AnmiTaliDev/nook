package dev.anmitali.nook.core.domain

import dev.anmitali.nook.core.model.ArchiveEntry
import dev.anmitali.nook.core.model.FileOperationProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeArchiveRepository(
    private val entries: Result<List<ArchiveEntry>> = Result.success(emptyList()),
    private val passwordProtected: Boolean = false,
) : ArchiveRepository {

    var lastCreateCall: Pair<List<String>, String>? = null
        private set
    var lastExtractCall: Triple<String, String, String?>? = null
        private set

    override fun createZipArchive(sourcePaths: List<String>, destinationZipPath: String): Flow<FileOperationProgress> {
        lastCreateCall = sourcePaths to destinationZipPath
        return flowOf(FileOperationProgress(sourcePaths.size, sourcePaths.size, ""))
    }

    override fun extractZipArchive(
        zipPath: String,
        destinationDirectory: String,
        password: String?,
    ): Flow<FileOperationProgress> {
        lastExtractCall = Triple(zipPath, destinationDirectory, password)
        return flowOf(FileOperationProgress(1, 1, ""))
    }

    override suspend fun isPasswordProtected(zipPath: String): Boolean = passwordProtected

    override suspend fun listArchiveEntries(archivePath: String): Result<List<ArchiveEntry>> = entries
}
