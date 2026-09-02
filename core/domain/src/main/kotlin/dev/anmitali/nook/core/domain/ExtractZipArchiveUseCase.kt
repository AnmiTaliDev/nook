package dev.anmitali.nook.core.domain

import dev.anmitali.nook.core.model.FileOperationProgress
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ExtractZipArchiveUseCase @Inject constructor(
    private val archiveRepository: ArchiveRepository,
) {
    operator fun invoke(
        zipPath: String,
        destinationDirectory: String,
        password: String? = null,
    ): Flow<FileOperationProgress> = archiveRepository.extractZipArchive(zipPath, destinationDirectory, password)
}
