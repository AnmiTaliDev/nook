package dev.anmitali.nook.core.data

import android.content.Context
import android.os.storage.StorageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.anmitali.nook.core.domain.FileSource
import dev.anmitali.nook.core.model.FileItem
import dev.anmitali.nook.core.model.FileType
import dev.anmitali.nook.core.model.Volume
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class LocalFileSource @Inject constructor(
    @ApplicationContext private val context: Context,
) : FileSource {

    override fun listDirectory(path: String): Flow<List<FileItem>> = flow {
        val children = File(path).listFiles().orEmpty()
        emit(children.map { it.toFileItem() }.sortedWith(directoryFirstThenName))
    }.flowOn(Dispatchers.IO)

    override fun listVolumes(): Flow<List<Volume>> = flow {
        val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        val volumes = storageManager.storageVolumes.mapNotNull { volume ->
            val rootDir = volume.directory ?: return@mapNotNull null
            Volume(
                id = volume.uuid ?: rootDir.absolutePath,
                label = volume.getDescription(context),
                rootPath = rootDir.absolutePath,
                isPrimary = volume.isPrimary,
                isRemovable = volume.isRemovable,
                totalBytes = rootDir.totalSpace,
                availableBytes = rootDir.usableSpace,
            )
        }
        emit(volumes)
    }.flowOn(Dispatchers.IO)
}

private val directoryFirstThenName = compareByDescending<FileItem> { it.isDirectory }
    .thenBy { it.name.lowercase() }

private fun File.toFileItem(): FileItem = FileItem(
    name = name,
    path = absolutePath,
    type = if (isDirectory) FileType.DIRECTORY else fileTypeOf(extension),
    isDirectory = isDirectory,
    sizeBytes = if (isDirectory) 0L else length(),
    lastModifiedEpochMillis = lastModified(),
    isHidden = isHidden,
    canRead = canRead(),
    canWrite = canWrite(),
)

private fun fileTypeOf(extension: String): FileType = when (extension.lowercase()) {
    "jpg", "jpeg", "png", "gif", "webp", "bmp", "heic" -> FileType.IMAGE
    "mp4", "mkv", "webm", "avi", "mov" -> FileType.VIDEO
    "mp3", "wav", "flac", "ogg", "m4a" -> FileType.AUDIO
    "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "odt" -> FileType.DOCUMENT
    "zip", "rar", "7z", "tar", "gz" -> FileType.ARCHIVE
    "apk" -> FileType.APK
    "txt", "md", "json", "xml", "kt", "java", "log" -> FileType.TEXT
    else -> FileType.OTHER
}
