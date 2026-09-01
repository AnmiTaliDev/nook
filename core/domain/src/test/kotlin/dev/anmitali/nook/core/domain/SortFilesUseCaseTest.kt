package dev.anmitali.nook.core.domain

import dev.anmitali.nook.core.model.FileItem
import dev.anmitali.nook.core.model.FileType
import dev.anmitali.nook.core.model.SortBy
import dev.anmitali.nook.core.model.SortDirection
import dev.anmitali.nook.core.model.SortOrder
import org.junit.Assert.assertEquals
import org.junit.Test

class SortFilesUseCaseTest {

    private val useCase = SortFilesUseCase()

    private fun file(name: String, size: Long = 0L, modified: Long = 0L, isDirectory: Boolean = false) = FileItem(
        name = name,
        path = "/$name",
        type = if (isDirectory) FileType.DIRECTORY else FileType.TEXT,
        isDirectory = isDirectory,
        sizeBytes = size,
        lastModifiedEpochMillis = modified,
        isHidden = false,
        canRead = true,
        canWrite = true,
    )

    @Test
    fun `directories always come before files regardless of sort field`() {
        val items = listOf(file("b.txt"), file("Folder", isDirectory = true), file("a.txt"))

        val result = useCase(items, SortOrder(SortBy.NAME, SortDirection.ASCENDING))

        assertEquals(listOf("Folder", "a.txt", "b.txt"), result.map { it.name })
    }

    @Test
    fun `sorts by size descending within the same group`() {
        val items = listOf(file("small.txt", size = 10), file("large.txt", size = 100))

        val result = useCase(items, SortOrder(SortBy.SIZE, SortDirection.DESCENDING))

        assertEquals(listOf("large.txt", "small.txt"), result.map { it.name })
    }

    @Test
    fun `sorts by date modified ascending`() {
        val items = listOf(file("newer.txt", modified = 200), file("older.txt", modified = 100))

        val result = useCase(items, SortOrder(SortBy.DATE_MODIFIED, SortDirection.ASCENDING))

        assertEquals(listOf("older.txt", "newer.txt"), result.map { it.name })
    }
}
