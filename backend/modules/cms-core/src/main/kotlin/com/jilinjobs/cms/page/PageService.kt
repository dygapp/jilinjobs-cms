package com.jilinjobs.cms.page

import com.jilinjobs.cms.common.RichTextHtmlPolicy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import tools.jackson.databind.ObjectMapper

private val RENDERER_KEY = Regex("[A-Z0-9][A-Z0-9_-]{0,99}")
private const val CARD_COLLECTION = "CARD_COLLECTION"
private const val CARD_COLLECTION_SCHEMA_VERSION = 1

@Service
class PageService(
    private val mapper: PageMapper,
    private val objectMapper: ObjectMapper,
) : PageLookup {
    @Transactional(readOnly=true) fun listGroups(): List<CmsPageGroup> = mapper.findGroups().map { it.model() }
    @Transactional(readOnly=true) fun listPages(): List<CmsPage> = mapper.findPages().map { it.model() }

    @Transactional fun createGroup(draft: PageGroupDraft): CmsPageGroup {
        val d=normalize(draft); if(mapper.findGroupByAlias(d.alias)!=null) throw PageValidationException("单页分组别名已存在：${d.alias}")
        val r=PageGroupRecord(alias=d.alias,name=d.name,sortOrder=d.sortOrder,enabled=d.enabled); mapper.insertGroup(r); return mapper.findGroupById(requireNotNull(r.id))!!.model()
    }
    @Transactional fun updateGroup(id:Long,draft:PageGroupDraft):CmsPageGroup {
        val current=mapper.findGroupById(id)?:throw PageNotFoundException("单页分组不存在：$id"); val d=normalize(draft)
        if(current.preset&&d.alias!=current.alias)throw PageValidationException("预置单页分组的 Alias 属于稳定站点身份，不能修改")
        mapper.findGroupByAlias(d.alias)?.let { if(it.id!=id) throw PageValidationException("单页分组别名已存在：${d.alias}") }
        val r=PageGroupRecord(id,d.alias,d.name,d.sortOrder,d.enabled); mapper.updateGroup(r); return mapper.findGroupById(id)!!.model()
    }
    @Transactional fun createPage(draft:PageDraft):CmsPage { val d=normalize(draft,null); val r=d.record(); mapper.insertPage(r); return mapper.findPageById(requireNotNull(r.id))!!.model() }
    @Transactional fun updatePage(id:Long,draft:PageDraft):CmsPage {
        val current=mapper.findPageById(id)?:throw PageNotFoundException("单页不存在：$id"); val d=normalize(draft,id)
        if(current.preset&&d.alias!=current.alias)throw PageValidationException("预置单页的 Alias 属于稳定站点身份，不能修改")
        if(current.preset && !current.sameContract(d)) throw PageValidationException("预置单页的 Content Model / Renderer / Ownership 属于站点规划契约，不能通过普通编辑修改")
        val r=d.record(id); mapper.updatePage(r); return mapper.findPageById(id)!!.model()
    }
    @Transactional fun updateContent(id:Long,draft:PageContentDraft):CmsPage {
        val current=mapper.findPageById(id)?:throw PageNotFoundException("单页不存在：$id")
        val currentModel=current.model()
        if (draft.renderMode != null && draft.renderMode != currentModel.renderMode) {
            throw PageValidationException("正文编辑不能切换 Renderer contract")
        }
        val content = normalizeContent(
            contentModel = currentModel.contentModel,
            rendererKey = currentModel.rendererKey,
            contentOwner = currentModel.contentOwner,
            bodyHtml = draft.bodyHtml,
            structuredContent = draft.structuredContent,
            embedUrl = draft.embedUrl,
        )
        mapper.updatePageContent(id, content.bodyHtml, content.structuredPayload, content.embedUrl)
        return mapper.findPageById(id)!!.model()
    }
    @Transactional fun deletePage(id:Long) { val current=mapper.findPageById(id)?:throw PageNotFoundException("单页不存在：$id"); if(current.preset)throw PageValidationException("预置单页属于网站规划基线，不能删除"); mapper.deletePage(id) }

    @Transactional(readOnly=true)
    fun getPublicStandalone(alias:String):PublicPage { val page=mapper.findStandalone(alias.lowercase())?.model()?.takeIf{it.enabled}?:throw PageNotFoundException("单页不存在或已停用：$alias"); return public(page,null) }

    @Transactional(readOnly=true)
    fun getPublicGrouped(groupAlias:String,alias:String):PublicPage {
        val group=mapper.findGroupByAlias(groupAlias.lowercase())?.model()?.takeIf{it.enabled}?:throw PageNotFoundException("单页分组不存在或已停用：$groupAlias")
        val page=mapper.findGrouped(group.alias,alias.lowercase())?.model()?.takeIf{it.enabled}?:throw PageNotFoundException("单页不存在或已停用：$groupAlias/$alias")
        return public(page,group)
    }

    @Transactional(readOnly=true)
    fun getPublicGroup(groupAlias:String):PublicPageGroup {
        val group=mapper.findGroupByAlias(groupAlias.lowercase())?.model()?.takeIf{it.enabled}?:throw PageNotFoundException("单页分组不存在或已停用：$groupAlias")
        return group.publicGroup()
    }

    @Transactional(readOnly=true)
    override fun pathForPage(id:Long):String? {
        val p=mapper.findPageById(id)?.model()?:return null
        val g=p.groupId?.let(mapper::findGroupById)?.model()
        return if(g==null) "/page/${p.alias}" else "/page/${g.alias}/${p.alias}"
    }

    private fun public(page:CmsPage,group:CmsPageGroup?):PublicPage {
        val url=if(group==null) "/page/${page.alias}" else "/page/${group.alias}/${page.alias}"
        val publicGroup=group?.publicGroup()
        val crumbs=buildList { add(BreadcrumbItem("首页","/")); if(group!=null) add(BreadcrumbItem(group.name)); add(BreadcrumbItem(page.name,url)) }
        val publicBodyHtml = if (page.contentModel == PageContentModel.RICH_TEXT) RichTextHtmlPolicy.sanitize(page.bodyHtml) else page.bodyHtml
        val publicStructuredContent = page.structuredContent?.let(::sanitizeStructured)
        return PublicPage(
            page.id,
            page.alias,
            page.name,
            publicBodyHtml,
            page.contentModel,
            page.rendererKey,
            page.contentOwner,
            publicStructuredContent,
            page.renderMode,
            page.embedUrl,
            url,
            publicGroup,
            crumbs,
        )
    }

    private fun CmsPageGroup.publicGroup():PublicPageGroup = PublicPageGroup(alias,name,mapper.findByGroup(id).map{it.model()}.filter{it.enabled}.map{ PublicPageMember(it.alias,it.name,"/page/$alias/${it.alias}",it.sortOrder) })

    private fun normalize(d:PageGroupDraft):PageGroupDraft { val a=alias(d.alias); val n=d.name.trim(); if(n.isBlank())throw PageValidationException("单页分组名称不能为空"); return d.copy(alias=a,name=n) }
    private fun normalize(d:PageDraft,currentId:Long?):NormalizedPageDraft {
        val a=alias(d.alias); val n=d.name.trim(); if(n.isBlank())throw PageValidationException("单页名称不能为空")
        d.groupId?.let { mapper.findGroupById(it)?:throw PageValidationException("单页分组不存在：$it") }
        mapper.findPages().map{it.model()}.firstOrNull{it.groupId==d.groupId&&it.alias==a&&it.id!=currentId}?.let { throw PageValidationException("单页别名已存在：$a") }
        val contract=resolveContract(d)
        val content=normalizeContent(contract.contentModel,contract.rendererKey,contract.contentOwner,d.bodyHtml,d.structuredContent,d.embedUrl)
        return NormalizedPageDraft(
            groupId=d.groupId,
            alias=a,
            name=n,
            bodyHtml=content.bodyHtml,
            contentModel=contract.contentModel,
            rendererKey=contract.rendererKey,
            contentOwner=contract.contentOwner,
            structuredContent=content.structuredContent,
            structuredPayload=content.structuredPayload,
            embedUrl=content.embedUrl,
            sortOrder=d.sortOrder,
            enabled=d.enabled,
        )
    }

    private fun resolveContract(d:PageDraft):PageContract {
        val hasNewContract = d.contentModel != null || d.rendererKey != null || d.contentOwner != null
        if (!hasNewContract) return legacyContract(d.renderMode ?: PageRenderMode.RICH_TEXT)
        val contentModel=d.contentModel?:throw PageValidationException("contentModel / rendererKey / contentOwner 必须一起声明")
        val rendererKey=d.rendererKey?.trim()?.takeIf{it.isNotBlank()}?:throw PageValidationException("contentModel / rendererKey / contentOwner 必须一起声明")
        val contentOwner=d.contentOwner?:throw PageValidationException("contentModel / rendererKey / contentOwner 必须一起声明")
        if (!rendererKey.matches(RENDERER_KEY)) throw PageValidationException("rendererKey 不合法")
        d.renderMode?.let { legacy ->
            if (PageRendererKey.legacyMode(rendererKey) != legacy) throw PageValidationException("legacy renderMode 与 rendererKey 冲突")
        }
        validateContract(contentModel,rendererKey,contentOwner)
        return PageContract(contentModel,rendererKey,contentOwner)
    }

    private fun legacyContract(mode:PageRenderMode):PageContract = when(mode) {
        PageRenderMode.RICH_TEXT -> PageContract(PageContentModel.RICH_TEXT,PageRendererKey.RICH_TEXT,PageContentOwner.OPERATOR)
        PageRenderMode.EMBED_PLACEHOLDER -> PageContract(PageContentModel.NONE,PageRendererKey.EMBED_PLACEHOLDER,PageContentOwner.EXTERNAL)
        PageRenderMode.INTERNAL_STATIC -> PageContract(PageContentModel.NONE,PageRendererKey.INTERNAL_STATIC,PageContentOwner.ENGINEERING)
    }

    private fun validateContract(contentModel:PageContentModel,rendererKey:String,contentOwner:PageContentOwner) {
        when(contentModel) {
            PageContentModel.RICH_TEXT -> if(rendererKey!=PageRendererKey.RICH_TEXT || contentOwner!=PageContentOwner.OPERATOR) throw PageValidationException("RICH_TEXT 必须使用 generic Rich renderer 且由 OPERATOR 持有正文")
            PageContentModel.STRUCTURED -> if(contentOwner!=PageContentOwner.OPERATOR || rendererKey in setOf(PageRendererKey.RICH_TEXT,PageRendererKey.EMBED_PLACEHOLDER,PageRendererKey.INTERNAL_STATIC)) throw PageValidationException("STRUCTURED 必须使用显式 Structured renderer 且由 OPERATOR 持有正文")
            PageContentModel.NONE -> {
                val valid=(rendererKey==PageRendererKey.EMBED_PLACEHOLDER&&contentOwner==PageContentOwner.EXTERNAL)||(rendererKey==PageRendererKey.INTERNAL_STATIC&&contentOwner==PageContentOwner.ENGINEERING)
                if(!valid) throw PageValidationException("NONE content model 当前仅支持显式 External / Engineering compatibility profile")
            }
        }
    }

    private fun normalizeContent(
        contentModel:PageContentModel,
        rendererKey:String,
        contentOwner:PageContentOwner,
        bodyHtml:String,
        structuredContent:PageStructuredContent?,
        embedUrl:String?,
    ):NormalizedContent {
        validateContract(contentModel,rendererKey,contentOwner)
        val normalizedUrl=embedUrl?.trim()?.takeIf{it.isNotBlank()}
        return when(contentModel) {
            PageContentModel.RICH_TEXT -> {
                if(structuredContent!=null) throw PageValidationException("RICH_TEXT 不能同时声明 structuredContent")
                NormalizedContent(RichTextHtmlPolicy.sanitize(bodyHtml),null,null,normalizedUrl)
            }
            PageContentModel.STRUCTURED -> {
                if(bodyHtml.isNotBlank()) throw PageValidationException("STRUCTURED Page 不允许并行 whole-page bodyHtml authority")
                if(normalizedUrl!=null) throw PageValidationException("当前 Structured Page 不允许 embedUrl")
                val structured=sanitizeStructured(structuredContent?:throw PageValidationException("STRUCTURED Page 必须声明 structuredContent"))
                NormalizedContent("",structured,objectMapper.writeValueAsString(structured),null)
            }
            PageContentModel.NONE -> {
                if(rendererKey==PageRendererKey.INTERNAL_STATIC && !normalizedUrl.isNullOrBlank() && !normalizedUrl.startsWith("/")) throw PageValidationException("站内静态页面必须使用本站路径")
                if(structuredContent!=null) throw PageValidationException("NONE content model 不允许 structuredContent")
                NormalizedContent(bodyHtml,null,null,normalizedUrl)
            }
        }
    }

    private fun sanitizeStructured(content:PageStructuredContent):PageStructuredContent {
        if(content.schemaVersion!=CARD_COLLECTION_SCHEMA_VERSION) throw PageValidationException("不支持的 Structured schemaVersion：${content.schemaVersion}")
        if(content.kind!=CARD_COLLECTION) throw PageValidationException("不支持的 Structured kind：${content.kind}")
        if(content.items.isEmpty()) throw PageValidationException("CARD_COLLECTION items 不能为空")
        if(content.items.size>100) throw PageValidationException("CARD_COLLECTION items 不能超过 100 项")
        return content.copy(items=content.items.mapIndexed { index,item ->
            val title=item.title.trim()
            if(title.isBlank()||title.length>200) throw PageValidationException("CARD_COLLECTION 第 ${index+1} 项 title 不合法")
            PageStructuredCard(title=title,bodyHtml=RichTextHtmlPolicy.sanitize(item.bodyHtml))
        })
    }

    private fun alias(raw:String):String { val a=raw.trim().lowercase(); if(!a.matches(Regex("[a-z0-9][a-z0-9-]{0,99}")))throw PageValidationException("别名只能使用小写字母、数字和连字符"); return a }

    private fun NormalizedPageDraft.record(id:Long?=null)=PageRecord(
        id=id,
        groupId=groupId,
        alias=alias,
        name=name,
        bodyHtml=bodyHtml,
        contentModel=contentModel.name,
        rendererKey=rendererKey,
        contentOwner=contentOwner.name,
        structuredPayload=structuredPayload,
        embedUrl=embedUrl,
        sortOrder=sortOrder,
        enabled=enabled,
    )

    private fun PageRecord.model():CmsPage {
        val contentModelEnum=runCatching { PageContentModel.valueOf(contentModel) }.getOrElse { throw PageValidationException("不支持的 Page contentModel：$contentModel") }
        val contentOwnerEnum=runCatching { PageContentOwner.valueOf(contentOwner) }.getOrElse { throw PageValidationException("不支持的 Page contentOwner：$contentOwner") }
        if(!rendererKey.matches(RENDERER_KEY)) throw PageValidationException("不支持的 Page rendererKey：$rendererKey")
        validateContract(contentModelEnum,rendererKey,contentOwnerEnum)
        val structured=when {
            structuredPayload==null -> null
            contentModelEnum!=PageContentModel.STRUCTURED -> throw PageValidationException("非 Structured Page 不允许 persisted structured payload")
            else -> runCatching { objectMapper.readValue(structuredPayload,PageStructuredContent::class.java) }.getOrElse { throw PageValidationException("Structured Page payload 无法解析") }.let(::sanitizeStructured)
        }
        if(contentModelEnum==PageContentModel.STRUCTURED && structured==null) throw PageValidationException("Structured Page 缺少 payload")
        if(contentModelEnum==PageContentModel.STRUCTURED && bodyHtml.isNotBlank()) throw PageValidationException("Structured Page 存在并行 whole-page bodyHtml authority")
        return CmsPage(
            requireNotNull(id),groupId,alias,name,bodyHtml,contentModelEnum,rendererKey,contentOwnerEnum,structured,
            PageRendererKey.legacyMode(rendererKey),embedUrl,sortOrder,enabled,preset,
        )
    }

    private fun PageGroupRecord.model()=CmsPageGroup(requireNotNull(id),alias,name,sortOrder,enabled,preset)

    private data class PageContract(val contentModel:PageContentModel,val rendererKey:String,val contentOwner:PageContentOwner)
    private data class NormalizedContent(val bodyHtml:String,val structuredContent:PageStructuredContent?,val structuredPayload:String?,val embedUrl:String?)
    private data class NormalizedPageDraft(
        val groupId:Long?,val alias:String,val name:String,val bodyHtml:String,val contentModel:PageContentModel,val rendererKey:String,
        val contentOwner:PageContentOwner,val structuredContent:PageStructuredContent?,val structuredPayload:String?,val embedUrl:String?,
        val sortOrder:Int,val enabled:Boolean,
    )

    private fun PageRecord.sameContract(d:NormalizedPageDraft):Boolean =
        contentModel==d.contentModel.name && rendererKey==d.rendererKey && contentOwner==d.contentOwner.name
}
