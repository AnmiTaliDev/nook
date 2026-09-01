package dev.anmitali.nook.core.domain

import dev.anmitali.nook.core.model.FileItem
import dev.anmitali.nook.core.model.SortBy
import dev.anmitali.nook.core.model.SortDirection
import dev.anmitali.nook.core.model.SortOrder
import javax.inject.Inject

class SortFilesUseCase @Inject constructor() {

    operator fun invoke(items: List<FileItem>, sortOrder: SortOrder): List<FileItem> {
        val fieldComparator = when (sortOrder.sortBy) {
            SortBy.NAME -> compareBy<FileItem> { it.name.lowercase() }
            SortBy.DATE_MODIFIED -> compareBy { it.lastModifiedEpochMillis }
            SortBy.SIZE -> compareBy { it.sizeBytes }
            SortBy.TYPE -> compareBy { it.type.name }
        }
        val directed = if (sortOrder.direction == SortDirection.DESCENDING) {
            fieldComparator.reversed()
        } else {
            fieldComparator
        }
        return items.sortedWith(compareByDescending<FileItem> { it.isDirectory }.then(directed))
    }
}
