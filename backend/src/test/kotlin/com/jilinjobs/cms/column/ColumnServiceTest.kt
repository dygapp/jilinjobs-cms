package com.jilinjobs.cms.column

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class ColumnServiceTest {
    @Test
    fun `存在子栏目时拒绝删除父栏目`() {
        val repository = InMemoryColumnRepository()
        val service = ColumnService(repository, FixedColumnContentDependency(false))
        val parent = service.create(ColumnDraft(null, "父栏目", 10, true))
        service.create(ColumnDraft(parent.id, "子栏目", 10, true))

        val error = assertThrows(ColumnValidationException::class.java) {
            service.delete(parent.id)
        }

        assertEquals("栏目存在下级栏目，不能直接删除", error.message)
    }

    @Test
    fun `存在文章内容时拒绝删除栏目`() {
        val repository = InMemoryColumnRepository()
        val service = ColumnService(repository, FixedColumnContentDependency(true))
        val column = service.create(ColumnDraft(null, "内容栏目", 10, true))

        val error = assertThrows(ColumnValidationException::class.java) {
            service.delete(column.id)
        }

        assertEquals("栏目存在内容，不能直接删除", error.message)
    }

    @Test
    fun `编辑栏目时拒绝形成层级环`() {
        val repository = InMemoryColumnRepository()
        val service = ColumnService(repository, FixedColumnContentDependency(false))
        val parent = service.create(ColumnDraft(null, "父栏目", 10, true))
        val child = service.create(ColumnDraft(parent.id, "子栏目", 10, true))

        assertThrows(ColumnValidationException::class.java) {
            service.update(parent.id, ColumnDraft(child.id, "父栏目", 10, true))
        }
    }

    @Test
    fun `栏目属性可以更新并持久保留`() {
        val repository = InMemoryColumnRepository()
        val service = ColumnService(repository, FixedColumnContentDependency(false))
        val created = service.create(ColumnDraft(null, "栏目", 10, true))

        val updated = service.update(created.id, ColumnDraft(null, "更新栏目", 20, false))

        assertEquals("更新栏目", updated.name)
        assertEquals(20, updated.sortOrder)
        assertEquals(false, updated.enabled)
    }
}

private class FixedColumnContentDependency(
    private val hasContent: Boolean,
) : ColumnContentDependency {
    override fun hasContent(columnId: Long): Boolean = hasContent
}

private class InMemoryColumnRepository : ColumnRepository {
    private val data = linkedMapOf<Long, CmsColumn>()
    private var sequence = 0L

    override fun findAll(): List<CmsColumn> = data.values.sortedWith(
        compareBy<CmsColumn> { it.parentId ?: 0L }
            .thenBy { it.sortOrder }
            .thenBy { it.id },
    )

    override fun findById(id: Long): CmsColumn? = data[id]

    override fun insert(draft: ColumnDraft): CmsColumn {
        val id = ++sequence
        return CmsColumn(id, draft.parentId, draft.name, draft.sortOrder, draft.enabled)
            .also { data[id] = it }
    }

    override fun update(id: Long, draft: ColumnDraft): CmsColumn =
        CmsColumn(id, draft.parentId, draft.name, draft.sortOrder, draft.enabled)
            .also { data[id] = it }

    override fun delete(id: Long) {
        data.remove(id)
    }

    override fun countChildren(id: Long): Long = data.values.count { it.parentId == id }.toLong()
}
