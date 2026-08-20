package com.jilinjobs.cms.content

import com.jilinjobs.cms.column.CmsColumn
import com.jilinjobs.cms.column.ColumnQuery
import com.jilinjobs.cms.resource.ArticleResourceAssociation
import com.jilinjobs.cms.resource.ArticleResourceLinks
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

class ArticleServiceTest {
    @Test
    fun `新文章始终以草稿状态保存并保留资源关联`() {
        val repository = InMemoryArticleRepository()
        val resources = InMemoryArticleResourceAssociation()
        val service = ArticleService(repository, FixedColumnQuery(), resources)

        val created = service.create(sampleDraft())

        assertEquals(ArticleStatus.DRAFT, created.status)
        assertEquals(11L, created.coverResourceId)
        assertEquals(listOf(12L), created.bodyImageResourceIds)
        assertEquals(listOf(13L, 14L), created.attachmentResourceIds)
    }

    @Test
    fun `编辑文章不会改变当前发布状态`() {
        val repository = InMemoryArticleRepository()
        val resources = InMemoryArticleResourceAssociation()
        val existing = repository.insert(sampleDraft())
        repository.forceStatus(existing.id, ArticleStatus.PUBLISHED)
        val service = ArticleService(repository, FixedColumnQuery(), resources)

        val updated = service.update(existing.id, sampleDraft().copy(title = "更新后的标题"))

        assertEquals("更新后的标题", updated.title)
        assertEquals(ArticleStatus.PUBLISHED, updated.status)
    }

    private fun sampleDraft(): ArticleDraft = ArticleDraft(
        columnId = 1,
        title = "测试文章",
        bodyHtml = "<p>正文</p><img src=\"/api/admin/resources/12/content\">",
        source = "吉林就业",
        publishDate = LocalDate.of(2026, 8, 20),
        pinned = true,
        recommended = true,
        sortOrder = 20,
        coverResourceId = 11,
        bodyImageResourceIds = listOf(12),
        attachmentResourceIds = listOf(13, 14),
    )
}

private class FixedColumnQuery : ColumnQuery {
    override fun find(id: Long): CmsColumn? = CmsColumn(id, null, "栏目", 0, true)
}

private class InMemoryArticleResourceAssociation : ArticleResourceAssociation {
    private val links = mutableMapOf<Long, ArticleResourceLinks>()

    override fun findArticleResources(articleId: Long): ArticleResourceLinks = links[articleId] ?: ArticleResourceLinks()

    override fun replaceArticleResources(articleId: Long, links: ArticleResourceLinks) {
        this.links[articleId] = links
    }
}

private class InMemoryArticleRepository : ArticleRepository {
    private val data = linkedMapOf<Long, CmsArticle>()
    private var sequence = 0L

    override fun findAll(): List<CmsArticle> = data.values.toList()

    override fun findById(id: Long): CmsArticle? = data[id]

    override fun insert(draft: ArticleDraft): CmsArticle {
        val id = ++sequence
        val article = draft.toArticle(id, ArticleStatus.DRAFT)
        data[id] = article
        return article
    }

    override fun update(id: Long, draft: ArticleDraft): CmsArticle {
        val status = data[id]?.status ?: throw ArticleNotFoundException(id)
        val article = draft.toArticle(id, status)
        data[id] = article
        return article
    }

    override fun existsByColumn(columnId: Long): Boolean = data.values.any { it.columnId == columnId }

    fun forceStatus(id: Long, status: ArticleStatus) {
        data[id] = requireNotNull(data[id]).copy(status = status)
    }

    private fun ArticleDraft.toArticle(id: Long, status: ArticleStatus): CmsArticle = CmsArticle(
        id = id,
        columnId = columnId,
        title = title,
        bodyHtml = bodyHtml,
        source = source,
        publishDate = publishDate,
        pinned = pinned,
        recommended = recommended,
        sortOrder = sortOrder,
        status = status,
        actualPublishedAt = if (status == ArticleStatus.PUBLISHED) LocalDateTime.now() else null,
        viewCount = 0,
        updatedAt = LocalDateTime.now(),
    )
}
