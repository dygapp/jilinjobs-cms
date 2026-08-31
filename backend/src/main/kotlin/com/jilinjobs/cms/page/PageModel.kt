package com.jilinjobs.cms.page

enum class PageRenderMode { RICH_TEXT, EMBED_PLACEHOLDER, INTERNAL_STATIC }

data class CmsPageGroup(val id: Long, val alias: String, val name: String, val sortOrder: Int, val enabled: Boolean, val preset: Boolean = false)
data class PageGroupDraft(val alias: String, val name: String, val sortOrder: Int = 0, val enabled: Boolean = true)

data class CmsPage(
    val id: Long,
    val groupId: Long?,
    val alias: String,
    val name: String,
    val bodyHtml: String,
    val renderMode: PageRenderMode,
    val embedUrl: String?,
    val sortOrder: Int,
    val enabled: Boolean,
    val preset: Boolean = false,
)

data class PageDraft(
    val groupId: Long?,
    val alias: String,
    val name: String,
    val bodyHtml: String = "",
    val renderMode: PageRenderMode = PageRenderMode.RICH_TEXT,
    val embedUrl: String? = null,
    val sortOrder: Int = 0,
    val enabled: Boolean = true,
)

data class PublicPageMember(val alias: String, val name: String, val href: String, val sortOrder: Int)
data class PublicPageGroup(val alias: String, val name: String, val members: List<PublicPageMember>)
data class BreadcrumbItem(val title: String, val href: String? = null)
data class PublicPage(
    val id: Long,
    val alias: String,
    val name: String,
    val bodyHtml: String,
    val renderMode: PageRenderMode,
    val embedUrl: String?,
    val canonicalUrl: String,
    val group: PublicPageGroup?,
    val breadcrumbs: List<BreadcrumbItem>,
)

interface PageLookup {
    fun pathForPage(id: Long): String?
}
object EmptyPageLookup : PageLookup { override fun pathForPage(id: Long): String? = null }

class PageValidationException(message: String) : RuntimeException(message)
class PageNotFoundException(message: String) : RuntimeException(message)
