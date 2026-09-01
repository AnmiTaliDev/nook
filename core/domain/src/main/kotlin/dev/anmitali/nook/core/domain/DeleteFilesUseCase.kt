package dev.anmitali.nook.core.domain

import javax.inject.Inject

class DeleteFilesUseCase @Inject constructor(
    private val fileSource: FileSource,
) {
    suspend operator fun invoke(paths: List<String>): Result<Map<String, String>> =
        fileSource.moveToTrash(paths)
}
