package com.jilinjobs.cms.content

import com.jilinjobs.cms.column.ColumnContentDependency
import com.jilinjobs.cms.column.ColumnQuery
import com.jilinjobs.cms.resource.ArticleResourceAssociation
import com.jilinjobs.cms.resource.ArticleResourceLinks
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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
