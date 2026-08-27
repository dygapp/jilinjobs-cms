package com.jilinjobs.cms.navigation

import org.apache.ibatis.annotations.*
import org.springframework.stereotype.Repository

@Mapper
interface NavigationMapper {
    @Select("SELECT id,parent_id,name,position,category,target_type,target_column_id,target_page_id,target_url,open_mode,sort_order,enabled FROM cms_navigation ORDER BY position,COALESCE(parent_id,0),sort_order,id") fun findAll():List<NavigationRecord>
    @Select("SELECT id,parent_id,name,position,category,target_type,target_column_id,target_page_id,target_url,open_mode,sort_order,enabled FROM cms_navigation WHERE enabled=1 ORDER BY position,COALESCE(parent_id,0),sort_order,id") fun findEnabled():List<NavigationRecord>
    @Select("SELECT id,parent_id,name,position,category,target_type,target_column_id,target_page_id,target_url,open_mode,sort_order,enabled FROM cms_navigation WHERE id=#{id}") fun findById(@Param("id") id:Long):NavigationRecord?
    @Insert("INSERT INTO cms_navigation(parent_id,name,position,category,target_type,target_column_id,target_page_id,target_url,open_mode,sort_order,enabled) VALUES(#{parentId},#{name},#{position},#{category},#{targetType},#{targetColumnId},#{targetPageId},#{targetUrl},#{openMode},#{sortOrder},#{enabled})") @Options(useGeneratedKeys=true,keyProperty="id") fun insert(record:NavigationRecord):Int
    @Update("UPDATE cms_navigation SET parent_id=#{parentId},name=#{name},position=#{position},category=#{category},target_type=#{targetType},target_column_id=#{targetColumnId},target_page_id=#{targetPageId},target_url=#{targetUrl},open_mode=#{openMode},sort_order=#{sortOrder},enabled=#{enabled} WHERE id=#{id}") fun update(record:NavigationRecord):Int
    @Delete("DELETE FROM cms_navigation WHERE id=#{id}") fun delete(@Param("id") id:Long):Int
}

data class NavigationRecord(var id:Long?=null,var parentId:Long?=null,var name:String="",var position:String=NavigationPosition.MAIN.name,var category:String?=null,var targetType:String=NavigationTargetType.LINK.name,var targetColumnId:Long?=null,var targetPageId:Long?=null,var targetUrl:String?=null,var openMode:String=NavigationOpenMode.DEFAULT.name,var sortOrder:Int=0,var enabled:Boolean=true)

@Repository
class MyBatisNavigationRepository(private val mapper:NavigationMapper):NavigationRepository {
    override fun findAll()=mapper.findAll().map{it.model()}; override fun findEnabled()=mapper.findEnabled().map{it.model()}; override fun findById(id:Long)=mapper.findById(id)?.model()
    override fun insert(draft:NavigationDraft):CmsNavigation { val r=draft.record();mapper.insert(r);return r.model() }
    override fun update(id:Long,draft:NavigationDraft):CmsNavigation { val r=draft.record(id);mapper.update(r);return r.model() }
    override fun delete(id:Long){mapper.delete(id)}
    private fun NavigationDraft.record(id:Long?=null)=NavigationRecord(id,parentId,name,position.name,category,targetType.name,targetColumnId,targetPageId,targetUrl,openMode.name,sortOrder,enabled)
    private fun NavigationRecord.model()=CmsNavigation(requireNotNull(id),name,NavigationPosition.valueOf(position),category,NavigationTargetType.valueOf(targetType),targetColumnId,targetUrl,sortOrder,enabled,parentId,targetPageId,NavigationOpenMode.valueOf(openMode))
}
