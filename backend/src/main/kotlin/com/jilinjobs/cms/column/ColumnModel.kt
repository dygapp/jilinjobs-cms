package com.jilinjobs.cms.column

data class CmsColumn(
    val id: Long,
    val parentId: Long?,
    val name: String,
    val sortOrder: Int,
    val enabled: Boolean,
)

data class ColumnDraft(
    val parentId: Long?,
    val name: String,
    val sortOrder: Int,
    val enabled: Boolean,
)

interface ColumnQuery {
    fun find(id: Long): CmsColumn?
}

interface ColumnContentDependency {
    fun hasContent(columnId: Long): Boolean
}

class ColumnValidationException(message: String) : RuntimeException(message)

class ColumnNotFoundException(id: Long) : RuntimeException("栏目不存在：$id")
