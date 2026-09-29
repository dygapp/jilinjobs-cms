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

    @Test
    fun `managed upload compensation removes file written before database rollback`() {
        val storageRoot = Files.createDirectory(tempDir.resolve("managed-storage"))
        val journal = FileMutationJournal()
        val service = ResourceService(InMemoryResourceRepository(), LocalFileStorage(storageRoot.toString()), journal)
        journal.begin()

        service.upload(UploadContent("managed.bin", "application/octet-stream", 3) { byteArrayOf(1, 2, 3).inputStream() })
        Files.list(storageRoot).use { assertEquals(1, it.count()) }
        journal.compensate()

        Files.list(storageRoot).use { assertEquals(0, it.count()) }
    }
}

private class InMemoryResourceRepository : ResourceRepository {
    private var nextId = 1L
    override fun insert(draft: ResourceDraft) = CmsResource(
        nextId++, draft.storageKey, draft.originalFilename, draft.contentType, draft.sizeBytes,
    )
    override fun findById(id: Long): CmsResource? = null
    override fun findArticleResourceIds(articleId: Long, role: ArticleResourceRole) = emptyList<Long>()
    override fun isPublishedImage(resourceId: Long) = false
    override fun isPublishedBodyImage(resourceId: Long) = false
    override fun isPublishedAttachment(resourceId: Long) = false
    override fun deleteArticleLinks(articleId: Long) = Unit
    override fun insertArticleLink(articleId: Long, resourceId: Long, role: ArticleResourceRole, sortOrder: Int) = Unit
}
