package dev.anmitali.nook.core.domain

import dev.anmitali.nook.core.model.FileItem
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ListFilesUseCase @Inject constructor(
    private val fileSource: FileSource,
) {
    operator fun invoke(path: String): Flow<List<FileItem>> = fileSource.listDirectory(path)
}
