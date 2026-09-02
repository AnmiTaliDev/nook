package dev.anmitali.nook.core.domain

import javax.inject.Inject

class IsZipPasswordProtectedUseCase @Inject constructor(
    private val archiveRepository: ArchiveRepository,
) {
    suspend operator fun invoke(zipPath: String): Boolean = archiveRepository.isPasswordProtected(zipPath)
}
