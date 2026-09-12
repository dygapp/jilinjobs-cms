package com.jilinjobs.cms.page

enum class PageRenderMode { RICH_TEXT, EMBED_PLACEHOLDER, INTERNAL_STATIC }
enum class PageContentModel { RICH_TEXT, STRUCTURED, NONE }
enum class PageContentOwner { OPERATOR, SITE_PACKAGE, ENGINEERING, EXTERNAL }

object PageRendererKey {
    const val RICH_TEXT = "RICH_TEXT"
    const val EMBED_PLACEHOLDER = "EMBED_PLACEHOLDER"
    const val INTERNAL_STATIC = "INTERNAL_STATIC"
    const val JILINJOBS_GUIDE_CARDS = "JILINJOBS_GUIDE_CARDS"

    fun legacyMode(key: String): PageRenderMode? = runCatching { PageRenderMode.valueOf(key) }.getOrNull()
}

data class PageStructuredCard(
    val title: String = "",
    val bodyHtml: String = "",
)

data class PageStructuredContent(
    val schemaVersion: Int = 1,
    val kind: String = "CARD_COLLECTION",
    val items: List<PageStructuredCard> = emptyList(),
)

data class CmsPageGroup(val id: Long, val alias: String, val name: String, val sortOrder: Int, val enabled: Boolean, val preset: Boolean = false)
data class PageGroupDraft(val alias: String, val name: String, val sortOrder: Int = 0, val enabled: Boolean = true)

data class CmsPage(
    val id: Long,
    val groupId: Long?,
    val alias: String,
    val name: String,
    val bodyHtml: String,
    val contentModel: PageContentModel,
    val rendererKey: String,
    val contentOwner: PageContentOwner,
    val structuredContent: PageStructuredContent?,
    val renderMode: PageRenderMode?,
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
    val contentModel: PageContentModel? = null,
    val rendererKey: String? = null,
    val contentOwner: PageContentOwner? = null,
    val structuredContent: PageStructuredContent? = null,
    /** Compatibility input for clients created before EU-55. */
    val renderMode: PageRenderMode? = null,
    val embedUrl: String? = null,
    val sortOrder: Int = 0,
    val enabled: Boolean = true,
)

data class PageContentDraft(
    val bodyHtml: String = "",
    val structuredContent: PageStructuredContent? = null,
    /** Compatibility input. Contract switching remains forbidden for preset Pages. */
    val renderMode: PageRenderMode? = null,
    val embedUrl: String? = null,
)

data class PublicPageMember(val alias: String, val name: String, val href: String, val sortOrder: Int)
data class PublicPageGroup(val alias: String, val name: String, val members: List<PublicPageMember>)
data class BreadcrumbItem(val title: String, val href: String? = null)
data class PublicPage(
    val id: Long,
    val alias: String,
    val name: String,
    val bodyHtml: String,
    val contentModel: PageContentModel,
    val rendererKey: String,
    val contentOwner: PageContentOwner,
    val structuredContent: PageStructuredContent?,
    /** Deprecated compatibility projection for pre-EU-55 Rich / placeholder / internal clients. */
    val renderMode: PageRenderMode?,
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
