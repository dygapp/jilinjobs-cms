package com.jilinjobs.cms.resource

data class CmsResource(
    val id: Long,
    val storageKey: String,
    val originalFilename: String,
    val contentType: String?,
    val sizeBytes: Long,
)

data class ResourceDraft(
    val storageKey: String,
    val originalFilename: String,
    val contentType: String?,
    val sizeBytes: Long,
)

enum class ArticleResourceRole {
    COVER,
    BODY_IMAGE,
    ATTACHMENT,
}

data class ArticleResourceLinks(
    val coverResourceId: Long? = null,
    val bodyImageResourceIds: List<Long> = emptyList(),
    val attachmentResourceIds: List<Long> = emptyList(),
)

interface ResourceRepository {
    fun insert(draft: ResourceDraft): CmsResource

    fun findById(id: Long): CmsResource?

    fun findArticleResourceIds(articleId: Long, role: ArticleResourceRole): List<Long>

    fun isPublishedBodyImage(resourceId: Long): Boolean

    fun isPublishedAttachment(resourceId: Long): Boolean

    fun deleteArticleLinks(articleId: Long)

    fun insertArticleLink(articleId: Long, resourceId: Long, role: ArticleResourceRole, sortOrder: Int)
}

interface ArticleResourceAssociation {
    fun findArticleResources(articleId: Long): ArticleResourceLinks

    fun findArticleAttachments(articleId: Long): List<CmsResource>

    fun replaceArticleResources(articleId: Long, links: ArticleResourceLinks)
}

class ResourceValidationException(message: String) : RuntimeException(message)

class ResourceNotFoundException(id: Long) : RuntimeException("文件资源不存在或不可用：$id")
