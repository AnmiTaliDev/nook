package dev.anmitali.nook.core.domain

import dev.anmitali.nook.core.model.Volume
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetVolumesUseCaseTest {

    @Test
    fun `invoke returns volumes from the file source`() = runTest {
        val internalStorage = Volume(
            id = "primary",
            label = "Internal storage",
            rootPath = "/storage/emulated/0",
            isPrimary = true,
            isRemovable = false,
            totalBytes = 128_000_000_000L,
            availableBytes = 64_000_000_000L,
        )
        val useCase = GetVolumesUseCase(FakeFileSource(volumes = listOf(internalStorage)))

        val result = useCase().first()

        assertEquals(listOf(internalStorage), result)
    }
}
