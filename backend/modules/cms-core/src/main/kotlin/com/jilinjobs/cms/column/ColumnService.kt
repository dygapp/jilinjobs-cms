package com.jilinjobs.cms.column

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ColumnService(
    private val repository: ColumnRepository,
    private val contentDependency: ColumnContentDependency,
) : ColumnQuery {
    @Transactional(readOnly = true)
    fun list(): List<CmsColumn> = repository.findAll()

    @Transactional(readOnly = true)
    override fun find(id: Long): CmsColumn? = repository.findById(id)

    @Transactional(readOnly = true)
    override fun findByAlias(alias: String): CmsColumn? = repository.findByAlias(alias.trim().lowercase())

    @Transactional
    fun create(draft: ColumnDraft): CmsColumn {
        val normalized = normalize(draft)
        validateParent(normalized.parentId, null)
        validateAlias(normalized.alias, null)
        return repository.insert(normalized)
    }

    @Transactional
    fun update(id: Long, draft: ColumnDraft): CmsColumn {
        val current = repository.findById(id) ?: throw ColumnNotFoundException(id)
        val normalized = normalize(draft)
        if (current.preset && normalized.alias != current.alias) {
            throw ColumnValidationException("预置栏目的 Alias 属于稳定站点身份，不能修改")
        }
        validateParent(normalized.parentId, id)
        validateAlias(normalized.alias, id)
        return repository.update(id, normalized)
    }

    @Transactional
    fun delete(id: Long) {
        val current = repository.findById(id) ?: throw ColumnNotFoundException(id)
        if (current.preset) throw ColumnValidationException("预置栏目属于网站规划基线，不能删除")
        if (repository.countChildren(id) > 0) throw ColumnValidationException("栏目存在下级栏目，不能直接删除")
        if (contentDependency.hasContent(id)) throw ColumnValidationException("栏目存在内容，不能直接删除")
        repository.delete(id)
    }

    private fun normalize(draft: ColumnDraft): ColumnDraft {
        val name = draft.name.trim()
        if (name.isBlank()) throw ColumnValidationException("栏目名称不能为空")
        if (name.length > 100) throw ColumnValidationException("栏目名称不能超过 100 个字符")
        val alias = draft.alias.trim().lowercase().ifBlank { "column-${name.hashCode().toUInt()}" }
        if (!alias.matches(Regex("[a-z0-9][a-z0-9-]{0,99}"))) throw ColumnValidationException("栏目别名只能使用小写字母、数字和连字符")
        return draft.copy(name=name, alias=alias)
    }

    private fun validateAlias(alias: String, currentId: Long?) {
        val existing = repository.findByAlias(alias)
        if (existing != null && existing.id != currentId) throw ColumnValidationException("栏目别名已存在：$alias")
    }

    private fun validateParent(parentId: Long?, currentId: Long?) {
        var cursor = parentId
        while (cursor != null) {
            if (currentId != null && cursor == currentId) throw ColumnValidationException("栏目不能设置为自身或自身下级栏目的子栏目")
            val parent = repository.findById(cursor) ?: throw ColumnValidationException("上级栏目不存在：$cursor")
            cursor = parent.parentId
        }
    }
}
