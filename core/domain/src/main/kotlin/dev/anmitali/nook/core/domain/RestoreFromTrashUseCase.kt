package dev.anmitali.nook.core.domain

import javax.inject.Inject

class RestoreFromTrashUseCase @Inject constructor(
    private val fileSource: FileSource,
) {
    suspend operator fun invoke(trashMapping: Map<String, String>): Result<Unit> =
        fileSource.restoreFromTrash(trashMapping)
}
