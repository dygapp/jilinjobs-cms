package com.jilinjobs.cms.navigation

import com.jilinjobs.cms.column.ColumnQuery
import com.jilinjobs.cms.page.*
import java.net.URI
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NavigationService(private val repository:NavigationRepository,private val columns:ColumnQuery,private val pages:PageLookup=EmptyPageLookup){
 @Transactional(readOnly=true) fun listAdmin()=repository.findAll()
 @Transactional(readOnly=true) fun listPublic()=repository.findEnabled().map{it.toPublic()}
 @Transactional fun create(draft:NavigationDraft)=repository.insert(normalize(draft,null))
 @Transactional fun update(id:Long,draft:NavigationDraft):CmsNavigation{repository.findById(id)?:throw NavigationNotFoundException(id);return repository.update(id,normalize(draft,id))}
 @Transactional fun delete(id:Long){repository.findById(id)?:throw NavigationNotFoundException(id);if(repository.findAll().any{it.parentId==id})throw NavigationValidationException("导航存在下级菜单，不能直接删除");repository.delete(id)}
 private fun normalize(d:NavigationDraft,currentId:Long?):NavigationDraft{val name=d.name.trim();if(name.isBlank())throw NavigationValidationException("导航名称不能为空");if(name.length>100)throw NavigationValidationException("导航名称不能超过 100 个字符");validateParent(d.parentId,currentId);val base=d.copy(name=name,category=d.category?.trim()?.takeIf{it.isNotBlank()});return when(d.targetType){NavigationTargetType.HOME,NavigationTargetType.PLACEHOLDER->base.copy(targetColumnId=null,targetPageId=null,targetUrl=null);NavigationTargetType.COLUMN->{val id=d.targetColumnId?:throw NavigationValidationException("栏目目标必须选择栏目");if(columns.find(id)==null)throw NavigationValidationException("目标栏目不存在：$id");base.copy(targetColumnId=id,targetPageId=null,targetUrl=null)};NavigationTargetType.PAGE->{val id=d.targetPageId?:throw NavigationValidationException("固定页面目标必须选择页面");if(pages.pathForPage(id)==null)throw NavigationValidationException("目标固定页面不存在：$id");base.copy(targetColumnId=null,targetPageId=id,targetUrl=null)};NavigationTargetType.LINK->base.copy(targetColumnId=null,targetPageId=null,targetUrl=normalizeLink(d.targetUrl))}}
 private fun validateParent(parentId:Long?,currentId:Long?){var cursor=parentId;while(cursor!=null){if(currentId!=null&&cursor==currentId)throw NavigationValidationException("导航不能设置为自身或自身下级的子菜单");val parent=repository.findById(cursor)?:throw NavigationValidationException("上级导航不存在：$cursor");cursor=parent.parentId}}
 private fun normalizeLink(raw:String?):String{val v=raw?.trim().orEmpty();if(v.isBlank())throw NavigationValidationException("链接目标不能为空");if(v.length>1000)throw NavigationValidationException("链接地址不能超过 1000 个字符");if(v.startsWith("/")&&!v.startsWith("//"))return v;val u=runCatching{URI(v)}.getOrElse{throw NavigationValidationException("链接地址格式不正确")};if(u.scheme?.lowercase() !in setOf("http","https")||u.host.isNullOrBlank())throw NavigationValidationException("链接地址必须是站内路径或 HTTP(S) 地址");return v}
 private fun CmsNavigation.toPublic():PublicNavigation{val href=when(targetType){NavigationTargetType.HOME->"/";NavigationTargetType.COLUMN->{val c=columns.find(requireNotNull(targetColumnId));if(c?.alias.isNullOrBlank()||c!!.alias.startsWith("column-"))"/columns/${targetColumnId}" else "/column/${c.alias}"};NavigationTargetType.PAGE->pages.pathForPage(requireNotNull(targetPageId))?:"#";NavigationTargetType.LINK->requireNotNull(targetUrl);NavigationTargetType.PLACEHOLDER->"#"};val external=href.startsWith("http://")||href.startsWith("https://");val nw=when(openMode){NavigationOpenMode.NEW_WINDOW->true;NavigationOpenMode.SAME_WINDOW->false;NavigationOpenMode.DEFAULT->external};return PublicNavigation(id,name,position,category,sortOrder,targetType,href,external,parentId,nw,targetType!=NavigationTargetType.PLACEHOLDER)}
}
