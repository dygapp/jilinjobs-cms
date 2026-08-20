package com.jilinjobs.cms.resource

import org.apache.ibatis.annotations.Delete
import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Options
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select
import org.springframework.stereotype.Repository

@Mapper
interface ResourceMapper {
    @Insert(
        """
        INSERT INTO cms_resource(storage_key, original_filename, content_type, size_bytes)
        VALUES(#{storageKey}, #{originalFilename}, #{contentType}, #{sizeBytes})
        """,
    )
    @Options(useGeneratedKeys = true, keyProperty = "id")
    fun insert(record: ResourceRecord): Int

    @Select(
        """
        SELECT id, storage_key, original_filename, content_type, size_bytes
        FROM cms_resource
        WHERE id = #{id}
        """,
    )
    fun findById(@Param("id") id: Long): ResourceRecord?

    @Select(
        """
        SELECT resource_id
        FROM cms_article_resource
        WHERE article_id = #{articleId} AND resource_role = #{role}
        ORDER BY sort_order, resource_id
        """,
    )
    fun findArticleResourceIds(
        @Param("articleId") articleId: Long,
        @Param("role") role: String,
    ): List<Long>

    @Delete("DELETE FROM cms_article_resource WHERE article_id = #{articleId}")
    fun deleteArticleLinks(@Param("articleId") articleId: Long): Int

    @Insert(
        """
        INSERT INTO cms_article_resource(article_id, resource_id, resource_role, sort_order)
        VALUES(#{articleId}, #{resourceId}, #{role}, #{sortOrder})
        """,
    )
    fun insertArticleLink(
        @Param("articleId") articleId: Long,
        @Param("resourceId") resourceId: Long,
        @Param("role") role: String,
        @Param("sortOrder") sortOrder: Int,
    ): Int
}

data class ResourceRecord(
    var id: Long? = null,
    var storageKey: String = "",
    var originalFilename: String = "",
    var contentType: String? = null,
    var sizeBytes: Long = 0,
)

@Repository
class MyBatisResourceRepository(
    private val mapper: ResourceMapper,
) : ResourceRepository {
    override fun insert(draft: ResourceDraft): CmsResource {
        val record = ResourceRecord(
            storageKey = draft.storageKey,
            originalFilename = draft.originalFilename,
            contentType = draft.contentType,
            sizeBytes = draft.sizeBytes,
        )
        mapper.insert(record)
        return record.toModel()
    }

    override fun findById(id: Long): CmsResource? = mapper.findById(id)?.toModel()

    override fun findArticleResourceIds(articleId: Long, role: ArticleResourceRole): List<Long> =
        mapper.findArticleResourceIds(articleId, role.name)

    override fun deleteArticleLinks(articleId: Long) {
        mapper.deleteArticleLinks(articleId)
    }

    override fun insertArticleLink(articleId: Long, resourceId: Long, role: ArticleResourceRole, sortOrder: Int) {
        mapper.insertArticleLink(articleId, resourceId, role.name, sortOrder)
    }

    private fun ResourceRecord.toModel(): CmsResource = CmsResource(
        id = requireNotNull(id) { "文件资源记录缺少主键" },
        storageKey = storageKey,
        originalFilename = originalFilename,
        contentType = contentType,
        sizeBytes = sizeBytes,
    )
}
