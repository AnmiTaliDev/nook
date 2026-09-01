package dev.anmitali.nook.core.domain

import dev.anmitali.nook.core.model.FileConflictPolicy
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FileOperationsUseCasesTest {

    @Test
    fun `findConflicts returns names that already exist at the destination`() = runTest {
        val fileSource = FakeFileSource(existingNames = setOf("notes.txt"))
        val useCase = FindFileConflictsUseCase(fileSource)

        val conflicts = useCase(listOf("/sd/notes.txt", "/sd/photo.jpg"), "/dest")

        assertEquals(listOf("notes.txt"), conflicts)
    }

    @Test
    fun `copy delegates to the file source with the chosen policy`() = runTest {
        val fileSource = FakeFileSource()
        val useCase = CopyFilesUseCase(fileSource)

        useCase(listOf("/sd/a.txt"), "/dest", FileConflictPolicy.KEEP_BOTH).toList()

        assertEquals(
            Triple(listOf("/sd/a.txt"), "/dest", FileConflictPolicy.KEEP_BOTH),
            fileSource.lastCopyCall,
        )
    }

    @Test
    fun `move delegates to the file source with the chosen policy`() = runTest {
        val fileSource = FakeFileSource()
        val useCase = MoveFilesUseCase(fileSource)

        useCase(listOf("/sd/a.txt"), "/dest", FileConflictPolicy.OVERWRITE).toList()

        assertEquals(
            Triple(listOf("/sd/a.txt"), "/dest", FileConflictPolicy.OVERWRITE),
            fileSource.lastMoveCall,
        )
    }

    @Test
    fun `delete moves files to trash and returns a restorable mapping`() = runTest {
        val fileSource = FakeFileSource()
        val useCase = DeleteFilesUseCase(fileSource)

        val result = useCase(listOf("/sd/a.txt"))

        assertTrue(result.isSuccess)
        assertEquals(listOf("/sd/a.txt"), fileSource.lastTrashedPaths)
        assertEquals(mapOf("/sd/a.txt" to "/trash/a.txt"), result.getOrThrow())
    }

    @Test
    fun `restore delegates the trash mapping back to the file source`() = runTest {
        val fileSource = FakeFileSource()
        val useCase = RestoreFromTrashUseCase(fileSource)
        val mapping = mapOf("/sd/a.txt" to "/trash/a.txt")

        val result = useCase(mapping)

        assertTrue(result.isSuccess)
        assertEquals(mapping, fileSource.lastRestoredMapping)
    }
}
