package com.jilinjobs.cms.navigation

import org.apache.ibatis.annotations.*
import org.springframework.stereotype.Repository

@Mapper
interface NavigationMapper {
    @Select("SELECT n.id,n.parent_id,n.name,n.position,n.category,n.target_type,n.target_column_id,n.target_page_id,n.target_url,n.open_mode,n.icon_path,n.sort_order,n.enabled,n.preset FROM cms_navigation n ORDER BY n.position,COALESCE(n.parent_id,0),n.sort_order,n.id")
    fun findAll(): List<NavigationRecord>

    @Select("SELECT n.id,n.parent_id,n.name,n.position,n.category,n.target_type,n.target_column_id,n.target_page_id,n.target_url,n.open_mode,n.icon_path,n.sort_order,n.enabled,n.preset FROM cms_navigation n JOIN cms_navigation_location l ON l.code=n.position WHERE n.enabled=1 AND l.enabled=1 ORDER BY l.sort_order,n.position,COALESCE(n.parent_id,0),n.sort_order,n.id")
    fun findEnabled(): List<NavigationRecord>

    @Select("SELECT id,parent_id,name,position,category,target_type,target_column_id,target_page_id,target_url,open_mode,icon_path,sort_order,enabled,preset FROM cms_navigation WHERE id=#{id}")
    fun findById(@Param("id") id: Long): NavigationRecord?

    @Select("SELECT COUNT(*) FROM cms_navigation WHERE position=#{position}")
    fun countByPosition(@Param("position") position: String): Long

    @Select("SELECT icon_path FROM cms_navigation WHERE enabled=1 AND icon_path LIKE '/static/%'")
    fun findReferencedIcons(): List<String>

    @Insert("INSERT INTO cms_navigation(parent_id,name,position,category,target_type,target_column_id,target_page_id,target_url,open_mode,icon_path,sort_order,enabled) VALUES(#{parentId},#{name},#{position},#{category},#{targetType},#{targetColumnId},#{targetPageId},#{targetUrl},#{openMode},#{iconPath},#{sortOrder},#{enabled})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    fun insert(record: NavigationRecord): Int

    @Update("UPDATE cms_navigation SET parent_id=#{parentId},name=#{name},position=#{position},category=#{category},target_type=#{targetType},target_column_id=#{targetColumnId},target_page_id=#{targetPageId},target_url=#{targetUrl},open_mode=#{openMode},icon_path=#{iconPath},sort_order=#{sortOrder},enabled=#{enabled} WHERE id=#{id}")
    fun update(record: NavigationRecord): Int

    @Delete("DELETE FROM cms_navigation WHERE id=#{id}")
    fun delete(@Param("id") id: Long): Int
}

data class NavigationRecord(
    var id: Long? = null,
    var parentId: Long? = null,
    var name: String = "",
    var position: String = "MAIN",
    var category: String? = null,
    var targetType: String = NavigationTargetType.LINK.name,
    var targetColumnId: Long? = null,
    var targetPageId: Long? = null,
    var targetUrl: String? = null,
    var openMode: String = NavigationOpenMode.DEFAULT.name,
    var iconPath: String? = null,
    var sortOrder: Int = 0,
    var enabled: Boolean = true,
    var preset: Boolean = false,
)

@Repository
class MyBatisNavigationRepository(private val mapper: NavigationMapper) : NavigationRepository {
    override fun findAll() = mapper.findAll().map { it.model() }
    override fun findEnabled() = mapper.findEnabled().map { it.model() }
    override fun findById(id: Long) = mapper.findById(id)?.model()

    override fun insert(draft: NavigationDraft): CmsNavigation {
        val record = draft.record()
        mapper.insert(record)
        return mapper.findById(requireNotNull(record.id))!!.model()
    }

    override fun update(id: Long, draft: NavigationDraft): CmsNavigation {
        val record = draft.record(id)
        mapper.update(record)
        return mapper.findById(id)!!.model()
    }

    override fun delete(id: Long) {
        mapper.delete(id)
    }

    private fun NavigationDraft.record(id: Long? = null) = NavigationRecord(
        id, parentId, name, position, category, targetType.name, targetColumnId, targetPageId,
        targetUrl, openMode.name, iconPath, sortOrder, enabled,
    )

    private fun NavigationRecord.model() = CmsNavigation(
        requireNotNull(id), name, position, category, NavigationTargetType.valueOf(targetType),
        targetColumnId, targetUrl, sortOrder, enabled, parentId, targetPageId,
        NavigationOpenMode.valueOf(openMode), iconPath, preset,
    )
}
