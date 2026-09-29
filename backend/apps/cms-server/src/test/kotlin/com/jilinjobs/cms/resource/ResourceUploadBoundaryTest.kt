package com.jilinjobs.cms.resource

import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

class ResourceUploadBoundaryTest {
    @TempDir lateinit var tempDir: Path

    @Test
    fun `path-backed upload can be read repeatedly without web types`() {
        val path = tempDir.resolve("source.txt")
        Files.writeString(path, "中文资源")
        val content = UploadContent.fromPath(path, "original.txt", "text/plain")

        assertEquals("original.txt", content.originalFilename)
        assertEquals("text/plain", content.contentType)
        assertEquals(Files.size(path), content.size)
        repeat(2) {
            assertTrue(content.inputStream.use { it.readAllBytes() }.contentEquals(Files.readAllBytes(path)))
        }
    }

    @Test
    fun `partial storage failure removes incomplete file`() {
        val storageRoot = Files.createDirectory(tempDir.resolve("storage"))
        val upload = UploadContent("broken.bin", "application/octet-stream", 2) {
            object : InputStream() {
                private var first = true
                override fun read(): Int {
                    if (first) {
                        first = false
                        return 0x42
                    }
                    throw IOException("source stream interrupted")
                }
            }
        }

        assertThrows(IOException::class.java) { LocalFileStorage(storageRoot.toString()).store(upload) }
        Files.list(storageRoot).use { assertEquals(0, it.count()) }
    }
}
