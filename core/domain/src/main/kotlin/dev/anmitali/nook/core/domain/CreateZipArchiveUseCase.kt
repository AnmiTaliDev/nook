package dev.anmitali.nook.core.domain

import dev.anmitali.nook.core.model.FileOperationProgress
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class CreateZipArchiveUseCase @Inject constructor(
    private val archiveRepository: ArchiveRepository,
) {
    operator fun invoke(sourcePaths: List<String>, destinationZipPath: String): Flow<FileOperationProgress> =
        archiveRepository.createZipArchive(sourcePaths, destinationZipPath)
}
