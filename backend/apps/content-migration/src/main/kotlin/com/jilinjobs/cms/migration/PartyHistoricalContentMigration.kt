package com.jilinjobs.cms.migration

import com.jilinjobs.cms.content.ArticleType
import java.time.LocalDate

data class PartyMigrationSource(
    val system: String,
    val legacyKey: String,
    val contentId: String?,
    val typeCode: String,
    val detailPath: String,
    val url: String,
)

data class PartyMigrationTarget(
    val columnAlias: String,
    val articleType: ArticleType,
)

data class PartyMigrationContent(
    val title: String,
    val source: String,
    val publishDate: LocalDate?,
    val bodyHtml: String,
    val externalUrl: String?,
)

data class PartyMigrationResource(
    val role: String,
    val sourceUrl: String,
    val originalReference: String,
    val token: String,
    val snapshotPath: String,
    val sha256: String,
    val contentType: String?,
    val sizeBytes: Long,
)

data class PartyMigrationEvidence(
    val listPage: Int,
    val listTitle: String,
    val listPublishDate: LocalDate?,
    val sourceOrder: Int,
    val detailPublishDate: LocalDate? = null,
    val rawDetailPath: String? = null,
)

data class PartyMigrationRecord(
    val source: PartyMigrationSource,
    val target: PartyMigrationTarget,
    val content: PartyMigrationContent,
    val resources: List<PartyMigrationResource>,
    val sourceFingerprint: String,
    val evidence: PartyMigrationEvidence,
)

data class PartyCanonicalIndexEntry(
    val legacyKey: String,
    val path: String,
    val sourceFingerprint: String? = null,
    val columnAlias: String? = null,
    val articleType: String? = null,
)

enum class PartyRecordImportStatus { CREATED, SKIPPED, CONFLICT, INVALID }

data class PartyRecordImportResult(
    val legacyKey: String,
    val status: PartyRecordImportStatus,
    val articleId: Long? = null,
    val message: String? = null,
)

data class PartySnapshotImportReport(
    val total: Int,
    val created: Int,
    val skipped: Int,
    val conflicts: Int,
    val invalid: Int,
    val results: List<PartyRecordImportResult>,
)
