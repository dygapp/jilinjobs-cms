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

    @Transactional
    fun create(draft: ColumnDraft): CmsColumn {
        val normalized = normalize(draft)
        validateParent(normalized.parentId, currentId = null)
        return repository.insert(normalized)
    }

    @Transactional
    fun update(id: Long, draft: ColumnDraft): CmsColumn {
        repository.findById(id) ?: throw ColumnNotFoundException(id)
        val normalized = normalize(draft)
        validateParent(normalized.parentId, currentId = id)
        return repository.update(id, normalized)
    }

    @Transactional
    fun delete(id: Long) {
        repository.findById(id) ?: throw ColumnNotFoundException(id)
        if (repository.countChildren(id) > 0) {
            throw ColumnValidationException("栏目存在下级栏目，不能直接删除")
        }
        if (contentDependency.hasContent(id)) {
            throw ColumnValidationException("栏目存在内容，不能直接删除")
        }
        repository.delete(id)
    }

    private fun normalize(draft: ColumnDraft): ColumnDraft {
        val name = draft.name.trim()
        if (name.isBlank()) {
            throw ColumnValidationException("栏目名称不能为空")
        }
        if (name.length > 100) {
            throw ColumnValidationException("栏目名称不能超过 100 个字符")
        }
        return draft.copy(name = name)
    }

    private fun validateParent(parentId: Long?, currentId: Long?) {
        var cursor = parentId
        while (cursor != null) {
            if (currentId != null && cursor == currentId) {
                throw ColumnValidationException("栏目不能设置为自身或自身下级栏目的子栏目")
            }
            val parent = repository.findById(cursor)
                ?: throw ColumnValidationException("上级栏目不存在：$cursor")
            cursor = parent.parentId
        }
    }
}
