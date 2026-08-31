package com.jilinjobs.cms.content

import com.jilinjobs.cms.column.CmsColumn
import com.jilinjobs.cms.column.ColumnQuery
import com.jilinjobs.cms.common.ContentImagePolicy
import com.jilinjobs.cms.resource.ArticleResourceAssociation
import com.jilinjobs.cms.resource.ArticleResourceLinks
import com.jilinjobs.cms.resource.CmsResource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
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
        assertEquals(ArticleType.INTERNAL, created.articleType)
        assertNull(created.externalUrl)
        assertEquals(11L, created.coverResourceId)
        assertEquals(listOf(12L), created.bodyImageResourceIds)
        assertEquals(listOf(13L, 14L), created.attachmentResourceIds)
    }

    @Test
    fun `外链文章只保存基础信息和原文链接`() {
        val repository = InMemoryArticleRepository()
        val resources = InMemoryArticleResourceAssociation()
        val service = ArticleService(repository, FixedColumnQuery(), resources)

        val created = service.create(
            sampleDraft().copy(
                articleType = ArticleType.EXTERNAL_LINK,
                externalUrl = "https://example.com/jobs/1",
            ),
        )
        service.publish(created.id)
        val summary = service.listPublic(1, 0, 10).items.single()

        assertEquals(ArticleType.EXTERNAL_LINK, created.articleType)
        assertEquals("https://example.com/jobs/1", created.externalUrl)
        assertEquals("", created.bodyHtml)
        assertNull(created.coverResourceId)
        assertEquals(emptyList<Long>(), created.bodyImageResourceIds)
        assertEquals(emptyList<Long>(), created.attachmentResourceIds)
        assertEquals("https://example.com/jobs/1", summary.externalUrl)
        assertEquals("吉林就业", summary.source)
    }

    @Test
    fun `外链文章必须提供有效 http 地址`() {
        val service = ArticleService(InMemoryArticleRepository(), FixedColumnQuery(), InMemoryArticleResourceAssociation())
        assertThrows(ArticleValidationException::class.java) {
            service.create(sampleDraft().copy(articleType = ArticleType.EXTERNAL_LINK, externalUrl = null))
        }
        assertThrows(ArticleValidationException::class.java) {
            service.create(sampleDraft().copy(articleType = ArticleType.EXTERNAL_LINK, externalUrl = "javascript:alert(1)"))
        }
    }

    @Test
    fun `招聘公告栏目同时支持站内和外链文章`() {
        val service = ArticleService(InMemoryArticleRepository(), RecruitmentColumnQuery(), InMemoryArticleResourceAssociation())

        val internal = service.create(sampleDraft())
        val external = service.create(
            sampleDraft().copy(
                title = "外链招聘公告",
                articleType = ArticleType.EXTERNAL_LINK,
                externalUrl = "https://example.com/recruitment/1",
            ),
        )

        assertEquals(ArticleType.INTERNAL, internal.articleType)
        assertNull(internal.externalUrl)
        assertEquals(ArticleType.EXTERNAL_LINK, external.articleType)
        assertEquals("https://example.com/recruitment/1", external.externalUrl)
    }

    @Test
    fun `编辑文章不会改变当前发布状态`() {
        val repository = InMemoryArticleRepository()
        val resources = InMemoryArticleResourceAssociation()
        val existing = repository.insert(sampleDraft())
        repository.updateStatus(existing.id, ArticleStatus.PUBLISHED, LocalDateTime.now())
        val service = ArticleService(repository, FixedColumnQuery(), resources)

        val updated = service.update(existing.id, sampleDraft().copy(title = "更新后的标题"))

        assertEquals("更新后的标题", updated.title)
        assertEquals(ArticleStatus.PUBLISHED, updated.status)
        assertNotNull(updated.actualPublishedAt)
    }

    @Test
    fun `封面必填栏目允许无封面草稿但阻止发布`() {
        val service = ArticleService(InMemoryArticleRepository(), RequiredCoverColumnQuery(), InMemoryArticleResourceAssociation())
        val created = service.create(sampleDraft().copy(coverResourceId = null))

        assertEquals(ArticleStatus.DRAFT, created.status)
        assertNull(created.coverResourceId)
        assertThrows(ArticleValidationException::class.java) { service.publish(created.id) }
    }

    @Test
    fun `已发布文章不能编辑为违反封面必填契约的状态`() {
        val repository = InMemoryArticleRepository()
        val resources = InMemoryArticleResourceAssociation()
        val service = ArticleService(repository, RequiredCoverColumnQuery(), resources)
        val created = service.create(sampleDraft())
        service.publish(created.id)

        assertThrows(ArticleValidationException::class.java) {
            service.update(created.id, sampleDraft().copy(title = "移除封面的编辑", coverResourceId = null))
        }

        val persisted = service.get(created.id)
        assertEquals(ArticleStatus.PUBLISHED, persisted.status)
        assertEquals(11L, persisted.coverResourceId)
    }

    @Test
    fun `不使用封面的栏目拒绝保存封面数据`() {
        val service = ArticleService(InMemoryArticleRepository(), NoCoverColumnQuery(), InMemoryArticleResourceAssociation())
        assertThrows(ArticleValidationException::class.java) { service.create(sampleDraft()) }
        val created = service.create(sampleDraft().copy(coverResourceId = null))
        assertNull(created.coverResourceId)
    }

    @Test
    fun `公开文章摘要携带封面资源引用`() {
        val service = ArticleService(InMemoryArticleRepository(), FixedColumnQuery(), InMemoryArticleResourceAssociation())
        val created = service.create(sampleDraft())
        service.publish(created.id)

        val summary = service.listPublic(1, 0, 10).items.single()
        assertEquals(11L, summary.coverResourceId)
    }

    @Test
    fun `文章只允许通过显式状态操作发布撤回并重新发布`() {
        val repository = InMemoryArticleRepository()
        val resources = InMemoryArticleResourceAssociation()
        val service = ArticleService(repository, FixedColumnQuery(), resources)
        val created = service.create(sampleDraft())

        val published = service.publish(created.id)
        assertEquals(ArticleStatus.PUBLISHED, published.status)
        assertNotNull(published.actualPublishedAt)
        assertThrows(ArticleValidationException::class.java) { service.publish(created.id) }

        val withdrawn = service.withdraw(created.id)
        assertEquals(ArticleStatus.WITHDRAWN, withdrawn.status)
        assertEquals(published.actualPublishedAt, withdrawn.actualPublishedAt)
        assertThrows(ArticleValidationException::class.java) { service.withdraw(created.id) }

        val republished = service.publish(created.id)
        assertEquals(ArticleStatus.PUBLISHED, republished.status)
        assertNotNull(republished.actualPublishedAt)
    }

    @Test
    fun `公开查询只返回已发布文章且撤回后详情不可访问`() {
        val repository = InMemoryArticleRepository()
        val resources = InMemoryArticleResourceAssociation()
        val service = ArticleService(repository, FixedColumnQuery(), resources)
        val draft = service.create(sampleDraft().copy(title = "草稿文章"))
        val published = service.create(sampleDraft().copy(title = "公开文章"))
        service.publish(published.id)

        val page = service.listPublic(columnId = 1, page = 0, size = 10)
        assertEquals(1, page.total)
        assertEquals(listOf("公开文章"), page.items.map { it.title })
        val detail = service.getPublic(published.id)
        assertEquals("栏目 1", detail.columnName)
        assertEquals(listOf("附件-13", "附件-14"), detail.attachments.map { it.originalFilename })
        assertEquals(1L, repository.findById(published.id)?.viewCount)
        assertThrows(ArticleNotFoundException::class.java) { service.getPublic(draft.id) }
        assertEquals(0L, repository.findById(draft.id)?.viewCount)

        service.withdraw(published.id)
        assertEquals(0, service.listPublic(columnId = 1, page = 0, size = 10).total)
        assertThrows(ArticleNotFoundException::class.java) { service.getPublic(published.id) }
    }

    private fun sampleDraft(): ArticleDraft = ArticleDraft(
        columnId = 1,
        title = "测试文章",
        bodyHtml = "<p>正文</p><img src=\"/api/admin/resources/12/content\">",
        source = "吉林就业",
        articleType = ArticleType.INTERNAL,
        externalUrl = null,
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
    override fun find(id: Long): CmsColumn? = CmsColumn(id, null, "栏目 $id", 0, true)
}

