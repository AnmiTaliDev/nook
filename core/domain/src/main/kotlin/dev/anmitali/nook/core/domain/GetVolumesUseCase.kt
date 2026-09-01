package dev.anmitali.nook.core.domain

import dev.anmitali.nook.core.model.Volume
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetVolumesUseCase @Inject constructor(
    private val fileSource: FileSource,
) {
    operator fun invoke(): Flow<List<Volume>> = fileSource.listVolumes()
}
