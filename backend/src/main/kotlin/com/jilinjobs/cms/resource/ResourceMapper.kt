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

    @Select(
        """
        SELECT (
            EXISTS(
                SELECT 1
                FROM cms_article_resource ar
                JOIN cms_article a ON a.id = ar.article_id
                WHERE ar.resource_id = #{resourceId}
                  AND ar.resource_role IN ('COVER','BODY_IMAGE')
                  AND a.status = 'PUBLISHED'
            )
            OR EXISTS(
                SELECT 1
                FROM cms_list_item i
                JOIN cms_list l ON l.id = i.list_id
                JOIN cms_article a ON a.id = i.article_id
                WHERE i.image_resource_id = #{resourceId}
                  AND i.source_type = 'ARTICLE'
                  AND i.enabled = 1
                  AND l.enabled = 1
                  AND a.status = 'PUBLISHED'
            )
        )
        """,
    )
    fun isPublishedImage(@Param("resourceId") resourceId: Long): Boolean

    @Select(
        """
        SELECT COUNT(*) > 0
        FROM cms_article_resource ar
        JOIN cms_article a ON a.id = ar.article_id
        WHERE ar.resource_id = #{resourceId}
          AND ar.resource_role = 'BODY_IMAGE'
          AND a.status = 'PUBLISHED'
        """,
    )
    fun isPublishedBodyImage(@Param("resourceId") resourceId: Long): Boolean

    @Select(
        """
        SELECT COUNT(*) > 0
        FROM cms_article_resource ar
        JOIN cms_article a ON a.id = ar.article_id
        WHERE ar.resource_id = #{resourceId}
          AND ar.resource_role = 'ATTACHMENT'
          AND a.status = 'PUBLISHED'
        """,
    )
    fun isPublishedAttachment(@Param("resourceId") resourceId: Long): Boolean

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

    override fun isPublishedImage(resourceId: Long): Boolean = mapper.isPublishedImage(resourceId)

    override fun isPublishedBodyImage(resourceId: Long): Boolean = mapper.isPublishedBodyImage(resourceId)

    override fun isPublishedAttachment(resourceId: Long): Boolean = mapper.isPublishedAttachment(resourceId)

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
