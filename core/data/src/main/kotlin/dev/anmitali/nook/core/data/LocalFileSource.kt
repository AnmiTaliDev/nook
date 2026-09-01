package dev.anmitali.nook.core.data

import android.content.Context
import android.os.Environment
import android.os.storage.StorageManager
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.anmitali.nook.core.domain.FileSource
import dev.anmitali.nook.core.model.FileConflictPolicy
import dev.anmitali.nook.core.model.FileItem
import dev.anmitali.nook.core.model.FileOperationProgress
import dev.anmitali.nook.core.model.FileType
import dev.anmitali.nook.core.model.Volume
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

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

    override suspend fun findConflicts(
        sourcePaths: List<String>,
        destinationDirectory: String,
    ): List<String> = withContext(Dispatchers.IO) {
        val destinationDir = File(destinationDirectory)
        sourcePaths.map { File(it).name }.filter { name -> File(destinationDir, name).exists() }
    }

    override fun copy(
        sourcePaths: List<String>,
        destinationDirectory: String,
        policy: FileConflictPolicy,
    ): Flow<FileOperationProgress> = flow {
        val destinationDir = File(destinationDirectory)
        val sources = sourcePaths.map { File(it) }
        emitProgressAnd(sources) { source ->
            val target = resolveTarget(source, destinationDir, policy) ?: return@emitProgressAnd
            copyRecursively(source, target)
        }
    }.flowOn(Dispatchers.IO)

    override fun move(
        sourcePaths: List<String>,
        destinationDirectory: String,
        policy: FileConflictPolicy,
    ): Flow<FileOperationProgress> = flow {
        val destinationDir = File(destinationDirectory)
        val sources = sourcePaths.map { File(it) }
        emitProgressAnd(sources) { source ->
            val target = resolveTarget(source, destinationDir, policy) ?: return@emitProgressAnd
            moveFile(source, target)
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun moveToTrash(paths: List<String>): Result<Map<String, String>> =
        withContext(Dispatchers.IO) {
            runCatching {
                trashDirectory.mkdirs()
                paths.associateWith { path ->
                    val source = File(path)
                    val target = uniqueName(trashDirectory, source.name)
                    moveFile(source, target)
                    target.absolutePath
                }
            }
        }

    override suspend fun restoreFromTrash(trashMapping: Map<String, String>): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                trashMapping.forEach { (originalPath, trashPath) ->
                    val originalFile = File(originalPath)
                    originalFile.parentFile?.mkdirs()
                    moveFile(File(trashPath), originalFile)
                }
            }
        }

    override suspend fun rename(path: String, newName: String): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val source = File(path)
                val target = File(source.parentFile, newName)
                check(!target.exists()) { "\"$newName\" already exists." }
                check(source.renameTo(target)) { "Could not rename \"${source.name}\"." }
                target.absolutePath
            }
        }

    override suspend fun createDirectory(parentPath: String, name: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val dir = File(parentPath, name)
                check(!dir.exists()) { "\"$name\" already exists." }
                check(dir.mkdirs()) { "Could not create folder \"$name\"." }
            }
        }

    override suspend fun createFile(parentPath: String, name: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val file = File(parentPath, name)
                check(!file.exists()) { "\"$name\" already exists." }
                check(file.createNewFile()) { "Could not create file \"$name\"." }
            }
        }

    private val trashDirectory: File
        get() = File(Environment.getExternalStorageDirectory(), TRASH_DIRECTORY_NAME)
}

private const val TRASH_DIRECTORY_NAME = ".nook_trash"

private suspend fun kotlinx.coroutines.flow.FlowCollector<FileOperationProgress>.emitProgressAnd(
    sources: List<File>,
    action: (File) -> Unit,
) {
    val total = sources.size
    sources.forEachIndexed { index, source ->
        emit(FileOperationProgress(index, total, source.name))
        action(source)
    }
    emit(FileOperationProgress(total, total, ""))
}

private fun resolveTarget(source: File, destinationDir: File, policy: FileConflictPolicy): File? {
    val target = File(destinationDir, source.name)
    if (!target.exists()) return target
    return when (policy) {
        FileConflictPolicy.SKIP -> null
        FileConflictPolicy.OVERWRITE -> {
            target.deleteRecursively()
            target
        }
        FileConflictPolicy.KEEP_BOTH -> uniqueName(destinationDir, source.name)
    }
}

private fun copyRecursively(source: File, target: File) {
    if (source.isDirectory) {
        target.mkdirs()
        source.listFiles()?.forEach { child -> copyRecursively(child, File(target, child.name)) }
    } else {
        source.copyTo(target, overwrite = true)
    }
}

private fun moveFile(source: File, target: File) {
    if (!source.renameTo(target)) {
        copyRecursively(source, target)
        source.deleteRecursively()
    }
}

private fun uniqueName(directory: File, name: String): File {
    var candidate = File(directory, name)
    if (!candidate.exists()) return candidate
    val dotIndex = name.lastIndexOf('.')
    val base = if (dotIndex > 0) name.substring(0, dotIndex) else name
    val extension = if (dotIndex > 0) name.substring(dotIndex) else ""
    var counter = 1
    do {
        candidate = File(directory, "$base ($counter)$extension")
        counter++
    } while (candidate.exists())
    return candidate
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
