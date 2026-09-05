package com.jilinjobs.cms.common

import com.jilinjobs.cms.column.CmsColumn
import com.jilinjobs.cms.column.ColumnQuery
import com.jilinjobs.cms.content.ArticleDraft
import com.jilinjobs.cms.content.ArticleNotFoundException
import com.jilinjobs.cms.content.ArticleRepository
import com.jilinjobs.cms.content.ArticleService
import com.jilinjobs.cms.content.ArticleStatus
import com.jilinjobs.cms.content.ArticleType
import com.jilinjobs.cms.content.CmsArticle
import com.jilinjobs.cms.page.PageGroupRecord
import com.jilinjobs.cms.page.PageMapper
import com.jilinjobs.cms.page.PageRecord
import com.jilinjobs.cms.page.PageRenderMode
import com.jilinjobs.cms.page.PageService
import com.jilinjobs.cms.resource.ArticleResourceAssociation
import com.jilinjobs.cms.resource.ArticleResourceLinks
import com.jilinjobs.cms.resource.CmsResource
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class RichTextLegacyReadDefenseTest {
    @Test
    fun `public article defensively sanitizes legacy stored html without rewriting persistence`() {
        val raw = legacyHostileHtml()
        val repository = LegacyArticleRepository(raw)
        val service = ArticleService(repository, LegacyColumnQuery, EmptyArticleResources)

        val public = service.getPublic(1)

        assertSafeLegacyOutput(public.bodyHtml)
        assertEquals(raw, repository.stored.bodyHtml)
        assertEquals(0, repository.contentWriteCount)
        assertEquals(1L, repository.stored.viewCount)
    }

    @Test
    fun `public rich text page defensively sanitizes legacy stored html without rewriting persistence`() {
        val raw = legacyHostileHtml()
        val mapper = LegacyPageMapper(raw)
        val service = PageService(mapper)

        val public = service.getPublicStandalone("legacy")

        assertSafeLegacyOutput(public.bodyHtml)
        assertEquals(raw, mapper.stored.bodyHtml)
        assertEquals(0, mapper.updateCount)
    }

    private fun legacyHostileHtml() = """
        <p style="text-align:center"><span style="font-family:宋体;font-size:16px;color:#333333;background-color:#ffffff"><strong>党建历史正文</strong></span></p>
        <script>alert(1)</script>
        <a href="javascript:alert(2)" onclick="alert(3)">危险链接</a>
        <iframe src="https://example.com"></iframe>
    """.trimIndent()

    private fun assertSafeLegacyOutput(html: String) {
        assertTrue(html.contains("党建历史正文"))
        assertTrue(html.contains("<strong>"))
        assertTrue(html.contains("font-family"))
        assertTrue(html.contains("font-size"))
        assertTrue(html.contains("color"))
        assertTrue(html.contains("background-color"))
        assertTrue(html.contains("text-align"))
        assertFalse(html.contains("<script"))
        assertFalse(html.contains("javascript:"))
        assertFalse(html.contains("onclick"))
        assertFalse(html.contains("<iframe"))
    }
}

private object LegacyColumnQuery : ColumnQuery {
    override fun find(id: Long): CmsColumn? = CmsColumn(id, null, "存量栏目", 0, true)
}

private object EmptyArticleResources : ArticleResourceAssociation {
    override fun findArticleResources(articleId: Long) = ArticleResourceLinks()
    override fun findArticleAttachments(articleId: Long): List<CmsResource> = emptyList()
    override fun replaceArticleResources(articleId: Long, links: ArticleResourceLinks) = Unit
}

private class LegacyArticleRepository(rawBodyHtml: String) : ArticleRepository {
    var stored = CmsArticle(
        id = 1,
        columnId = 1,
        title = "存量文章",
        bodyHtml = rawBodyHtml,
        source = "历史迁移",
        articleType = ArticleType.INTERNAL,
        externalUrl = null,
        publishDate = null,
        pinned = false,
        sortOrder = 0,
        status = ArticleStatus.PUBLISHED,
        actualPublishedAt = LocalDateTime.of(2026, 1, 1, 0, 0),
        viewCount = 0,
        updatedAt = LocalDateTime.of(2026, 1, 1, 0, 0),
    )
    var contentWriteCount = 0

    override fun findAll() = listOf(stored)
    override fun findById(id: Long) = stored.takeIf { it.id == id }
    override fun insert(draft: ArticleDraft): CmsArticle = error("not used")
    override fun update(id: Long, draft: ArticleDraft): CmsArticle {
        contentWriteCount += 1
        error("public legacy read must not rewrite persistence")
    }
    override fun updateStatus(id: Long, status: ArticleStatus, actualPublishedAt: LocalDateTime?): CmsArticle = error("not used")
    override fun findPublished(columnId: Long?, limit: Int, offset: Int) = listOf(stored).filter { columnId == null || it.columnId == columnId }
    override fun countPublished(columnId: Long?) = if (columnId == null || stored.columnId == columnId) 1L else 0L
    override fun findPublishedById(id: Long) = stored.takeIf { it.id == id && it.status == ArticleStatus.PUBLISHED }
    override fun incrementPublishedViewCount(id: Long): Boolean {
        if (stored.id != id || stored.status != ArticleStatus.PUBLISHED) return false
        stored = stored.copy(viewCount = stored.viewCount + 1)
        return true
    }
    override fun existsByColumn(columnId: Long) = stored.columnId == columnId
}

private class LegacyPageMapper(rawBodyHtml: String) : PageMapper {
    val stored = PageRecord(
        id = 1,
        groupId = null,
        alias = "legacy",
        name = "存量单页",
        bodyHtml = rawBodyHtml,
        renderMode = PageRenderMode.RICH_TEXT.name,
        embedUrl = null,
        sortOrder = 0,
        enabled = true,
        preset = true,
    )
    var updateCount = 0

    override fun findGroups(): List<PageGroupRecord> = emptyList()
    override fun findGroupById(id: Long): PageGroupRecord? = null
    override fun findGroupByAlias(alias: String): PageGroupRecord? = null
    override fun insertGroup(record: PageGroupRecord): Int = error("not used")
    override fun updateGroup(record: PageGroupRecord): Int = error("not used")
    override fun findPages() = listOf(stored)
    override fun findPageById(id: Long) = stored.takeIf { it.id == id }
    override fun findStandalone(alias: String) = stored.takeIf { it.alias == alias }
    override fun findGrouped(groupAlias: String, alias: String): PageRecord? = null
    override fun findByGroup(groupId: Long): List<PageRecord> = emptyList()
    override fun insertPage(record: PageRecord): Int = error("not used")
    override fun updatePage(record: PageRecord): Int {
        updateCount += 1
        error("public legacy read must not rewrite persistence")
    }
    override fun deletePage(id: Long): Int = error("not used")
}
