package com.jilinjobs.cms.resource

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.UUID

interface FileStorage {
    fun store(file: MultipartFile): StoredFile

    fun resolve(storageKey: String): Path

    fun delete(storageKey: String)
}

data class StoredFile(
    val storageKey: String,
    val sizeBytes: Long,
)

@Component
class LocalFileStorage(
    @Value("\${cms.storage.root:./data/uploads}") storageRoot: String,
) : FileStorage {
    private val root: Path = Paths.get(storageRoot).toAbsolutePath().normalize().also { Files.createDirectories(it) }

    override fun store(file: MultipartFile): StoredFile {
        val storageKey = UUID.randomUUID().toString()
        val target = safePath(storageKey)
        file.inputStream.use { input ->
            Files.copy(input, target)
        }
        return StoredFile(storageKey, Files.size(target))
    }

    override fun resolve(storageKey: String): Path = safePath(storageKey)

    override fun delete(storageKey: String) {
        Files.deleteIfExists(safePath(storageKey))
    }

    private fun safePath(storageKey: String): Path {
        val target = root.resolve(storageKey).normalize()
        if (!target.startsWith(root)) {
            throw ResourceValidationException("非法文件存储键")
        }
        return target
    }
}

@Service
class ResourceService(
    private val repository: ResourceRepository,
    private val storage: FileStorage,
) : ArticleResourceAssociation {
    fun upload(file: MultipartFile): CmsResource {
        if (file.isEmpty) {
            throw ResourceValidationException("上传文件不能为空")
        }
        val originalFilename = normalizeFilename(file.originalFilename)
        val stored = storage.store(file)
        return try {
            repository.insert(
                ResourceDraft(
                    storageKey = stored.storageKey,
                    originalFilename = originalFilename,
                    contentType = file.contentType,
                    sizeBytes = stored.sizeBytes,
                ),
            )
        } catch (exception: RuntimeException) {
            storage.delete(stored.storageKey)
            throw exception
        }
    }

    fun get(id: Long): CmsResource = repository.findById(id) ?: throw ResourceNotFoundException(id)

    fun resolveContent(id: Long): Pair<CmsResource, Path> {
        val resource = get(id)
        val path = storage.resolve(resource.storageKey)
        if (!Files.isRegularFile(path)) {
            throw ResourceNotFoundException(id)
        }
        return resource to path
    }

    fun resolvePublishedImage(id: Long): Pair<CmsResource, Path> {
        if (!repository.isPublishedImage(id)) {
            throw ResourceNotFoundException(id)
        }
        return resolveContent(id)
    }

    fun resolvePublishedBodyImage(id: Long): Pair<CmsResource, Path> {
        if (!repository.isPublishedBodyImage(id)) {
            throw ResourceNotFoundException(id)
        }
        return resolveContent(id)
    }

    fun resolvePublishedAttachment(id: Long): Pair<CmsResource, Path> {
        if (!repository.isPublishedAttachment(id)) {
            throw ResourceNotFoundException(id)
        }
        return resolveContent(id)
    }

    override fun findArticleResources(articleId: Long): ArticleResourceLinks = ArticleResourceLinks(
        coverResourceId = repository.findArticleResourceIds(articleId, ArticleResourceRole.COVER).firstOrNull(),
        bodyImageResourceIds = repository.findArticleResourceIds(articleId, ArticleResourceRole.BODY_IMAGE),
        attachmentResourceIds = repository.findArticleResourceIds(articleId, ArticleResourceRole.ATTACHMENT),
    )

    override fun findArticleAttachments(articleId: Long): List<CmsResource> =
        repository.findArticleResourceIds(articleId, ArticleResourceRole.ATTACHMENT).map(::get)

    override fun replaceArticleResources(articleId: Long, links: ArticleResourceLinks) {
        val bodyImages = links.bodyImageResourceIds.distinct()
        val attachments = links.attachmentResourceIds.distinct()
        val resourceIds = buildList {
            links.coverResourceId?.let(::add)
            addAll(bodyImages)
            addAll(attachments)
        }.distinct()
        resourceIds.forEach { id ->
            repository.findById(id) ?: throw ResourceValidationException("文件资源不存在：$id")
        }

        repository.deleteArticleLinks(articleId)
        links.coverResourceId?.let { repository.insertArticleLink(articleId, it, ArticleResourceRole.COVER, 0) }
        bodyImages.forEachIndexed { index, id ->
            repository.insertArticleLink(articleId, id, ArticleResourceRole.BODY_IMAGE, index)
        }
        attachments.forEachIndexed { index, id ->
            repository.insertArticleLink(articleId, id, ArticleResourceRole.ATTACHMENT, index)
        }
    }

    private fun normalizeFilename(filename: String?): String {
        val normalized = filename.orEmpty().substringAfterLast('/').substringAfterLast('\\').ifBlank { "upload.bin" }
        if (normalized.length > 255) {
            throw ResourceValidationException("文件名不能超过 255 个字符")
        }
        return normalized
    }
}
