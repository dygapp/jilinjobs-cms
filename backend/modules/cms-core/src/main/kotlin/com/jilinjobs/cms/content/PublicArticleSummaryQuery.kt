package com.jilinjobs.cms.content

import com.jilinjobs.cms.column.ColumnQuery
import java.time.LocalDate
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

data class PublicArticleSummaryQueryRow(
    var id: Long = 0,
    var columnId: Long = 0,
    var columnName: String = "",
    var columnAlias: String = "",
    var title: String = "",
    var source: String = "",
    var articleType: String = ArticleType.INTERNAL.name,
    var externalUrl: String? = null,
    var publishDate: LocalDate? = null,
    var pinned: Boolean = false,
    var sortOrder: Int = 0,
    var coverResourceId: Long? = null,
)

@Mapper
interface PublicArticleSummaryQueryMapper {
    @Select(
        """
        SELECT a.id, a.column_id, c.name AS column_name, c.alias AS column_alias,
               a.title, a.source, a.article_type, a.external_url, a.publish_date,
               a.pinned, a.sort_order,
               (SELECT ar.resource_id
                  FROM cms_article_resource ar
                 WHERE ar.article_id = a.id AND ar.resource_role = 'COVER'
                 ORDER BY ar.sort_order, ar.resource_id
                 LIMIT 1) AS cover_resource_id
        FROM cms_article a
        JOIN cms_column c ON c.id = a.column_id
        WHERE a.status = 'PUBLISHED'
          AND (#{columnId} IS NULL OR a.column_id = #{columnId})
          AND (#{articleType} IS NULL OR a.article_type = #{articleType})
        ORDER BY a.pinned DESC,
                 a.sort_order DESC,
                 COALESCE(a.publish_date, DATE(a.actual_published_at)) DESC,
                 a.id DESC
        LIMIT #{limit} OFFSET #{offset}
        """,
    )
    fun findPublished(
        @Param("columnId") columnId: Long?,
        @Param("articleType") articleType: String?,
        @Param("limit") limit: Int,
        @Param("offset") offset: Int,
    ): List<PublicArticleSummaryQueryRow>

    @Select(
        """
        SELECT COUNT(*)
        FROM cms_article a
        WHERE a.status = 'PUBLISHED'
          AND (#{columnId} IS NULL OR a.column_id = #{columnId})
          AND (#{articleType} IS NULL OR a.article_type = #{articleType})
        """,
    )
    fun countPublished(
        @Param("columnId") columnId: Long?,
        @Param("articleType") articleType: String?,
    ): Long
}

@Service
class PublicArticleSummaryQueryService(
    private val mapper: PublicArticleSummaryQueryMapper,
    private val columns: ColumnQuery,
) {
    @Transactional(readOnly = true)
    fun list(columnId: Long?, articleType: ArticleType?, page: Int, size: Int): PublicArticlePage {
        if (page < 0) throw ArticleValidationException("页码不能小于 0")
        if (size !in 1..50) throw ArticleValidationException("每页数量必须在 1 到 50 之间")
        columnId?.let { if (columns.find(it) == null) throw ArticleValidationException("所属栏目不存在：$it") }
        val type = articleType?.name
        val rows = mapper.findPublished(columnId, type, size, page * size)
        return PublicArticlePage(
            items = rows.map { row ->
                PublicArticleSummary(
                    id = row.id,
                    columnId = row.columnId,
                    columnName = row.columnName,
                    title = row.title,
                    publishDate = row.publishDate,
                    pinned = row.pinned,
                    sortOrder = row.sortOrder,
                    columnAlias = row.columnAlias,
                    source = row.source,
                    articleType = ArticleType.valueOf(row.articleType),
                    externalUrl = row.externalUrl,
                    coverResourceId = row.coverResourceId,
                )
            },
            page = page,
            size = size,
            total = mapper.countPublished(columnId, type),
        )
    }
}
