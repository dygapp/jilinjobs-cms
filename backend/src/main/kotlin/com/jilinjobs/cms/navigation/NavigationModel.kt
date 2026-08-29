package com.jilinjobs.cms.navigation

enum class NavigationTargetType { HOME, COLUMN, PAGE, LINK, PLACEHOLDER }
enum class NavigationOpenMode { DEFAULT, SAME_WINDOW, NEW_WINDOW }

data class CmsNavigation(
    val id: Long,
    val name: String,
    val position: String,
    val category: String?,
    val targetType: NavigationTargetType,
    val targetColumnId: Long?,
    val targetUrl: String?,
    val sortOrder: Int,
    val enabled: Boolean,
    val parentId: Long? = null,
    val targetPageId: Long? = null,
    val openMode: NavigationOpenMode = NavigationOpenMode.DEFAULT,
)

data class NavigationDraft(
    val name: String,
    val position: String,
    val category: String?,
    val targetType: NavigationTargetType,
    val targetColumnId: Long?,
    val targetUrl: String?,
    val sortOrder: Int,
    val enabled: Boolean,
    val parentId: Long? = null,
    val targetPageId: Long? = null,
    val openMode: NavigationOpenMode = NavigationOpenMode.DEFAULT,
)

data class PublicNavigation(
    val id: Long,
    val name: String,
    val position: String,
    val category: String?,
    val sortOrder: Int,
    val targetType: NavigationTargetType,
    val href: String,
    val external: Boolean,
    val parentId: Long? = null,
    val newWindow: Boolean = false,
    val clickable: Boolean = true,
)

class NavigationValidationException(message: String) : RuntimeException(message)
class NavigationNotFoundException(id: Long) : RuntimeException("导航条目不存在：$id")
