package com.zeljko.abstractive.zsv.manager.core.services

import com.zeljko.abstractive.zsv.manager.core.objects.IndexEntry
import com.zeljko.abstractive.zsv.manager.utils.FileUtils.INDEX_DIR
import org.springframework.stereotype.Service
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


@Service
class IndexService(
    val blobService: BlobService
) {
    val indexPath: Path = Paths.get(INDEX_DIR)

    fun saveFileToIndex(filePath: Path) {

        if (!Files.exists(indexPath)) {
            Files.createFile(indexPath)
            val buffer: ByteBuffer = ByteBuffer.allocate(12)
            buffer.position(0).put("DIRC".toByteArray())
            buffer.position(4).putInt(2)
            buffer.position(8).putInt(0)
            Files.write(indexPath, buffer.array())
        }

        val indexEntry = IndexEntry.getFileAttributes(filePath)
        val index = parseIndexFile(false)

        index.firstOrNull {
            it.pathName == indexEntry.pathName
        }?.let { entry ->
            if (entry.mtime != indexEntry.mtime) {
                updateIndexEntry(indexEntry, entry.offset!!)
            }
        } ?: writeIndexEntry(filePath, indexEntry)
    }

    private fun updateIndexEntry(indexEntry: IndexEntry, offset: Int) {
        RandomAccessFile(indexPath.toFile(), "rw").use { file ->
            file.seek(offset.toLong())
            val blobSha = blobService.compressFromFile(true, Paths.get(indexEntry.pathName))
            indexEntry.serialize(file, blobSha)
        }
    }

    private fun writeIndexEntry(filePath: Path, indexEntry: IndexEntry) {
        RandomAccessFile(indexPath.toFile(), "rw").use { file ->
            file.seek(8)
            val entries = file.readInt()

            file.seek(8)
            file.writeInt(entries + 1)

            file.seek(file.length())

            val blobSha = blobService.compressFromFile(true, filePath)
            indexEntry.serialize(file, blobSha)
        }
    }

    fun parseIndexFile(print: Boolean = true): List<IndexEntry> {
        val indexEntries = mutableListOf<IndexEntry>()
        val bytes = indexPath.toFile().readBytes()
        val buffer = ByteBuffer.wrap(bytes)

        val header = ByteArray(4)
        buffer.get(header)
        if (String(header) != "DIRC") {
            throw IllegalStateException("This is not index file, missing DIRC signature")
        }

        val version = buffer.getInt()
        val entries = buffer.getInt()
        if (print) {
            println("Number of entries: $entries")
        }

        for (entry in 1..entries) {
            indexEntries.add(IndexEntry.deserialize(buffer))
        }

        return indexEntries
    }
}