private class RecruitmentColumnQuery : ColumnQuery {
    override fun find(id: Long): CmsColumn? = CmsColumn(id, null, "招聘公告", 0, true, "recruitment-announcement")
}

private class RequiredCoverColumnQuery : ColumnQuery {
    override fun find(id: Long): CmsColumn? = CmsColumn(id, null, "图片必填栏目", 0, true, "required-cover", ContentImagePolicy.REQUIRED)
}

private class NoCoverColumnQuery : ColumnQuery {
    override fun find(id: Long): CmsColumn? = CmsColumn(id, null, "无封面栏目", 0, true, "no-cover", ContentImagePolicy.NONE)
}

private class InMemoryArticleResourceAssociation : ArticleResourceAssociation {
    private val links = mutableMapOf<Long, ArticleResourceLinks>()
    override fun findArticleResources(articleId: Long): ArticleResourceLinks = links[articleId] ?: ArticleResourceLinks()
    override fun findArticleAttachments(articleId: Long): List<CmsResource> =
        findArticleResources(articleId).attachmentResourceIds.map { id -> CmsResource(id, "resource-$id", "附件-$id", "application/octet-stream", id) }
    override fun replaceArticleResources(articleId: Long, links: ArticleResourceLinks) { this.links[articleId] = links }
}

