package com.jilinjobs.cms.migration

import com.jilinjobs.cms.ContentMigrationApplication
import com.jilinjobs.cms.migration.party.PartyMigrationFacade
import java.nio.file.Path
import org.springframework.boot.WebApplicationType
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.stereotype.Service
import tools.jackson.databind.ObjectMapper

@Service
class PartyHistoricalContentMigrationV2Service(
    private val facade: PartyMigrationFacade,
) {
    fun importSnapshot(snapshotRoot: Path): PartySnapshotImportReport = facade.importArticles(snapshotRoot)
}

fun main(args: Array<String>) {
    require(args.isNotEmpty()) { "用法：importPartyHistoricalContent <snapshot-root> [Spring Boot args...]" }
    val snapshotRoot = Path.of(args.first()).toAbsolutePath().normalize()
    val context = SpringApplicationBuilder(ContentMigrationApplication::class.java)
        .web(WebApplicationType.NONE)
        .run(*args.drop(1).toTypedArray())
    try {
        val report = context.getBean(PartyHistoricalContentMigrationV2Service::class.java).importSnapshot(snapshotRoot)
        println("EU29_IMPORT_REPORT ${context.getBean(ObjectMapper::class.java).writeValueAsString(report)}")
        require(report.conflicts == 0 && report.invalid == 0) { "Party historical import 存在 conflict/invalid，拒绝静默完成" }
    } finally {
        context.close()
    }
}
