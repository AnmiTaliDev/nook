package dev.anmitali.nook.core.domain

import dev.anmitali.nook.core.model.FileDetails
import javax.inject.Inject

class GetFileDetailsUseCase @Inject constructor(
    private val fileSource: FileSource,
) {
    suspend operator fun invoke(path: String): FileDetails = fileSource.getFileDetails(path)
}
