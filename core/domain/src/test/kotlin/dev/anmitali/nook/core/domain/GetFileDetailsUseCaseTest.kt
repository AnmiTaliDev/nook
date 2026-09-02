package dev.anmitali.nook.core.domain

import dev.anmitali.nook.core.model.FileDetails
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetFileDetailsUseCaseTest {

    @Test
    fun `invoke delegates to the file source`() = runTest {
        val details = FileDetails(
            path = "/sd/notes.txt",
            name = "notes.txt",
            isDirectory = false,
            sizeBytes = 42L,
            itemCount = null,
            createdEpochMillis = null,
            modifiedEpochMillis = 100L,
            canRead = true,
            canWrite = true,
            canExecute = false,
            owner = null,
            mimeType = "text/plain",
        )
        val useCase = GetFileDetailsUseCase(FakeFileSource(fileDetails = details))

        assertEquals(details, useCase("/sd/notes.txt"))
    }
}
