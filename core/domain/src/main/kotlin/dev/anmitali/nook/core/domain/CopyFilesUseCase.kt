package dev.anmitali.nook.core.domain

import dev.anmitali.nook.core.model.FileConflictPolicy
import dev.anmitali.nook.core.model.FileOperationProgress
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class CopyFilesUseCase @Inject constructor(
    private val fileSource: FileSource,
) {
    operator fun invoke(
        sourcePaths: List<String>,
        destinationDirectory: String,
        policy: FileConflictPolicy,
    ): Flow<FileOperationProgress> = fileSource.copy(sourcePaths, destinationDirectory, policy)
}
