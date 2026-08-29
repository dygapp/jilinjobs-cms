package com.jilinjobs.cms.navigation

import com.jilinjobs.cms.column.ColumnQuery
import com.jilinjobs.cms.page.EmptyPageLookup
import com.jilinjobs.cms.page.PageLookup
import java.net.URI
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NavigationService(
    private val repository: NavigationRepository,
    private val columns: ColumnQuery,
    private val locations: NavigationLocationMapper,
    private val pages: PageLookup = EmptyPageLookup,
) {
    @Transactional(readOnly = true) fun listAdmin() = repository.findAll()
    @Transactional(readOnly = true) fun listPublic() = repository.findEnabled().map { it.toPublic() }
    @Transactional fun create(draft: NavigationDraft) = repository.insert(normalize(draft, null))

    @Transactional
    fun update(id: Long, draft: NavigationDraft): CmsNavigation {
        repository.findById(id) ?: throw NavigationNotFoundException(id)
        return repository.update(id, normalize(draft, id))
    }

    @Transactional
    fun delete(id: Long) {
        repository.findById(id) ?: throw NavigationNotFoundException(id)
        if (repository.findAll().any { it.parentId == id }) throw NavigationValidationException("导航存在下级菜单，不能直接删除")
        repository.delete(id)
    }

    private fun normalize(draft: NavigationDraft, currentId: Long?): NavigationDraft {
        val name = draft.name.trim()
        if (name.isBlank()) throw NavigationValidationException("导航名称不能为空")
        if (name.length > 100) throw NavigationValidationException("导航名称不能超过 100 个字符")
        val position = draft.position.trim().uppercase()
        if (locations.findByCode(position) == null) throw NavigationValidationException("导航位置不存在：$position")
        validateParent(draft.parentId, currentId, position)
        val base = draft.copy(name = name, position = position, category = draft.category?.trim()?.takeIf { it.isNotBlank() })
        return when (draft.targetType) {
            NavigationTargetType.HOME, NavigationTargetType.PLACEHOLDER -> base.copy(targetColumnId = null, targetPageId = null, targetUrl = null)
            NavigationTargetType.COLUMN -> {
                val id = draft.targetColumnId ?: throw NavigationValidationException("栏目目标必须选择栏目")
                if (columns.find(id) == null) throw NavigationValidationException("目标栏目不存在：$id")
                base.copy(targetColumnId = id, targetPageId = null, targetUrl = null)
            }
            NavigationTargetType.PAGE -> {
                val id = draft.targetPageId ?: throw NavigationValidationException("固定页面目标必须选择页面")
                if (pages.pathForPage(id) == null) throw NavigationValidationException("目标固定页面不存在：$id")
                base.copy(targetColumnId = null, targetPageId = id, targetUrl = null)
            }
            NavigationTargetType.LINK -> base.copy(targetColumnId = null, targetPageId = null, targetUrl = normalizeLink(draft.targetUrl))
        }
    }

    private fun validateParent(parentId: Long?, currentId: Long?, position: String) {
        var cursor = parentId
        while (cursor != null) {
            if (currentId != null && cursor == currentId) throw NavigationValidationException("导航不能设置为自身或自身下级的子菜单")
            val parent = repository.findById(cursor) ?: throw NavigationValidationException("上级导航不存在：$cursor")
            if (parent.position != position) throw NavigationValidationException("上级导航必须与当前导航属于同一位置")
            cursor = parent.parentId
        }
    }

    private fun normalizeLink(raw: String?): String {
        val value = raw?.trim().orEmpty()
        if (value.isBlank()) throw NavigationValidationException("链接目标不能为空")
        if (value.length > 1000) throw NavigationValidationException("链接地址不能超过 1000 个字符")
        if (value.startsWith("/") && !value.startsWith("//")) return value
        val uri = runCatching { URI(value) }.getOrElse { throw NavigationValidationException("链接地址格式不正确") }
        if (uri.scheme?.lowercase() !in setOf("http", "https") || uri.host.isNullOrBlank()) throw NavigationValidationException("链接地址必须是站内路径或 HTTP(S) 地址")
        return value
    }

    private fun CmsNavigation.toPublic(): PublicNavigation {
        val href = when (targetType) {
            NavigationTargetType.HOME -> "/"
            NavigationTargetType.COLUMN -> {
                val column = columns.find(requireNotNull(targetColumnId))
                if (column?.alias.isNullOrBlank() || column!!.alias.startsWith("column-")) "/columns/$targetColumnId" else "/column/${column.alias}"
            }
            NavigationTargetType.PAGE -> pages.pathForPage(requireNotNull(targetPageId)) ?: "#"
            NavigationTargetType.LINK -> requireNotNull(targetUrl)
            NavigationTargetType.PLACEHOLDER -> "#"
        }
        val external = href.startsWith("http://") || href.startsWith("https://")
        val newWindow = when (openMode) {
            NavigationOpenMode.NEW_WINDOW -> true
            NavigationOpenMode.SAME_WINDOW -> false
            NavigationOpenMode.DEFAULT -> external
        }
        return PublicNavigation(id, name, position, category, sortOrder, targetType, href, external, parentId, newWindow, targetType != NavigationTargetType.PLACEHOLDER)
    }
}
