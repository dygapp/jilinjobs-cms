package com.jilinjobs.cms.content

import com.jilinjobs.cms.column.ColumnQuery
import java.time.LocalDate
import java.time.LocalDateTime
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class AdminArticleSummary(
    val id: Long,
    val columnId: Long,
    val title: String,
    val source: String,
    val articleType: ArticleType,
    val publishDate: LocalDate?,
    val status: ArticleStatus,
    val viewCount: Long,
    val updatedAt: LocalDateTime,
)

data class AdminArticlePage(
    val items: List<AdminArticleSummary>,
    val page: Int,
    val size: Int,
    val total: Long,
)

data class AdminArticleQueryRow(
    var id: Long = 0,
    var columnId: Long = 0,
    var title: String = "",
    var source: String = "",
    var articleType: String = ArticleType.INTERNAL.name,
    var publishDate: LocalDate? = null,
    var status: String = ArticleStatus.DRAFT.name,
    var viewCount: Long = 0,
    var updatedAt: LocalDateTime = LocalDateTime.now(),
)

@Mapper
interface AdminArticleQueryMapper {
    @Select(
        """
        WITH RECURSIVE scoped_columns AS (
            SELECT id
            FROM cms_column
            WHERE #{columnId} IS NOT NULL AND id = #{columnId}
            UNION ALL
            SELECT c.id
            FROM cms_column c
            JOIN scoped_columns scoped ON c.parent_id = scoped.id
        )
        SELECT a.id, a.column_id, a.title, a.source, a.article_type, a.publish_date,
               a.status, a.view_count, a.updated_at
        FROM cms_article a
        WHERE (#{columnId} IS NULL OR a.column_id IN (SELECT id FROM scoped_columns))
          AND (#{keyword} IS NULL OR LOWER(CONCAT(a.title, ' ', a.source)) LIKE CONCAT('%', LOWER(#{keyword}), '%'))
          AND (#{status} IS NULL OR a.status = #{status})
          AND (#{articleType} IS NULL OR a.article_type = #{articleType})
        ORDER BY a.updated_at DESC, a.id DESC
        LIMIT #{limit} OFFSET #{offset}
        """,
    )
    fun findPage(
        @Param("keyword") keyword: String?,
        @Param("columnId") columnId: Long?,
        @Param("status") status: String?,
        @Param("articleType") articleType: String?,
        @Param("limit") limit: Int,
        @Param("offset") offset: Int,
    ): List<AdminArticleQueryRow>

    @Select(
        """
        WITH RECURSIVE scoped_columns AS (
            SELECT id
            FROM cms_column
            WHERE #{columnId} IS NOT NULL AND id = #{columnId}
            UNION ALL
            SELECT c.id
            FROM cms_column c
            JOIN scoped_columns scoped ON c.parent_id = scoped.id
        )
        SELECT COUNT(*)
        FROM cms_article a
        WHERE (#{columnId} IS NULL OR a.column_id IN (SELECT id FROM scoped_columns))
          AND (#{keyword} IS NULL OR LOWER(CONCAT(a.title, ' ', a.source)) LIKE CONCAT('%', LOWER(#{keyword}), '%'))
          AND (#{status} IS NULL OR a.status = #{status})
          AND (#{articleType} IS NULL OR a.article_type = #{articleType})
        """,
    )
    fun count(
        @Param("keyword") keyword: String?,
        @Param("columnId") columnId: Long?,
        @Param("status") status: String?,
        @Param("articleType") articleType: String?,
    ): Long
}

@Service
class AdminArticleQueryService(
    private val mapper: AdminArticleQueryMapper,
    private val columns: ColumnQuery,
) {
    @Transactional(readOnly = true)
    fun list(
        keyword: String?,
        columnId: Long?,
        status: ArticleStatus?,
        articleType: ArticleType?,
        page: Int,
        size: Int,
    ): AdminArticlePage {
        if (page < 0) throw ArticleValidationException("页码不能小于 0")
        if (size !in 1..100) throw ArticleValidationException("每页数量必须在 1 到 100 之间")
        columnId?.let { if (columns.find(it) == null) throw ArticleValidationException("所属栏目不存在：$it") }

        val normalizedKeyword = keyword?.trim()?.takeIf { it.isNotEmpty() }
        val statusName = status?.name
        val articleTypeName = articleType?.name
        val rows = mapper.findPage(normalizedKeyword, columnId, statusName, articleTypeName, size, page * size)
        return AdminArticlePage(
            items = rows.map { row ->
                AdminArticleSummary(
                    id = row.id,
                    columnId = row.columnId,
                    title = row.title,
                    source = row.source,
                    articleType = ArticleType.valueOf(row.articleType),
                    publishDate = row.publishDate,
                    status = ArticleStatus.valueOf(row.status),
                    viewCount = row.viewCount,
                    updatedAt = row.updatedAt,
                )
            },
            page = page,
            size = size,
            total = mapper.count(normalizedKeyword, columnId, statusName, articleTypeName),
        )
    }
}
