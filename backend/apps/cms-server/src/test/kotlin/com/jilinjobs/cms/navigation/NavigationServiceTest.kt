package com.jilinjobs.cms.navigation

import com.jilinjobs.cms.column.CmsColumn
import com.jilinjobs.cms.column.ColumnQuery
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class NavigationServiceTest {
    @Test
    fun `公开导航只返回启用条目并解析栏目与外部目标`() {
        val columns = StubColumnQuery(CmsColumn(7, null, "政策法规", 10, true))
        val repository = InMemoryNavigationRepository()
        val service = NavigationService(repository, columns, FakeNavigationLocationMapper("MAIN"))
        service.create(NavigationDraft("外部服务", "MAIN", null, NavigationTargetType.LINK, null, "https://example.com/service", 20, true))
        service.create(NavigationDraft("政策法规", "MAIN", null, NavigationTargetType.COLUMN, 7, null, 10, true))
        service.create(NavigationDraft("停用入口", "MAIN", null, NavigationTargetType.LINK, null, "/future/article/1", 1, false))
        val result = service.listPublic()
        assertEquals(listOf("政策法规", "外部服务"), result.map { it.name })
        assertEquals("/columns/7", result[0].href)
        assertFalse(result[0].external)
        assertEquals("https://example.com/service", result[1].href)
        assertTrue(result[1].external)
    }

    @Test fun `导航图标作为条目属性公开输出`() { val service = NavigationService(InMemoryNavigationRepository(), StubColumnQuery(), FakeNavigationLocationMapper("HOME_SHORTCUT")); service.create(NavigationDraft("快捷入口", "HOME_SHORTCUT", null, NavigationTargetType.LINK, null, "/service", 10, true, iconPath = "/static/icons/top-nav-01.png")); assertEquals("/static/icons/top-nav-01.png", service.listPublic().single().iconPath) }
    @Test fun `导航图标必须引用静态资源路径`() { val service = NavigationService(InMemoryNavigationRepository(), StubColumnQuery(), FakeNavigationLocationMapper("HOME_SHORTCUT")); assertThrows(NavigationValidationException::class.java) { service.create(NavigationDraft("快捷入口", "HOME_SHORTCUT", null, NavigationTargetType.LINK, null, "/service", 10, true, iconPath = "https://example.com/icon.png")) } }
    @Test fun `栏目目标必须引用已有栏目`() { val service = NavigationService(InMemoryNavigationRepository(), StubColumnQuery(), FakeNavigationLocationMapper("MAIN")); val error = assertThrows(NavigationValidationException::class.java) { service.create(NavigationDraft("不存在栏目", "MAIN", null, NavigationTargetType.COLUMN, 999, null, 0, true)) }; assertEquals("目标栏目不存在：999", error.message) }
    @Test fun `站内链接目标保留为站内路径`() { val service = NavigationService(InMemoryNavigationRepository(), StubColumnQuery(), FakeNavigationLocationMapper("HOME_SHORTCUT")); service.create(NavigationDraft("未来文章", "HOME_SHORTCUT", null, NavigationTargetType.LINK, null, "/articles/12", 0, true)); val item = service.listPublic().single(); assertEquals("/articles/12", item.href); assertFalse(item.external) }

    @Test
    fun `父子导航必须属于同一位置`() {
        val repository = InMemoryNavigationRepository()
        val locations = FakeNavigationLocationMapper("MAIN", "HOME_SHORTCUT")
        val service = NavigationService(repository, StubColumnQuery(), locations)
        val parent = service.create(NavigationDraft("主导航", "MAIN", null, NavigationTargetType.PLACEHOLDER, null, null, 0, true))
        assertThrows(NavigationValidationException::class.java) { service.create(NavigationDraft("跨位置子项", "HOME_SHORTCUT", null, NavigationTargetType.LINK, null, "/x", 0, true, parentId = parent.id)) }
    }

    @Test
    fun `预置导航不能删除但仍可修改运营属性`() {
        val repository = InMemoryNavigationRepository()
        val preset = repository.seedPreset("网站首页", "MAIN")
        val service = NavigationService(repository, StubColumnQuery(), FakeNavigationLocationMapper("MAIN"))
        val error = assertThrows(NavigationValidationException::class.java) { service.delete(preset.id) }
        assertEquals("预置导航属于网站规划基线，不能删除", error.message)
        val updated = service.update(preset.id, NavigationDraft("首页", "MAIN", null, NavigationTargetType.HOME, null, null, 20, false))
        assertEquals("首页", updated.name)
        assertEquals(false, updated.enabled)
        assertEquals(true, updated.preset)
    }
}

private class StubColumnQuery(vararg columns: CmsColumn) : ColumnQuery { private val data = columns.associateBy { it.id }; override fun find(id: Long): CmsColumn? = data[id] }

private class InMemoryNavigationRepository : NavigationRepository {
    private val data = linkedMapOf<Long, CmsNavigation>()
    private var sequence = 0L
    fun seedPreset(name: String, position: String): CmsNavigation { val id = ++sequence; return CmsNavigation(id, name, position, null, NavigationTargetType.HOME, null, null, 10, true, preset = true).also { data[id] = it } }
    override fun findAll(): List<CmsNavigation> = sorted(data.values)
    override fun findEnabled(): List<CmsNavigation> = sorted(data.values.filter { it.enabled })
    override fun findById(id: Long): CmsNavigation? = data[id]
    override fun insert(draft: NavigationDraft): CmsNavigation { val id = ++sequence; return draft.toModel(id, false).also { data[id] = it } }
    override fun update(id: Long, draft: NavigationDraft): CmsNavigation = draft.toModel(id, data[id]?.preset ?: false).also { data[id] = it }
    override fun delete(id: Long) { data.remove(id) }
    private fun sorted(items: Collection<CmsNavigation>) = items.sortedWith(compareBy<CmsNavigation> { it.position }.thenBy { it.sortOrder }.thenBy { it.id })
    private fun NavigationDraft.toModel(id: Long, preset: Boolean) = CmsNavigation(id, name, position, category, targetType, targetColumnId, targetUrl, sortOrder, enabled, parentId, targetPageId, openMode, iconPath, preset)
}

private class FakeNavigationLocationMapper(vararg codes: String) : NavigationLocationMapper {
    private val rows = codes.mapIndexed { index, code -> NavigationLocationRecord((index + 1).toLong(), code, code, "", index * 10, true, true) }.associateBy { it.code }.toMutableMap()
    override fun findAll() = rows.values.toList()
    override fun findByCode(code: String) = rows[code]
    override fun insert(record: NavigationLocationRecord): Int { rows[record.code] = record.copy(id = (rows.size + 1).toLong()); return 1 }
    override fun update(record: NavigationLocationRecord): Int { val current = rows[record.code] ?: return 0; rows[record.code] = record.copy(id = current.id, preset = current.preset); return 1 }
    override fun delete(code: String): Int = if (rows.remove(code) != null) 1 else 0
}
