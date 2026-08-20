package com.jilinjobs.cms.content

import java.time.LocalDate
import java.time.LocalDateTime

enum class ArticleStatus {
    DRAFT,
    PUBLISHED,
    WITHDRAWN,
}

data class CmsArticle(
    val id: Long,
    val columnId: Long,
    val title: String,
    val bodyHtml: String,
    val source: String,
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
    val publishDate: LocalDate?,
    val pinned: Boolean,
    val recommended: Boolean,
    val sortOrder: Int,
    val coverResourceId: Long?,
    val bodyImageResourceIds: List<Long>,
    val attachmentResourceIds: List<Long>,
)

interface ArticleRepository {
    fun findAll(): List<CmsArticle>

    fun findById(id: Long): CmsArticle?

    fun insert(draft: ArticleDraft): CmsArticle

    fun update(id: Long, draft: ArticleDraft): CmsArticle

    fun existsByColumn(columnId: Long): Boolean
}

class ArticleValidationException(message: String) : RuntimeException(message)

class ArticleNotFoundException(id: Long) : RuntimeException("文章不存在：$id")
