package dev.anmitali.nook.core.domain

import dev.anmitali.nook.core.model.FileItem
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class SearchFilesUseCase @Inject constructor(
    private val fileSource: FileSource,
) {
    operator fun invoke(rootPath: String, query: String): Flow<List<FileItem>> =
        fileSource.search(rootPath, query)
}