private class InMemoryArticleRepository : ArticleRepository {
    private val data = linkedMapOf<Long, CmsArticle>()
    private var sequence = 0L
    override fun findAll(): List<CmsArticle> = data.values.toList()
    override fun findById(id: Long): CmsArticle? = data[id]
    override fun insert(draft: ArticleDraft): CmsArticle {
        val id = ++sequence
        val article = draft.toArticle(id, ArticleStatus.DRAFT, null)
        data[id] = article
        return article
    }
    override fun update(id: Long, draft: ArticleDraft): CmsArticle {
        val current = data[id] ?: throw ArticleNotFoundException(id)
        val article = draft.toArticle(id, current.status, current.actualPublishedAt)
        data[id] = article
        return article
    }
    override fun updateStatus(id: Long, status: ArticleStatus, actualPublishedAt: LocalDateTime?): CmsArticle {
        val current = data[id] ?: throw ArticleNotFoundException(id)
        val updated = current.copy(status = status, actualPublishedAt = actualPublishedAt, updatedAt = LocalDateTime.now())
        data[id] = updated
        return updated
    }
    override fun findPublished(columnId: Long?, limit: Int, offset: Int): List<CmsArticle> = data.values.filter { it.status == ArticleStatus.PUBLISHED && (columnId == null || it.columnId == columnId) }.drop(offset).take(limit)
    override fun countPublished(columnId: Long?): Long = data.values.count { it.status == ArticleStatus.PUBLISHED && (columnId == null || it.columnId == columnId) }.toLong()
    override fun findPublishedById(id: Long): CmsArticle? = data[id]?.takeIf { it.status == ArticleStatus.PUBLISHED }
    override fun incrementPublishedViewCount(id: Long): Boolean {
        val current = findPublishedById(id) ?: return false
        data[id] = current.copy(viewCount = current.viewCount + 1)
        return true
    }
    override fun existsByColumn(columnId: Long): Boolean = data.values.any { it.columnId == columnId }

    private fun ArticleDraft.toArticle(id: Long, status: ArticleStatus, actualPublishedAt: LocalDateTime?): CmsArticle = CmsArticle(
        id = id,
        columnId = columnId,
        title = title,
        bodyHtml = bodyHtml,
        source = source,
        articleType = articleType,
        externalUrl = externalUrl,
        publishDate = publishDate,
        pinned = pinned,
        recommended = recommended,
        sortOrder = sortOrder,
        status = status,
        actualPublishedAt = actualPublishedAt,
        viewCount = 0,
        updatedAt = LocalDateTime.now(),
    )
}
