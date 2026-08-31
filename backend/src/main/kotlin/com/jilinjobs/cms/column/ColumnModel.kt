package com.jilinjobs.cms.column

import com.jilinjobs.cms.common.ContentImagePolicy

data class CmsColumn(
    val id: Long,
    val parentId: Long?,
    val name: String,
    val sortOrder: Int,
    val enabled: Boolean,
    val alias: String = "",
    val coverPolicy: ContentImagePolicy = ContentImagePolicy.OPTIONAL,
)

data class ColumnDraft(
    val parentId: Long?,
    val name: String,
    val sortOrder: Int,
    val enabled: Boolean,
    val alias: String = "",
    val coverPolicy: ContentImagePolicy = ContentImagePolicy.OPTIONAL,
)

interface ColumnQuery {
    fun find(id: Long): CmsColumn?
    fun findByAlias(alias: String): CmsColumn? = null
}

interface ColumnContentDependency {
    fun hasContent(columnId: Long): Boolean
}

class ColumnValidationException(message: String) : RuntimeException(message)
class ColumnNotFoundException(id: Long) : RuntimeException("栏目不存在：$id")
class ColumnAliasNotFoundException(alias: String) : RuntimeException("栏目不存在：$alias")
