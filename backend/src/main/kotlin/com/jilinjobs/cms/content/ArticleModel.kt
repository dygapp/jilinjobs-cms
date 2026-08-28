package com.jilinjobs.cms.content

import java.time.LocalDate
import java.time.LocalDateTime

enum class ArticleStatus { DRAFT, PUBLISHED, WITHDRAWN }
enum class ArticleType { INTERNAL, EXTERNAL_LINK }

data class CmsArticle(
    val id: Long,
    val columnId: Long,
    val title: String,
    val bodyHtml: String,
    val source: String,
    val articleType: ArticleType,
    val externalUrl: String?,
    val publishDate: LocalDate?,
    val pinned: Boolean,
    val recommended: Boolean,
    val sortOrder: Int,
    val status: ArticleStatus,
    val actualPublishedAt: LocalDateTime?,
    val viewCount: Long,
    val updatedAt: LocalDateTime,
    val coverResourceId: Long? = null,
    val bodyImageResourceIds: List<Long> = emptyList(),
    val attachmentResourceIds: List<Long> = emptyList(),
)

data class ArticleDraft(
    val columnId: Long,
    val title: String,
    val bodyHtml: String,
    val source: String,
    val articleType: ArticleType,
    val externalUrl: String?,
    val publishDate: LocalDate?,
    val pinned: Boolean,
    val recommended: Boolean,
    val sortOrder: Int,
    val coverResourceId: Long?,
    val bodyImageResourceIds: List<Long>,
    val attachmentResourceIds: List<Long>,
)

data class PublicArticleSummary(
    val id: Long,
    val columnId: Long,
    val columnName: String,
    val title: String,
    val publishDate: LocalDate?,
    val pinned: Boolean,
    val recommended: Boolean,
    val sortOrder: Int,
    val columnAlias: String = "",
    val source: String = "",
    val articleType: ArticleType = ArticleType.INTERNAL,
    val externalUrl: String? = null,
)

data class PublicArticleDetail(
    val id: Long,
    val columnId: Long,
    val columnName: String,
    val title: String,
    val bodyHtml: String,
    val source: String,
    val publishDate: LocalDate?,
    val bodyImageResourceIds: List<Long>,
    val attachments: List<PublicArticleAttachment>,
    val columnAlias: String = "",
    val articleType: ArticleType = ArticleType.INTERNAL,
    val externalUrl: String? = null,
)

data class PublicArticleAttachment(val id: Long,val originalFilename: String,val contentType: String?,val sizeBytes: Long)
data class PublicArticlePage(val items: List<PublicArticleSummary>,val page: Int,val size: Int,val total: Long)

interface ArticleRepository {
    fun findAll(): List<CmsArticle>
    fun findById(id: Long): CmsArticle?
    fun insert(draft: ArticleDraft): CmsArticle
    fun update(id: Long, draft: ArticleDraft): CmsArticle
    fun updateStatus(id: Long, status: ArticleStatus, actualPublishedAt: LocalDateTime?): CmsArticle
    fun findPublished(columnId: Long?, limit: Int, offset: Int): List<CmsArticle>
    fun countPublished(columnId: Long?): Long
    fun findPublishedById(id: Long): CmsArticle?
    fun incrementPublishedViewCount(id: Long): Boolean
    fun existsByColumn(columnId: Long): Boolean
}
class ArticleValidationException(message: String):RuntimeException(message)
class ArticleNotFoundException(id: Long):RuntimeException("文章不存在或不可用：$id")
