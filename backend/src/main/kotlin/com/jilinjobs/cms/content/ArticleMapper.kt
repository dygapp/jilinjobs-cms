package com.jilinjobs.cms.content

import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Options
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select
import org.apache.ibatis.annotations.Update
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime

@Mapper
interface ArticleMapper {
    @Select(
        """
        SELECT id, column_id, title, body_html, source, publish_date,
               pinned, recommended, sort_order, status, actual_published_at,
               view_count, updated_at
        FROM cms_article
        ORDER BY updated_at DESC, id DESC
        """,
    )
    fun findAll(): List<ArticleRecord>

    @Select(
        """
        SELECT id, column_id, title, body_html, source, publish_date,
               pinned, recommended, sort_order, status, actual_published_at,
               view_count, updated_at
        FROM cms_article
        WHERE id = #{id}
        """,
    )
    fun findById(@Param("id") id: Long): ArticleRecord?

    @Insert(
        """
        INSERT INTO cms_article(
            column_id, title, body_html, source, publish_date,
            pinned, recommended, sort_order, status
        )
        VALUES(
            #{columnId}, #{title}, #{bodyHtml}, #{source}, #{publishDate},
            #{pinned}, #{recommended}, #{sortOrder}, 'DRAFT'
        )
        """,
    )
    @Options(useGeneratedKeys = true, keyProperty = "id")
    fun insert(record: ArticleRecord): Int

    @Update(
        """
        UPDATE cms_article
        SET column_id = #{columnId},
            title = #{title},
            body_html = #{bodyHtml},
            source = #{source},
            publish_date = #{publishDate},
            pinned = #{pinned},
            recommended = #{recommended},
            sort_order = #{sortOrder}
        WHERE id = #{id}
        """,
    )
    fun update(record: ArticleRecord): Int

    @Select("SELECT COUNT(*) > 0 FROM cms_article WHERE column_id = #{columnId}")
    fun existsByColumn(@Param("columnId") columnId: Long): Boolean
}

data class ArticleRecord(
    var id: Long? = null,
    var columnId: Long = 0,
    var title: String = "",
    var bodyHtml: String = "",
    var source: String = "",
    var publishDate: LocalDate? = null,
    var pinned: Boolean = false,
    var recommended: Boolean = false,
    var sortOrder: Int = 0,
    var status: String = ArticleStatus.DRAFT.name,
    var actualPublishedAt: LocalDateTime? = null,
    var viewCount: Long = 0,
    var updatedAt: LocalDateTime = LocalDateTime.now(),
)

@Repository
class MyBatisArticleRepository(
    private val mapper: ArticleMapper,
) : ArticleRepository {
    override fun findAll(): List<CmsArticle> = mapper.findAll().map { it.toModel() }

    override fun findById(id: Long): CmsArticle? = mapper.findById(id)?.toModel()

    override fun insert(draft: ArticleDraft): CmsArticle {
        val record = draft.toRecord()
        mapper.insert(record)
        return mapper.findById(requireNotNull(record.id))?.toModel()
            ?: error("文章创建后无法重新读取")
    }

    override fun update(id: Long, draft: ArticleDraft): CmsArticle {
        mapper.update(draft.toRecord(id))
        return mapper.findById(id)?.toModel() ?: throw ArticleNotFoundException(id)
    }

    override fun existsByColumn(columnId: Long): Boolean = mapper.existsByColumn(columnId)

    private fun ArticleDraft.toRecord(id: Long? = null): ArticleRecord = ArticleRecord(
        id = id,
        columnId = columnId,
        title = title,
        bodyHtml = bodyHtml,
        source = source,
        publishDate = publishDate,
        pinned = pinned,
        recommended = recommended,
        sortOrder = sortOrder,
    )

    private fun ArticleRecord.toModel(): CmsArticle = CmsArticle(
        id = requireNotNull(id) { "文章记录缺少主键" },
        columnId = columnId,
        title = title,
        bodyHtml = bodyHtml,
        source = source,
        publishDate = publishDate,
        pinned = pinned,
        recommended = recommended,
        sortOrder = sortOrder,
        status = ArticleStatus.valueOf(status),
        actualPublishedAt = actualPublishedAt,
        viewCount = viewCount,
        updatedAt = updatedAt,
    )
}
