package dev.anmitali.nook.core.domain

import javax.inject.Inject

class FindFileConflictsUseCase @Inject constructor(
    private val fileSource: FileSource,
) {
    suspend operator fun invoke(sourcePaths: List<String>, destinationDirectory: String): List<String> =
        fileSource.findConflicts(sourcePaths, destinationDirectory)
}
