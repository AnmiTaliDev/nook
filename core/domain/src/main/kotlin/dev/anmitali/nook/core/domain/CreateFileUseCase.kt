package dev.anmitali.nook.core.domain

import javax.inject.Inject

class CreateFileUseCase @Inject constructor(
    private val fileSource: FileSource,
) {
    suspend operator fun invoke(parentPath: String, name: String): Result<Unit> =
        fileSource.createFile(parentPath, name)
}
