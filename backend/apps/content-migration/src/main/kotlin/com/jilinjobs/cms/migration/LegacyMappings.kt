package com.jilinjobs.cms.migration

import org.apache.ibatis.annotations.Insert
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.apache.ibatis.annotations.Select

@Mapper
interface ArticleLegacyMappingMapper {
    @Select(
        """
        SELECT id, source_system, legacy_key, content_id, type_code, detail_path,
               source_url, source_fingerprint, article_id
        FROM cms_article_legacy_mapping
        WHERE source_system=#{sourceSystem} AND legacy_key=#{legacyKey}
        """,
    )
    fun find(
        @Param("sourceSystem") sourceSystem: String,
        @Param("legacyKey") legacyKey: String,
    ): ArticleLegacyMappingRecord?

    @Insert(
        """
        INSERT INTO cms_article_legacy_mapping(
            source_system, legacy_key, content_id, type_code, detail_path,
            source_url, source_fingerprint, article_id
        ) VALUES(
            #{sourceSystem}, #{legacyKey}, #{contentId}, #{typeCode}, #{detailPath},
            #{sourceUrl}, #{sourceFingerprint}, #{articleId}
        )
        """,
    )
    fun insert(record: ArticleLegacyMappingRecord): Int
}

data class ArticleLegacyMappingRecord(
    var id: Long? = null,
    var sourceSystem: String = "",
    var legacyKey: String = "",
    var contentId: String? = null,
    var typeCode: String = "",
    var detailPath: String = "",
    var sourceUrl: String = "",
    var sourceFingerprint: String = "",
    var articleId: Long = 0,
)

@Mapper
interface CmsListItemLegacyMappingMapper {
    @Select(
        """
        SELECT id, source_system, legacy_key, source_url, source_fingerprint,
               image_source_url, image_sha256, list_item_id
        FROM cms_list_item_legacy_mapping
        WHERE source_system=#{sourceSystem} AND legacy_key=#{legacyKey}
        """,
    )
    fun find(
        @Param("sourceSystem") sourceSystem: String,
        @Param("legacyKey") legacyKey: String,
    ): CmsListItemLegacyMappingRecord?

    @Insert(
        """
        INSERT INTO cms_list_item_legacy_mapping(
            source_system, legacy_key, source_url, source_fingerprint,
            image_source_url, image_sha256, list_item_id
        ) VALUES(
            #{sourceSystem}, #{legacyKey}, #{sourceUrl}, #{sourceFingerprint},
            #{imageSourceUrl}, #{imageSha256}, #{listItemId}
        )
        """,
    )
    fun insert(record: CmsListItemLegacyMappingRecord): Int
}

data class CmsListItemLegacyMappingRecord(
    var id: Long? = null,
    var sourceSystem: String = "",
    var legacyKey: String = "",
    var sourceUrl: String = "",
    var sourceFingerprint: String = "",
    var imageSourceUrl: String = "",
    var imageSha256: String = "",
    var listItemId: Long = 0,
)

@Mapper
interface PageLegacyMappingMapper {
    @Select(
        """
        SELECT id, source_system, legacy_key, source_url, source_fingerprint, page_id
        FROM cms_page_legacy_mapping
        WHERE source_system=#{sourceSystem} AND legacy_key=#{legacyKey}
        """,
    )
    fun find(
        @Param("sourceSystem") sourceSystem: String,
        @Param("legacyKey") legacyKey: String,
    ): PageLegacyMappingRecord?

    @Insert(
        """
        INSERT INTO cms_page_legacy_mapping(
            source_system, legacy_key, source_url, source_fingerprint, page_id
        ) VALUES(
            #{sourceSystem}, #{legacyKey}, #{sourceUrl}, #{sourceFingerprint}, #{pageId}
        )
        """,
    )
    fun insert(record: PageLegacyMappingRecord): Int
}

data class PageLegacyMappingRecord(
    var id: Long? = null,
    var sourceSystem: String = "",
    var legacyKey: String = "",
    var sourceUrl: String = "",
    var sourceFingerprint: String = "",
    var pageId: Long = 0,
)
