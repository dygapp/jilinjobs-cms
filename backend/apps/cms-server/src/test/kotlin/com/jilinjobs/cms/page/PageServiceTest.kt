package com.jilinjobs.cms.page

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PageServiceTest {
    @Test fun `rejects external url for internal static page`() { val service = PageService(FakePageMapper()); val error = assertThrows(PageValidationException::class.java) { service.createPage(PageDraft(null, "special", "特殊页面", "", PageRenderMode.INTERNAL_STATIC, "https://example.com/page", 0, true)) }; assertTrue(error.message!!.contains("本站路径")) }
    @Test fun `keeps alias unique within the same page group`() { val mapper = FakePageMapper(); val service = PageService(mapper); val guide = service.createGroup(PageGroupDraft("guide", "业务指南")); service.createPage(PageDraft(guide.id, "contact", "联系我们")); assertThrows(PageValidationException::class.java) { service.createPage(PageDraft(guide.id, "contact", "重复页面")) } }
    @Test fun `public page group contains only enabled members in configured order`() { val service = PageService(FakePageMapper()); val group = service.createGroup(PageGroupDraft("guide", "业务指南")); service.createPage(PageDraft(group.id, "second", "第二项", sortOrder = 20, enabled = true)); service.createPage(PageDraft(group.id, "hidden", "停用项", sortOrder = 5, enabled = false)); service.createPage(PageDraft(group.id, "first", "第一项", sortOrder = 10, enabled = true)); val publicGroup = service.getPublicGroup("guide"); assertEquals(listOf("first", "second"), publicGroup.members.map { it.alias }) }

    @Test
    fun `预置单页分组不能修改 Alias`() {
        val mapper = FakePageMapper()
        val group = mapper.seedPresetGroup("guide", "业务指南")
        val service = PageService(mapper)
        val error = assertThrows(PageValidationException::class.java) { service.updateGroup(group.id!!, PageGroupDraft("guide-new", "业务指南", 20, false)) }
        assertEquals("预置单页分组的 Alias 属于稳定站点身份，不能修改", error.message)
        val updated = service.updateGroup(group.id!!, PageGroupDraft("guide", "业务指南（调整）", 20, false))
        assertEquals(true, updated.preset)
        assertEquals("业务指南（调整）", updated.name)
    }

    @Test
    fun `预置单页不能删除或修改 Alias`() {
        val mapper = FakePageMapper()
        val page = mapper.seedPresetPage(null, "about", "关于我们")
        val service = PageService(mapper)
        val deleteError = assertThrows(PageValidationException::class.java) { service.deletePage(page.id!!) }
        assertEquals("预置单页属于网站规划基线，不能删除", deleteError.message)
        val aliasError = assertThrows(PageValidationException::class.java) { service.updatePage(page.id!!, PageDraft(null, "about-new", "关于我们")) }
        assertEquals("预置单页的 Alias 属于稳定站点身份，不能修改", aliasError.message)
        val updated = service.updatePage(page.id!!, PageDraft(null, "about", "关于我们（调整）", sortOrder = 30, enabled = false))
        assertEquals(true, updated.preset)
        assertEquals(false, updated.enabled)
    }
}

private class FakePageMapper : PageMapper {
    private val groups = mutableListOf<PageGroupRecord>()
    private val pages = mutableListOf<PageRecord>()
    private var nextGroupId = 1L
    private var nextPageId = 1L

    fun seedPresetGroup(alias: String, name: String): PageGroupRecord = PageGroupRecord(nextGroupId++, alias, name, 10, true, true).also { groups += it.copy() }
    fun seedPresetPage(groupId: Long?, alias: String, name: String): PageRecord = PageRecord(nextPageId++, groupId, alias, name, "", PageRenderMode.RICH_TEXT.name, null, 10, true, true).also { pages += it.copy() }

    override fun findGroups(): List<PageGroupRecord> = groups.sortedWith(compareBy<PageGroupRecord> { it.sortOrder }.thenBy { it.id })
    override fun findGroupById(id: Long): PageGroupRecord? = groups.find { it.id == id }
    override fun findGroupByAlias(alias: String): PageGroupRecord? = groups.find { it.alias == alias }
    override fun insertGroup(record: PageGroupRecord): Int { record.id = nextGroupId++; groups += record.copy(); return 1 }
    override fun updateGroup(record: PageGroupRecord): Int { val index = groups.indexOfFirst { it.id == record.id }; if (index < 0) return 0; groups[index] = record.copy(preset = groups[index].preset); return 1 }
    override fun findPages(): List<PageRecord> = pages.sortedWith(compareBy<PageRecord> { it.groupId ?: 0 }.thenBy { it.sortOrder }.thenBy { it.id })
    override fun findPageById(id: Long): PageRecord? = pages.find { it.id == id }
    override fun findStandalone(alias: String): PageRecord? = pages.find { it.groupId == null && it.alias == alias }
    override fun findGrouped(groupAlias: String, alias: String): PageRecord? { val groupId = groups.find { it.alias == groupAlias }?.id ?: return null; return pages.find { it.groupId == groupId && it.alias == alias } }
    override fun findByGroup(groupId: Long): List<PageRecord> = pages.filter { it.groupId == groupId }.sortedWith(compareBy<PageRecord> { it.sortOrder }.thenBy { it.id })
    override fun insertPage(record: PageRecord): Int { record.id = nextPageId++; pages += record.copy(); return 1 }
    override fun updatePage(record: PageRecord): Int { val index = pages.indexOfFirst { it.id == record.id }; if (index < 0) return 0; pages[index] = record.copy(preset = pages[index].preset); return 1 }
    override fun deletePage(id: Long): Int { val index = pages.indexOfFirst { it.id == id }; if (index < 0) return 0; pages.removeAt(index); return 1 }
}
