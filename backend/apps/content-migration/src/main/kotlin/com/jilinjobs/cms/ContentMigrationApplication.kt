package com.jilinjobs.cms

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
    require(args.isNotEmpty()) {
        "用法：content-migration <snapshot-root> [Spring Boot args...]"
    }
    val snapshotRoot = Path.of(args[0]).toAbsolutePath().normalize()
    val context = SpringApplicationBuilder(ContentMigrationApplication::class.java)
        .web(WebApplicationType.NONE)
        .run(*args.drop(1).toTypedArray())
    try {
        val report = context.getBean(GenericContentMigrationService::class.java).importSnapshot(snapshotRoot)
        println("CONTENT_MIGRATION_REPORT " + context.getBean(ObjectMapper::class.java).writeValueAsString(report))
        require(report.conflicts == 0 && report.invalid == 0) {
            "Content migration 存在 conflict/invalid，拒绝静默完成"
        }
    } finally {
        context.close()
    }
}
