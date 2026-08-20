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
        val columns = StubColumnQuery(
            CmsColumn(7, null, "政策法规", 10, true),
        )
        val repository = InMemoryNavigationRepository()
        val service = NavigationService(repository, columns)

        service.create(
            NavigationDraft(
                name = "外部服务",
                position = NavigationPosition.MAIN,
                category = null,
                targetType = NavigationTargetType.LINK,
                targetColumnId = null,
                targetUrl = "https://example.com/service",
                sortOrder = 20,
                enabled = true,
            ),
        )
        service.create(
            NavigationDraft(
                name = "政策法规",
                position = NavigationPosition.MAIN,
                category = null,
                targetType = NavigationTargetType.COLUMN,
                targetColumnId = 7,
                targetUrl = null,
                sortOrder = 10,
                enabled = true,
            ),
        )
        service.create(
            NavigationDraft(
                name = "停用入口",
                position = NavigationPosition.MAIN,
                category = null,
                targetType = NavigationTargetType.LINK,
                targetColumnId = null,
                targetUrl = "/future/article/1",
                sortOrder = 1,
                enabled = false,
            ),
        )

        val result = service.listPublic()

        assertEquals(listOf("政策法规", "外部服务"), result.map { it.name })
        assertEquals("/columns/7", result[0].href)
        assertFalse(result[0].external)
        assertEquals("https://example.com/service", result[1].href)
        assertTrue(result[1].external)
    }

    @Test
    fun `栏目目标必须引用已有栏目`() {
        val service = NavigationService(InMemoryNavigationRepository(), StubColumnQuery())

        val error = assertThrows(NavigationValidationException::class.java) {
            service.create(
                NavigationDraft(
                    name = "不存在栏目",
                    position = NavigationPosition.MAIN,
                    category = null,
                    targetType = NavigationTargetType.COLUMN,
                    targetColumnId = 999,
                    targetUrl = null,
                    sortOrder = 0,
                    enabled = true,
                ),
            )
        }

        assertEquals("目标栏目不存在：999", error.message)
    }

    @Test
    fun `站内链接目标保留为站内路径`() {
        val service = NavigationService(InMemoryNavigationRepository(), StubColumnQuery())
        service.create(
            NavigationDraft(
                name = "未来文章",
                position = NavigationPosition.SERVICE,
                category = "快捷入口",
                targetType = NavigationTargetType.LINK,
                targetColumnId = null,
                targetUrl = "/articles/12",
                sortOrder = 0,
                enabled = true,
            ),
        )

        val item = service.listPublic().single()

        assertEquals("/articles/12", item.href)
        assertFalse(item.external)
    }
}

private class StubColumnQuery(vararg columns: CmsColumn) : ColumnQuery {
    private val data = columns.associateBy { it.id }

    override fun find(id: Long): CmsColumn? = data[id]
}

private class InMemoryNavigationRepository : NavigationRepository {
    private val data = linkedMapOf<Long, CmsNavigation>()
    private var sequence = 0L

    override fun findAll(): List<CmsNavigation> = sorted(data.values)

    override fun findEnabled(): List<CmsNavigation> = sorted(data.values.filter { it.enabled })

    override fun findById(id: Long): CmsNavigation? = data[id]

    override fun insert(draft: NavigationDraft): CmsNavigation {
        val id = ++sequence
        return draft.toModel(id).also { data[id] = it }
    }

    override fun update(id: Long, draft: NavigationDraft): CmsNavigation =
        draft.toModel(id).also { data[id] = it }

    override fun delete(id: Long) {
        data.remove(id)
    }

    private fun sorted(items: Collection<CmsNavigation>): List<CmsNavigation> = items.sortedWith(
        compareBy<CmsNavigation> { it.position.name }
            .thenBy { it.category.orEmpty() }
            .thenBy { it.sortOrder }
            .thenBy { it.id },
    )

    private fun NavigationDraft.toModel(id: Long): CmsNavigation = CmsNavigation(
        id = id,
        name = name,
        position = position,
        category = category,
        targetType = targetType,
        targetColumnId = targetColumnId,
        targetUrl = targetUrl,
        sortOrder = sortOrder,
        enabled = enabled,
    )
}
