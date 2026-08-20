package com.jilinjobs.cms.navigation

import com.jilinjobs.cms.column.ColumnQuery
import java.net.URI
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NavigationService(
    private val repository: NavigationRepository,
    private val columns: ColumnQuery,
) {
    @Transactional(readOnly = true)
    fun listAdmin(): List<CmsNavigation> = repository.findAll()

    @Transactional(readOnly = true)
    fun listPublic(): List<PublicNavigation> = repository.findEnabled().map { it.toPublic() }

    @Transactional
    fun create(draft: NavigationDraft): CmsNavigation = repository.insert(normalize(draft))

    @Transactional
    fun update(id: Long, draft: NavigationDraft): CmsNavigation {
        repository.findById(id) ?: throw NavigationNotFoundException(id)
        return repository.update(id, normalize(draft))
    }

    @Transactional
    fun delete(id: Long) {
        repository.findById(id) ?: throw NavigationNotFoundException(id)
        repository.delete(id)
    }

    private fun normalize(draft: NavigationDraft): NavigationDraft {
        val name = draft.name.trim()
        if (name.isBlank()) {
            throw NavigationValidationException("导航名称不能为空")
        }
        if (name.length > 100) {
            throw NavigationValidationException("导航名称不能超过 100 个字符")
        }

        val category = draft.category?.trim()?.takeIf { it.isNotBlank() }
        if (category != null && category.length > 100) {
            throw NavigationValidationException("导航类别不能超过 100 个字符")
        }

        return when (draft.targetType) {
            NavigationTargetType.COLUMN -> {
                val columnId = draft.targetColumnId
                    ?: throw NavigationValidationException("栏目目标必须选择栏目")
                if (columns.find(columnId) == null) {
                    throw NavigationValidationException("目标栏目不存在：$columnId")
                }
                draft.copy(
                    name = name,
                    category = category,
                    targetColumnId = columnId,
                    targetUrl = null,
                )
            }

            NavigationTargetType.LINK -> draft.copy(
                name = name,
                category = category,
                targetColumnId = null,
                targetUrl = normalizeLink(draft.targetUrl),
            )
        }
    }

    private fun normalizeLink(raw: String?): String {
        val value = raw?.trim().orEmpty()
        if (value.isBlank()) {
            throw NavigationValidationException("链接目标不能为空")
        }
        if (value.length > 1000) {
            throw NavigationValidationException("链接地址不能超过 1000 个字符")
        }
        if (value.startsWith("/") && !value.startsWith("//")) {
            return value
        }

        val uri = runCatching { URI(value) }
            .getOrElse { throw NavigationValidationException("链接地址格式不正确") }
        val scheme = uri.scheme?.lowercase()
        if (scheme !in setOf("http", "https") || uri.host.isNullOrBlank()) {
            throw NavigationValidationException("链接地址必须是站内路径或 HTTP(S) 地址")
        }
        return value
    }

    private fun CmsNavigation.toPublic(): PublicNavigation {
        val href = when (targetType) {
            NavigationTargetType.COLUMN -> "/columns/${requireNotNull(targetColumnId)}"
            NavigationTargetType.LINK -> requireNotNull(targetUrl)
        }
        return PublicNavigation(
            id = id,
            name = name,
            position = position,
            category = category,
            sortOrder = sortOrder,
            targetType = targetType,
            href = href,
            external = href.startsWith("http://") || href.startsWith("https://"),
        )
    }
}
