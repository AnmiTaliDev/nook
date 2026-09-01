package dev.anmitali.nook.core.domain

import dev.anmitali.nook.core.model.FileItem
import dev.anmitali.nook.core.model.FileType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ListFilesUseCaseTest {

    private val documentItem = FileItem(
        name = "notes.txt",
        path = "/storage/emulated/0/notes.txt",
        type = FileType.TEXT,
        isDirectory = false,
        sizeBytes = 1024L,
        lastModifiedEpochMillis = 0L,
        isHidden = false,
        canRead = true,
        canWrite = true,
    )

    @Test
    fun `invoke returns items for the requested path`() = runTest {
        val fileSource = FakeFileSource(
            directories = mapOf("/storage/emulated/0" to listOf(documentItem)),
        )
        val useCase = ListFilesUseCase(fileSource)

        val result = useCase("/storage/emulated/0").first()

        assertEquals(listOf(documentItem), result)
    }

    @Test
    fun `invoke returns empty list for unknown path`() = runTest {
        val useCase = ListFilesUseCase(FakeFileSource())

        val result = useCase("/unknown").first()

        assertEquals(emptyList<FileItem>(), result)
    }
}
