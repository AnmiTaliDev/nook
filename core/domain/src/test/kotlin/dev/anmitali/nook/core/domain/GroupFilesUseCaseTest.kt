package dev.anmitali.nook.core.domain

import dev.anmitali.nook.core.model.FileGroupLabel
import dev.anmitali.nook.core.model.FileItem
import dev.anmitali.nook.core.model.FileType
import dev.anmitali.nook.core.model.GroupBy
import org.junit.Assert.assertEquals
import org.junit.Test

class GroupFilesUseCaseTest {

    private val useCase = GroupFilesUseCase()

    private fun file(name: String, type: FileType, modified: Long = 0L) = FileItem(
        name = name,
        path = "/$name",
        type = type,
        isDirectory = type == FileType.DIRECTORY,
        sizeBytes = 0L,
        lastModifiedEpochMillis = modified,
        isHidden = false,
        canRead = true,
        canWrite = true,
    )

    @Test
    fun `NONE groupBy returns a single group with all items`() {
        val items = listOf(file("a.jpg", FileType.IMAGE), file("b.txt", FileType.TEXT))

        val result = useCase(items, GroupBy.NONE)

        assertEquals(1, result.size)
        assertEquals(FileGroupLabel.None, result.first().label)
        assertEquals(items, result.first().items)
    }

    @Test
    fun `TYPE groupBy buckets items by file type`() {
        val items = listOf(file("a.jpg", FileType.IMAGE), file("b.txt", FileType.TEXT), file("c.png", FileType.IMAGE))

        val result = useCase(items, GroupBy.TYPE)

        val imageGroup = result.first { it.label == FileGroupLabel.Type(FileType.IMAGE) }
        assertEquals(setOf("a.jpg", "c.png"), imageGroup.items.map { it.name }.toSet())
    }

    @Test
    fun `DATE groupBy buckets a fresh file as today`() {
        val items = listOf(file("now.txt", FileType.TEXT, modified = System.currentTimeMillis()))

        val result = useCase(items, GroupBy.DATE)

        assertEquals(1, result.size)
        assertEquals("now.txt", result.first().items.first().name)
    }
}
