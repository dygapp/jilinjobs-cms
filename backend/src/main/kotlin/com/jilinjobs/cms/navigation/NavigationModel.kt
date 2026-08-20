package com.jilinjobs.cms.navigation

enum class NavigationPosition {
    MAIN,
    SERVICE,
    SITE,
}

enum class NavigationTargetType {
    COLUMN,
    LINK,
}

data class CmsNavigation(
    val id: Long,
    val name: String,
    val position: NavigationPosition,
    val category: String?,
    val targetType: NavigationTargetType,
    val targetColumnId: Long?,
    val targetUrl: String?,
    val sortOrder: Int,
    val enabled: Boolean,
)

data class NavigationDraft(
    val name: String,
    val position: NavigationPosition,
    val category: String?,
    val targetType: NavigationTargetType,
    val targetColumnId: Long?,
    val targetUrl: String?,
    val sortOrder: Int,
    val enabled: Boolean,
)

data class PublicNavigation(
    val id: Long,
    val name: String,
    val position: NavigationPosition,
    val category: String?,
    val sortOrder: Int,
    val targetType: NavigationTargetType,
    val href: String,
    val external: Boolean,
)

class NavigationValidationException(message: String) : RuntimeException(message)

class NavigationNotFoundException(id: Long) : RuntimeException("导航条目不存在：$id")
