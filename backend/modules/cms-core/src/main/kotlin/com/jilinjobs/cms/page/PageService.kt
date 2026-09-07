package com.jilinjobs.cms.page

import com.jilinjobs.cms.common.RichTextHtmlPolicy
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PageService(private val mapper: PageMapper) : PageLookup {
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
        val r=d.record(id); mapper.updatePage(r); return mapper.findPageById(id)!!.model()
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
        val publicBodyHtml = if (page.renderMode == PageRenderMode.RICH_TEXT) RichTextHtmlPolicy.sanitize(page.bodyHtml) else page.bodyHtml
        return PublicPage(page.id,page.alias,page.name,publicBodyHtml,page.renderMode,page.embedUrl,url,publicGroup,crumbs)
    }

    private fun CmsPageGroup.publicGroup():PublicPageGroup = PublicPageGroup(alias,name,mapper.findByGroup(id).map{it.model()}.filter{it.enabled}.map{ PublicPageMember(it.alias,it.name,"/page/$alias/${it.alias}",it.sortOrder) })

    private fun normalize(d:PageGroupDraft):PageGroupDraft { val a=alias(d.alias); val n=d.name.trim(); if(n.isBlank())throw PageValidationException("单页分组名称不能为空"); return d.copy(alias=a,name=n) }
    private fun normalize(d:PageDraft,currentId:Long?):PageDraft {
        val a=alias(d.alias); val n=d.name.trim(); if(n.isBlank())throw PageValidationException("单页名称不能为空")
        d.groupId?.let { mapper.findGroupById(it)?:throw PageValidationException("单页分组不存在：$it") }
        mapper.findPages().map{it.model()}.firstOrNull{it.groupId==d.groupId&&it.alias==a&&it.id!=currentId}?.let { throw PageValidationException("单页别名已存在：$a") }
        if(d.renderMode==PageRenderMode.INTERNAL_STATIC && !d.embedUrl.isNullOrBlank() && !d.embedUrl.startsWith("/")) throw PageValidationException("站内静态页面必须使用本站路径")
        val bodyHtml = if (d.renderMode == PageRenderMode.RICH_TEXT) RichTextHtmlPolicy.sanitize(d.bodyHtml) else d.bodyHtml
        return d.copy(alias=a,name=n,bodyHtml=bodyHtml,embedUrl=d.embedUrl?.trim()?.takeIf{it.isNotBlank()})
    }
    private fun alias(raw:String):String { val a=raw.trim().lowercase(); if(!a.matches(Regex("[a-z0-9][a-z0-9-]{0,99}")))throw PageValidationException("别名只能使用小写字母、数字和连字符"); return a }
    private fun PageDraft.record(id:Long?=null)=PageRecord(id,groupId,alias,name,bodyHtml,renderMode.name,embedUrl,sortOrder,enabled)
    private fun PageRecord.model()=CmsPage(requireNotNull(id),groupId,alias,name,bodyHtml,PageRenderMode.valueOf(renderMode),embedUrl,sortOrder,enabled,preset)
    private fun PageGroupRecord.model()=CmsPageGroup(requireNotNull(id),alias,name,sortOrder,enabled,preset)
}
