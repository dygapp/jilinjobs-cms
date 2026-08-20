package com.jilinjobs.cms.content

import com.jilinjobs.cms.column.ColumnContentDependency
import com.jilinjobs.cms.column.ColumnQuery
import com.jilinjobs.cms.resource.ArticleResourceAssociation
import com.jilinjobs.cms.resource.ArticleResourceLinks
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ArticleService(
    private val repository: ArticleRepository,
    private val columnQuery: ColumnQuery,
    private val resourceAssociation: ArticleResourceAssociation,
) {
    @Transactional(readOnly = true)
    fun list(): List<CmsArticle> = repository.findAll().map(::withResources)

    @Transactional(readOnly = true)
    fun get(id: Long): CmsArticle = withResources(
        repository.findById(id) ?: throw ArticleNotFoundException(id),
    )

    @Transactional
    fun create(draft: ArticleDraft): CmsArticle {
        val normalized = normalize(draft)
        val created = repository.insert(normalized)
        resourceAssociation.replaceArticleResources(created.id, normalized.toResourceLinks())
        return withResources(created)
    }

    @Transactional
    fun update(id: Long, draft: ArticleDraft): CmsArticle {
        repository.findById(id) ?: throw ArticleNotFoundException(id)
        val normalized = normalize(draft)
        val updated = repository.update(id, normalized)
        resourceAssociation.replaceArticleResources(id, normalized.toResourceLinks())
        return withResources(updated)
    }

    @Transactional
    fun publish(id: Long): CmsArticle {
        val current = repository.findById(id) ?: throw ArticleNotFoundException(id)
        if (current.status == ArticleStatus.PUBLISHED) {
            throw ArticleValidationException("文章已经处于已发布状态")
        }
        return withResources(repository.updateStatus(id, ArticleStatus.PUBLISHED, LocalDateTime.now()))
    }

    @Transactional
    fun withdraw(id: Long): CmsArticle {
        val current = repository.findById(id) ?: throw ArticleNotFoundException(id)
        if (current.status != ArticleStatus.PUBLISHED) {
            throw ArticleValidationException("只有已发布文章可以撤回")
        }
        return withResources(repository.updateStatus(id, ArticleStatus.WITHDRAWN, current.actualPublishedAt))
    }

    @Transactional(readOnly = true)
    fun listPublic(columnId: Long?, page: Int, size: Int): PublicArticlePage {
        if (page < 0) {
            throw ArticleValidationException("页码不能小于 0")
        }
        if (size !in 1..50) {
            throw ArticleValidationException("每页数量必须在 1 到 50 之间")
        }
        columnId?.let { id ->
            if (columnQuery.find(id) == null) {
                throw ArticleValidationException("所属栏目不存在：$id")
            }
        }
        val rows = repository.findPublished(columnId, size, page * size)
        return PublicArticlePage(
            items = rows.map(::toPublicSummary),
            page = page,
            size = size,
            total = repository.countPublished(columnId),
        )
    }

    @Transactional
    fun getPublic(id: Long): PublicArticleDetail {
        if (!repository.incrementPublishedViewCount(id)) {
            throw ArticleNotFoundException(id)
        }
        val article = withResources(repository.findPublishedById(id) ?: throw ArticleNotFoundException(id))
        val column = columnQuery.find(article.columnId) ?: throw ArticleNotFoundException(id)
        return PublicArticleDetail(
            id = article.id,
            columnId = article.columnId,
            columnName = column.name,
            title = article.title,
            bodyHtml = article.bodyHtml,
            source = article.source,
            publishDate = article.publishDate,
            bodyImageResourceIds = article.bodyImageResourceIds,
            attachments = resourceAssociation.findArticleAttachments(article.id).map { resource ->
                PublicArticleAttachment(
                    id = resource.id,
                    originalFilename = resource.originalFilename,
                    contentType = resource.contentType,
                    sizeBytes = resource.sizeBytes,
                )
            },
        )
    }

    private fun normalize(draft: ArticleDraft): ArticleDraft {
        val title = draft.title.trim()
        if (title.isBlank()) {
            throw ArticleValidationException("文章标题不能为空")
        }
        if (title.length > 200) {
            throw ArticleValidationException("文章标题不能超过 200 个字符")
        }
        if (draft.source.length > 200) {
            throw ArticleValidationException("内容来源不能超过 200 个字符")
        }
        if (columnQuery.find(draft.columnId) == null) {
            throw ArticleValidationException("所属栏目不存在：${draft.columnId}")
        }
        return draft.copy(
            title = title,
            source = draft.source.trim(),
            bodyImageResourceIds = draft.bodyImageResourceIds.distinct(),
            attachmentResourceIds = draft.attachmentResourceIds.distinct(),
        )
    }

    private fun toPublicSummary(article: CmsArticle): PublicArticleSummary {
        val columnName = columnQuery.find(article.columnId)?.name ?: "栏目 #${article.columnId}"
        return PublicArticleSummary(
            id = article.id,
            columnId = article.columnId,
            columnName = columnName,
            title = article.title,
            publishDate = article.publishDate,
            pinned = article.pinned,
            recommended = article.recommended,
            sortOrder = article.sortOrder,
        )
    }

    private fun withResources(article: CmsArticle): CmsArticle {
        val links = resourceAssociation.findArticleResources(article.id)
        return article.copy(
            coverResourceId = links.coverResourceId,
            bodyImageResourceIds = links.bodyImageResourceIds,
            attachmentResourceIds = links.attachmentResourceIds,
        )
    }

    private fun ArticleDraft.toResourceLinks(): ArticleResourceLinks = ArticleResourceLinks(
        coverResourceId = coverResourceId,
        bodyImageResourceIds = bodyImageResourceIds,
        attachmentResourceIds = attachmentResourceIds,
    )
}

@Component
class ArticleColumnContentDependency(
    private val repository: ArticleRepository,
) : ColumnContentDependency {
    override fun hasContent(columnId: Long): Boolean = repository.existsByColumn(columnId)
}
