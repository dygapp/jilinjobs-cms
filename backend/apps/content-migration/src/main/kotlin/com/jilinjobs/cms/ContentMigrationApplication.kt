package com.jilinjobs.cms

import com.jilinjobs.cms.migration.PartyCarouselMigrationV2Service
import com.jilinjobs.cms.migration.PartyHistoricalContentMigrationV2Service
import com.jilinjobs.cms.migration.generic.GenericContentMigrationService
import java.nio.file.Path
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import tools.jackson.databind.ObjectMapper

@SpringBootApplication
class ContentMigrationApplication

typealias CmsApplication = ContentMigrationApplication

fun main(args: Array<String>) {
    require(args.size >= 2) {
        "用法：content-migration <generic-content|party-content|party-carousel> <snapshot-root> [Spring Boot args...]"
    }
    val command = args[0]
    val snapshotRoot = Path.of(args[1]).toAbsolutePath().normalize()
    val context = SpringApplicationBuilder(ContentMigrationApplication::class.java)
        .web(WebApplicationType.NONE)
        .run(*args.drop(2).toTypedArray())
    try {
        val objectMapper = context.getBean(ObjectMapper::class.java)
        when (command) {
            "generic-content" -> {
                val report = context.getBean(GenericContentMigrationService::class.java).importSnapshot(snapshotRoot)
                println("CONTENT_MIGRATION_REPORT ${objectMapper.writeValueAsString(report)}")
                require(report.conflicts == 0 && report.invalid == 0) {
                    "Generic content migration 存在 conflict/invalid，拒绝静默完成"
                }
            }
            "party-content" -> {
                val report = context.getBean(PartyHistoricalContentMigrationV2Service::class.java).importSnapshot(snapshotRoot)
                println("EU29_IMPORT_REPORT ${objectMapper.writeValueAsString(report)}")
                require(report.conflicts == 0 && report.invalid == 0) {
                    "Party historical import 存在 conflict/invalid，拒绝静默完成"
                }
            }
            "party-carousel" -> {
                val report = context.getBean(PartyCarouselMigrationV2Service::class.java).importSnapshot(snapshotRoot)
                println("EU29_CAROUSEL_IMPORT_REPORT ${objectMapper.writeValueAsString(report)}")
                require(report.conflicts == 0 && report.invalid == 0) {
                    "Party carousel import 存在 conflict/invalid，拒绝静默完成"
                }
            }
            else -> error("不支持的 migration command：$command")
        }
    } finally {
        context.close()
    }
}
