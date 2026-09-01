package dev.anmitali.nook.core.domain

import javax.inject.Inject

class RenameFileUseCase @Inject constructor(
    private val fileSource: FileSource,
) {
    suspend operator fun invoke(path: String, newName: String): Result<String> =
        fileSource.rename(path, newName)
}
