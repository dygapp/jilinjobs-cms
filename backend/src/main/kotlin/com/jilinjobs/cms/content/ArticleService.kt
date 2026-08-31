package com.jilinjobs.cms.content

import com.jilinjobs.cms.column.ColumnContentDependency
import com.jilinjobs.cms.column.ColumnQuery
import com.jilinjobs.cms.common.ContentImagePolicy
import com.jilinjobs.cms.resource.ArticleResourceAssociation
import com.jilinjobs.cms.resource.ArticleResourceLinks
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.net.URI
import java.time.LocalDateTime

@Service
class ArticleService(
    private val repository: ArticleRepository,
    private val columnQuery: ColumnQuery,
    private val resourceAssociation: ArticleResourceAssociation,
) {
    @Transactional(readOnly = true)
    fun list() = repository.findAll().map(::withResources)

    @Transactional(readOnly = true)
    fun get(id: Long) = withResources(repository.findById(id) ?: throw ArticleNotFoundException(id))

    @Transactional
    fun create(draft: ArticleDraft): CmsArticle {
        val normalized = normalize(draft)
        val article = repository.insert(normalized)
        resourceAssociation.replaceArticleResources(article.id, normalized.links())
        return withResources(article)
    }

    @Transactional
    fun update(id: Long, draft: ArticleDraft): CmsArticle {
        repository.findById(id) ?: throw ArticleNotFoundException(id)
        val normalized = normalize(draft)
        val article = repository.update(id, normalized)
        resourceAssociation.replaceArticleResources(id, normalized.links())
        return withResources(article)
    }

    @Transactional
    fun publish(id: Long): CmsArticle {
        val article = withResources(repository.findById(id) ?: throw ArticleNotFoundException(id))
        if (article.status == ArticleStatus.PUBLISHED) throw ArticleValidationException("文章已经处于已发布状态")
        val column = columnQuery.find(article.columnId) ?: throw ArticleValidationException("所属栏目不存在：${article.columnId}")
        if (article.articleType == ArticleType.INTERNAL && column.coverPolicy == ContentImagePolicy.REQUIRED && article.coverResourceId == null) {
            throw ArticleValidationException("当前栏目要求文章设置封面图片，补充封面后才能发布")
        }
        return withResources(repository.updateStatus(id, ArticleStatus.PUBLISHED, LocalDateTime.now()))
    }

    @Transactional
    fun withdraw(id: Long): CmsArticle {
        val article = repository.findById(id) ?: throw ArticleNotFoundException(id)
        if (article.status != ArticleStatus.PUBLISHED) throw ArticleValidationException("只有已发布文章可以撤回")
        return withResources(repository.updateStatus(id, ArticleStatus.WITHDRAWN, article.actualPublishedAt))
    }

    @Transactional(readOnly = true)
    fun listPublic(columnId: Long?, page: Int, size: Int): PublicArticlePage {
        if (page < 0) throw ArticleValidationException("页码不能小于 0")
        if (size !in 1..50) throw ArticleValidationException("每页数量必须在 1 到 50 之间")
        columnId?.let { if (columnQuery.find(it) == null) throw ArticleValidationException("所属栏目不存在：$it") }
        val rows = repository.findPublished(columnId, size, page * size)
        return PublicArticlePage(rows.map(::summary), page, size, repository.countPublished(columnId))
    }

    @Transactional
    fun getPublic(id: Long): PublicArticleDetail {
        if (!repository.incrementPublishedViewCount(id)) throw ArticleNotFoundException(id)
        val article = withResources(repository.findPublishedById(id) ?: throw ArticleNotFoundException(id))
        val column = columnQuery.find(article.columnId) ?: throw ArticleNotFoundException(id)
        return PublicArticleDetail(
            article.id,
            article.columnId,
            column.name,
            article.title,
            article.bodyHtml,
            article.source,
            article.publishDate,
            article.bodyImageResourceIds,
            resourceAssociation.findArticleAttachments(article.id).map {
                PublicArticleAttachment(it.id, it.originalFilename, it.contentType, it.sizeBytes)
            },
            column.alias,
            article.articleType,
            article.externalUrl,
        )
    }

    private fun normalize(draft: ArticleDraft): ArticleDraft {
        val title = draft.title.trim()
        if (title.isBlank()) throw ArticleValidationException("文章标题不能为空")
        if (title.length > 200) throw ArticleValidationException("文章标题不能超过 200 个字符")
        if (draft.source.length > 200) throw ArticleValidationException("内容来源不能超过 200 个字符")
        val column = columnQuery.find(draft.columnId) ?: throw ArticleValidationException("所属栏目不存在：${draft.columnId}")

        val externalUrl = draft.externalUrl?.trim()?.takeIf { it.isNotEmpty() }
        if (draft.articleType == ArticleType.EXTERNAL_LINK) {
            if (externalUrl == null) throw ArticleValidationException("外链文章必须填写原文链接")
            val uri = runCatching { URI(externalUrl) }.getOrNull()
            if (uri == null || uri.scheme !in setOf("http", "https") || uri.host.isNullOrBlank()) {
                throw ArticleValidationException("原文链接必须是有效的 http/https 地址")
            }
            return draft.copy(
                title = title,
                source = draft.source.trim(),
                externalUrl = externalUrl,
                bodyHtml = "",
                coverResourceId = null,
                bodyImageResourceIds = emptyList(),
                attachmentResourceIds = emptyList(),
            )
        }

        if (column.coverPolicy == ContentImagePolicy.NONE && draft.coverResourceId != null) {
            throw ArticleValidationException("当前栏目不使用文章封面图片")
        }

        return draft.copy(
            title = title,
            source = draft.source.trim(),
            externalUrl = null,
            bodyImageResourceIds = draft.bodyImageResourceIds.distinct(),
            attachmentResourceIds = draft.attachmentResourceIds.distinct(),
        )
    }

    private fun summary(article: CmsArticle): PublicArticleSummary {
        val column = columnQuery.find(article.columnId)
        return PublicArticleSummary(
            article.id,
            article.columnId,
            column?.name ?: "栏目 #${article.columnId}",
            article.title,
            article.publishDate,
            article.pinned,
            article.recommended,
            article.sortOrder,
            column?.alias.orEmpty(),
            article.source,
            article.articleType,
            article.externalUrl,
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

    private fun ArticleDraft.links() = ArticleResourceLinks(coverResourceId, bodyImageResourceIds, attachmentResourceIds)
}

@Component
class ArticleColumnContentDependency(private val repository: ArticleRepository) : ColumnContentDependency {
    override fun hasContent(columnId: Long) = repository.existsByColumn(columnId)
}
