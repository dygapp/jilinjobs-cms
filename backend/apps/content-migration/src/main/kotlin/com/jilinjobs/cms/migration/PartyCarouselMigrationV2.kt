package com.jilinjobs.cms.migration

import com.jilinjobs.cms.ContentMigrationApplication
import com.jilinjobs.cms.listing.CmsListItemSourceType
import com.jilinjobs.cms.migration.party.PartyMigrationFacade
import java.nio.file.Path
import org.springframework.boot.WebApplicationType
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper

data class PartyCarouselArticleRef(
    val sourceSystem: String,
    val legacyKey: String,
)

data class PartyCarouselPlacementItem(
    val legacyKey: String,
    val sourceOrder: Int,
    val sourceType: CmsListItemSourceType = CmsListItemSourceType.LINK,
    val title: String,
    val url: String? = null,
    val articleRef: PartyCarouselArticleRef? = null,
    val openMode: String,
    val sourceFingerprint: String,
    val image: PartyCarouselSnapshotImage,
    val evidence: PartyCarouselSnapshotEvidence,
)

enum class PartyCarouselPlacementImportStatus { CREATED, UPDATED, SKIPPED, CONFLICT, INVALID }

data class PartyCarouselPlacementImportResult(
    val legacyKey: String,
    val status: PartyCarouselPlacementImportStatus,
    val listItemId: Long? = null,
    val message: String? = null,
)

data class PartyCarouselPlacementImportReport(
    val total: Int,
    val created: Int,
    val updated: Int,
    val skipped: Int,
    val conflicts: Int,
    val invalid: Int,
    val results: List<PartyCarouselPlacementImportResult>,
)

@Service
class PartyCarouselMigrationV2Service(
    private val facade: PartyMigrationFacade,
) {
    fun importSnapshot(snapshotRoot: Path): PartyCarouselPlacementImportReport = facade.importCarousel(snapshotRoot)
}

fun main(args: Array<String>) {
    require(args.isNotEmpty()) { "用法：importPartyCarousel <snapshot-root> [Spring Boot args...]" }
    val snapshotRoot = Path.of(args.first()).toAbsolutePath().normalize()
    val context = SpringApplicationBuilder(ContentMigrationApplication::class.java)
        .web(WebApplicationType.NONE)
        .run(*args.drop(1).toTypedArray())
    try {
        val report = context.getBean(PartyCarouselMigrationV2Service::class.java).importSnapshot(snapshotRoot)
        println("EU29_CAROUSEL_IMPORT_REPORT ${context.getBean(ObjectMapper::class.java).writeValueAsString(report)}")
        require(report.conflicts == 0 && report.invalid == 0) { "Party carousel import 存在 conflict/invalid，拒绝静默完成" }
    } finally {
        context.close()
    }
}
