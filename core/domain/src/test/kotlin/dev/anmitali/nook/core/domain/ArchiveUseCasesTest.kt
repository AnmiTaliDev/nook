package dev.anmitali.nook.core.domain

import dev.anmitali.nook.core.model.ArchiveEntry
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArchiveUseCasesTest {

    @Test
    fun `createZipArchive delegates to the repository`() = runTest {
        val repository = FakeArchiveRepository()
        val useCase = CreateZipArchiveUseCase(repository)

        useCase(listOf("/sd/a.txt"), "/sd/archive.zip").toList()

        assertEquals(listOf("/sd/a.txt") to "/sd/archive.zip", repository.lastCreateCall)
    }

    @Test
    fun `extractZipArchive delegates to the repository with the password`() = runTest {
        val repository = FakeArchiveRepository()
        val useCase = ExtractZipArchiveUseCase(repository)

        useCase("/sd/archive.zip", "/sd/out", "secret").toList()

        assertEquals(Triple("/sd/archive.zip", "/sd/out", "secret"), repository.lastExtractCall)
    }

    @Test
    fun `isPasswordProtected returns the repository value`() = runTest {
        val useCase = IsZipPasswordProtectedUseCase(FakeArchiveRepository(passwordProtected = true))

        assertTrue(useCase("/sd/archive.zip"))
    }

    @Test
    fun `listArchiveEntries returns entries from the repository`() = runTest {
        val entry = ArchiveEntry("notes.txt", "notes.txt", isDirectory = false, sizeBytes = 10L)
        val useCase = ListArchiveEntriesUseCase(FakeArchiveRepository(entries = Result.success(listOf(entry))))

        val result = useCase("/sd/archive.zip")

        assertEquals(listOf(entry), result.getOrThrow())
    }
}
