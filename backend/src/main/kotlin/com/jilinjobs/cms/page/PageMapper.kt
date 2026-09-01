package com.jilinjobs.cms.page

import org.apache.ibatis.annotations.*

@Mapper
interface PageMapper {
    @Select("SELECT id, alias, name, sort_order, enabled, preset FROM cms_page_group ORDER BY sort_order,id")
    fun findGroups(): List<PageGroupRecord>
    @Select("SELECT id, alias, name, sort_order, enabled, preset FROM cms_page_group WHERE id=#{id}") fun findGroupById(@Param("id") id: Long): PageGroupRecord?
    @Select("SELECT id, alias, name, sort_order, enabled, preset FROM cms_page_group WHERE alias=#{alias}") fun findGroupByAlias(@Param("alias") alias: String): PageGroupRecord?
    @Insert("INSERT INTO cms_page_group(alias,name,sort_order,enabled) VALUES(#{alias},#{name},#{sortOrder},#{enabled})") @Options(useGeneratedKeys=true,keyProperty="id") fun insertGroup(record: PageGroupRecord): Int
    @Update("UPDATE cms_page_group SET alias=#{alias},name=#{name},sort_order=#{sortOrder},enabled=#{enabled} WHERE id=#{id}") fun updateGroup(record: PageGroupRecord): Int

    @Select("SELECT id, group_id, alias, name, body_html, render_mode, embed_url, sort_order, enabled, preset FROM cms_page ORDER BY COALESCE(group_id,0),sort_order,id") fun findPages(): List<PageRecord>
    @Select("SELECT id, group_id, alias, name, body_html, render_mode, embed_url, sort_order, enabled, preset FROM cms_page WHERE id=#{id}") fun findPageById(@Param("id") id: Long): PageRecord?
    @Select("SELECT id, group_id, alias, name, body_html, render_mode, embed_url, sort_order, enabled, preset FROM cms_page WHERE group_id IS NULL AND alias=#{alias}") fun findStandalone(@Param("alias") alias: String): PageRecord?
    @Select("SELECT p.id,p.group_id,p.alias,p.name,p.body_html,p.render_mode,p.embed_url,p.sort_order,p.enabled,p.preset FROM cms_page p JOIN cms_page_group g ON g.id=p.group_id WHERE g.alias=#{groupAlias} AND p.alias=#{alias}") fun findGrouped(@Param("groupAlias") groupAlias: String,@Param("alias") alias: String): PageRecord?
    @Select("SELECT id, group_id, alias, name, body_html, render_mode, embed_url, sort_order, enabled, preset FROM cms_page WHERE group_id=#{groupId} ORDER BY sort_order,id") fun findByGroup(@Param("groupId") groupId: Long): List<PageRecord>
    @Insert("INSERT INTO cms_page(group_id,alias,name,body_html,render_mode,embed_url,sort_order,enabled) VALUES(#{groupId},#{alias},#{name},#{bodyHtml},#{renderMode},#{embedUrl},#{sortOrder},#{enabled})") @Options(useGeneratedKeys=true,keyProperty="id") fun insertPage(record: PageRecord): Int
    @Update("UPDATE cms_page SET group_id=#{groupId},alias=#{alias},name=#{name},body_html=#{bodyHtml},render_mode=#{renderMode},embed_url=#{embedUrl},sort_order=#{sortOrder},enabled=#{enabled} WHERE id=#{id}") fun updatePage(record: PageRecord): Int
    @Delete("DELETE FROM cms_page WHERE id=#{id}") fun deletePage(@Param("id") id: Long): Int
}

data class PageGroupRecord(var id: Long?=null,var alias:String="",var name:String="",var sortOrder:Int=0,var enabled:Boolean=true,var preset:Boolean=false)
data class PageRecord(var id:Long?=null,var groupId:Long?=null,var alias:String="",var name:String="",var bodyHtml:String="",var renderMode:String=PageRenderMode.RICH_TEXT.name,var embedUrl:String?=null,var sortOrder:Int=0,var enabled:Boolean=true,var preset:Boolean=false)
