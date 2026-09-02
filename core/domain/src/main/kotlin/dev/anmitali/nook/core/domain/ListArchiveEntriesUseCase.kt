package dev.anmitali.nook.core.domain

import dev.anmitali.nook.core.model.ArchiveEntry
import javax.inject.Inject

class ListArchiveEntriesUseCase @Inject constructor(
    private val archiveRepository: ArchiveRepository,
) {
    suspend operator fun invoke(archivePath: String): Result<List<ArchiveEntry>> =
        archiveRepository.listArchiveEntries(archivePath)
}
