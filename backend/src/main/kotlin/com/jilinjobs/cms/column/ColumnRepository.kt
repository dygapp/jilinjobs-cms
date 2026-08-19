package com.jilinjobs.cms.column

interface ColumnRepository {
    fun findAll(): List<CmsColumn>

    fun findById(id: Long): CmsColumn?

    fun insert(draft: ColumnDraft): CmsColumn

    fun update(id: Long, draft: ColumnDraft): CmsColumn

    fun delete(id: Long)

    fun countChildren(id: Long): Long
}
