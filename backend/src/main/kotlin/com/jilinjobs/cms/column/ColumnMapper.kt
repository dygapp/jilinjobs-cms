package com.jilinjobs.cms.column

import org.apache.ibatis.annotations.Delete
import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Options
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select
import org.apache.ibatis.annotations.Update
import org.springframework.stereotype.Repository

@Mapper
interface ColumnMapper {
    @Select(
        """
        SELECT id, parent_id, name, sort_order, enabled
        FROM cms_column
        ORDER BY COALESCE(parent_id, 0), sort_order, id
        """,
    )
    fun findAll(): List<ColumnRecord>

    @Select(
        """
        SELECT id, parent_id, name, sort_order, enabled
        FROM cms_column
        WHERE id = #{id}
        """,
    )
    fun findById(@Param("id") id: Long): ColumnRecord?

    @Insert(
        """
        INSERT INTO cms_column(parent_id, name, sort_order, enabled)
        VALUES(#{parentId}, #{name}, #{sortOrder}, #{enabled})
        """,
    )
    @Options(useGeneratedKeys = true, keyProperty = "id")
    fun insert(record: ColumnRecord): Int

    @Update(
        """
        UPDATE cms_column
        SET parent_id = #{parentId},
            name = #{name},
            sort_order = #{sortOrder},
            enabled = #{enabled}
        WHERE id = #{id}
        """,
    )
    fun update(record: ColumnRecord): Int

    @Delete("DELETE FROM cms_column WHERE id = #{id}")
    fun delete(@Param("id") id: Long): Int

    @Select("SELECT COUNT(*) FROM cms_column WHERE parent_id = #{id}")
    fun countChildren(@Param("id") id: Long): Long
}

data class ColumnRecord(
    var id: Long? = null,
    var parentId: Long? = null,
    var name: String = "",
    var sortOrder: Int = 0,
    var enabled: Boolean = true,
)

@Repository
class MyBatisColumnRepository(
    private val mapper: ColumnMapper,
) : ColumnRepository {
    override fun findAll(): List<CmsColumn> = mapper.findAll().map { it.toModel() }

    override fun findById(id: Long): CmsColumn? = mapper.findById(id)?.toModel()

    override fun insert(draft: ColumnDraft): CmsColumn {
        val record = ColumnRecord(
            parentId = draft.parentId,
            name = draft.name,
            sortOrder = draft.sortOrder,
            enabled = draft.enabled,
        )
        mapper.insert(record)
        return record.toModel()
    }

    override fun update(id: Long, draft: ColumnDraft): CmsColumn {
        val record = ColumnRecord(
            id = id,
            parentId = draft.parentId,
            name = draft.name,
            sortOrder = draft.sortOrder,
            enabled = draft.enabled,
        )
        mapper.update(record)
        return record.toModel()
    }

    override fun delete(id: Long) {
        mapper.delete(id)
    }

    override fun countChildren(id: Long): Long = mapper.countChildren(id)

    private fun ColumnRecord.toModel(): CmsColumn = CmsColumn(
        id = requireNotNull(id) { "栏目记录缺少主键" },
        parentId = parentId,
        name = name,
        sortOrder = sortOrder,
        enabled = enabled,
    )
}
