package com.jilinjobs.cms.navigation

import org.apache.ibatis.annotations.Delete
import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Options
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select
import org.apache.ibatis.annotations.Update
import org.springframework.stereotype.Repository

@Mapper
interface NavigationMapper {
    @Select(
        """
        SELECT id, name, position, category, target_type, target_column_id, target_url, sort_order, enabled
        FROM cms_navigation
        ORDER BY position, COALESCE(category, ''), sort_order, id
        """,
    )
    fun findAll(): List<NavigationRecord>

    @Select(
        """
        SELECT id, name, position, category, target_type, target_column_id, target_url, sort_order, enabled
        FROM cms_navigation
        WHERE enabled = 1
        ORDER BY position, COALESCE(category, ''), sort_order, id
        """,
    )
    fun findEnabled(): List<NavigationRecord>

    @Select(
        """
        SELECT id, name, position, category, target_type, target_column_id, target_url, sort_order, enabled
        FROM cms_navigation
        WHERE id = #{id}
        """,
    )
    fun findById(@Param("id") id: Long): NavigationRecord?

    @Insert(
        """
        INSERT INTO cms_navigation(
            name, position, category, target_type, target_column_id, target_url, sort_order, enabled
        )
        VALUES(
            #{name}, #{position}, #{category}, #{targetType}, #{targetColumnId}, #{targetUrl}, #{sortOrder}, #{enabled}
        )
        """,
    )
    @Options(useGeneratedKeys = true, keyProperty = "id")
    fun insert(record: NavigationRecord): Int

    @Update(
        """
        UPDATE cms_navigation
        SET name = #{name},
            position = #{position},
            category = #{category},
            target_type = #{targetType},
            target_column_id = #{targetColumnId},
            target_url = #{targetUrl},
            sort_order = #{sortOrder},
            enabled = #{enabled}
        WHERE id = #{id}
        """,
    )
    fun update(record: NavigationRecord): Int

    @Delete("DELETE FROM cms_navigation WHERE id = #{id}")
    fun delete(@Param("id") id: Long): Int
}

data class NavigationRecord(
    var id: Long? = null,
    var name: String = "",
    var position: String = NavigationPosition.MAIN.name,
    var category: String? = null,
    var targetType: String = NavigationTargetType.LINK.name,
    var targetColumnId: Long? = null,
    var targetUrl: String? = null,
    var sortOrder: Int = 0,
    var enabled: Boolean = true,
)

@Repository
class MyBatisNavigationRepository(
    private val mapper: NavigationMapper,
) : NavigationRepository {
    override fun findAll(): List<CmsNavigation> = mapper.findAll().map { it.toModel() }

    override fun findEnabled(): List<CmsNavigation> = mapper.findEnabled().map { it.toModel() }

    override fun findById(id: Long): CmsNavigation? = mapper.findById(id)?.toModel()

    override fun insert(draft: NavigationDraft): CmsNavigation {
        val record = draft.toRecord()
        mapper.insert(record)
        return record.toModel()
    }

    override fun update(id: Long, draft: NavigationDraft): CmsNavigation {
        val record = draft.toRecord(id)
        mapper.update(record)
        return record.toModel()
    }

    override fun delete(id: Long) {
        mapper.delete(id)
    }

    private fun NavigationDraft.toRecord(id: Long? = null): NavigationRecord = NavigationRecord(
        id = id,
        name = name,
        position = position.name,
        category = category,
        targetType = targetType.name,
        targetColumnId = targetColumnId,
        targetUrl = targetUrl,
        sortOrder = sortOrder,
        enabled = enabled,
    )

    private fun NavigationRecord.toModel(): CmsNavigation = CmsNavigation(
        id = requireNotNull(id) { "导航记录缺少主键" },
        name = name,
        position = NavigationPosition.valueOf(position),
        category = category,
        targetType = NavigationTargetType.valueOf(targetType),
        targetColumnId = targetColumnId,
        targetUrl = targetUrl,
        sortOrder = sortOrder,
        enabled = enabled,
    )
}
