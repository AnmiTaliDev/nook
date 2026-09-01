package dev.anmitali.nook.core.domain

import dev.anmitali.nook.core.model.FileItem
import dev.anmitali.nook.core.model.FileType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchFilesUseCaseTest {

    @Test
    fun `invoke delegates to the file source and returns matches`() = runTest {
        val match = FileItem(
            name = "notes.txt",
            path = "/sd/deep/notes.txt",
            type = FileType.TEXT,
            isDirectory = false,
            sizeBytes = 10L,
            lastModifiedEpochMillis = 0L,
            isHidden = false,
            canRead = true,
            canWrite = true,
        )
        val useCase = SearchFilesUseCase(FakeFileSource(searchResults = listOf(match)))

        val result = useCase("/sd", "notes").first()

        assertEquals(listOf(match), result)
    }
}
