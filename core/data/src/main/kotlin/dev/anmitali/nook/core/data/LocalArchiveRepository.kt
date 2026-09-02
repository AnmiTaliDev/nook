package dev.anmitali.nook.core.data

import dev.anmitali.nook.core.domain.ArchiveRepository
import dev.anmitali.nook.core.domain.WrongPasswordException
import dev.anmitali.nook.core.model.ArchiveEntry
import dev.anmitali.nook.core.model.FileOperationProgress
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.exception.ZipException
import net.lingala.zip4j.model.ZipParameters
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream

class LocalArchiveRepository @Inject constructor() : ArchiveRepository {

    override fun createZipArchive(
        sourcePaths: List<String>,
        destinationZipPath: String,
    ): Flow<FileOperationProgress> = flow {
        val zipFile = ZipFile(destinationZipPath)
        val total = sourcePaths.size
        sourcePaths.forEachIndexed { index, path ->
            val source = File(path)
            emit(FileOperationProgress(index, total, source.name))
            if (source.isDirectory) {
                zipFile.addFolder(source, ZipParameters())
            } else {
                zipFile.addFile(source, ZipParameters())
            }
        }
        emit(FileOperationProgress(total, total, ""))
    }.flowOn(Dispatchers.IO)

    override fun extractZipArchive(
        zipPath: String,
        destinationDirectory: String,
        password: String?,
    ): Flow<FileOperationProgress> = flow {
        val zipFile = ZipFile(zipPath)
        if (zipFile.isEncrypted && password != null) {
            zipFile.setPassword(password.toCharArray())
        }
        try {
            val headers = zipFile.fileHeaders
            val total = headers.size
            headers.forEachIndexed { index, header ->
                emit(FileOperationProgress(index, total, header.fileName))
                zipFile.extractFile(header, destinationDirectory)
            }
            emit(FileOperationProgress(total, total, ""))
        } catch (e: ZipException) {
            if (e.type == ZipException.Type.WRONG_PASSWORD) {
                throw WrongPasswordException(zipPath)
            }
            throw e
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun isPasswordProtected(zipPath: String): Boolean = withContext(Dispatchers.IO) {
        runCatching { ZipFile(zipPath).isEncrypted }.getOrDefault(false)
    }

    override suspend fun listArchiveEntries(archivePath: String): Result<List<ArchiveEntry>> =
        withContext(Dispatchers.IO) {
            runCatching {
                when (archiveFormatOf(archivePath)) {
                    ArchiveFormat.ZIP -> listZipEntries(archivePath)
                    ArchiveFormat.TAR -> listTarEntries(FileInputStream(archivePath))
                    ArchiveFormat.TAR_GZ -> listTarEntries(GzipCompressorInputStream(FileInputStream(archivePath)))
                    ArchiveFormat.SEVEN_ZIP -> listSevenZipEntries(archivePath)
                }
            }
        }

    private fun listZipEntries(archivePath: String): List<ArchiveEntry> =
        ZipFile(archivePath).fileHeaders.map { header ->
            ArchiveEntry(
                name = File(header.fileName).name,
                path = header.fileName,
                isDirectory = header.isDirectory,
                sizeBytes = header.uncompressedSize,
            )
        }

    private fun listTarEntries(inputStream: InputStream): List<ArchiveEntry> =
        TarArchiveInputStream(inputStream).use { tarStream ->
            generateSequence { tarStream.nextEntry }
                .map { entry ->
                    ArchiveEntry(
                        name = File(entry.name).name,
                        path = entry.name,
                        isDirectory = entry.isDirectory,
                        sizeBytes = entry.size,
                    )
                }
                .toList()
        }

    private fun listSevenZipEntries(archivePath: String): List<ArchiveEntry> =
        SevenZFile.builder().setFile(File(archivePath)).get().use { sevenZFile ->
            generateSequence { sevenZFile.nextEntry }
                .map { entry ->
                    ArchiveEntry(
                        name = File(entry.name).name,
                        path = entry.name,
                        isDirectory = entry.isDirectory,
                        sizeBytes = entry.size,
                    )
                }
                .toList()
        }
}

private enum class ArchiveFormat { ZIP, TAR, TAR_GZ, SEVEN_ZIP }

private fun archiveFormatOf(path: String): ArchiveFormat = when {
    path.endsWith(".tar.gz", ignoreCase = true) || path.endsWith(".tgz", ignoreCase = true) -> ArchiveFormat.TAR_GZ
    path.endsWith(".tar", ignoreCase = true) -> ArchiveFormat.TAR
    path.endsWith(".7z", ignoreCase = true) -> ArchiveFormat.SEVEN_ZIP
    else -> ArchiveFormat.ZIP
}
