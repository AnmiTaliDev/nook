package dev.anmitali.nook.core.domain

import javax.inject.Inject

class CreateDirectoryUseCase @Inject constructor(
    private val fileSource: FileSource,
) {
    suspend operator fun invoke(parentPath: String, name: String): Result<Unit> =
        fileSource.createDirectory(parentPath, name)
}
