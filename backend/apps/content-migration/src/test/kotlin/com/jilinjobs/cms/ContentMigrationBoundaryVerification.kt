package com.jilinjobs.cms

import com.jilinjobs.cms.column.ColumnMapper
import com.jilinjobs.cms.migration.PartyHistoricalContentMigrationV2Service
import com.jilinjobs.cms.provisioning.SitePackageLoader
import javax.sql.DataSource
import org.springframework.boot.WebApplicationType
import org.springframework.boot.builder.SpringApplicationBuilder

fun main() {
    val dbUrl = requireNotNull(System.getenv("MIGRATION_VERIFY_DB_URL")) {
        "MIGRATION_VERIFY_DB_URL is required"
    }
    require(runCatching { Class.forName("com.jilinjobs.cms.column.ColumnController") }.isFailure) {
        "Content Migration classpath contains Server ColumnController"
    }
    require(runCatching { Class.forName("com.jilinjobs.cms.common.ApiExceptionHandler") }.isFailure) {
        "Content Migration classpath contains Server ApiExceptionHandler"
    }
    require(runCatching { Class.forName("org.springframework.web.servlet.DispatcherServlet") }.isFailure) {
        "Content Migration classpath contains Spring MVC DispatcherServlet"
    }

    val context = SpringApplicationBuilder(ContentMigrationApplication::class.java)
        .web(WebApplicationType.NONE)
        .run(
            "--spring.datasource.url=$dbUrl",
            "--spring.datasource.username=${System.getenv("MIGRATION_VERIFY_DB_USERNAME") ?: "root"}",
            "--spring.datasource.password=${System.getenv("MIGRATION_VERIFY_DB_PASSWORD") ?: "root"}",
            "--spring.main.banner-mode=off",
        )
    try {
        require(!context.javaClass.name.contains("Servlet", ignoreCase = true)) {
            "Content Migration unexpectedly created a servlet application context: ${context.javaClass.name}"
        }
        context.getBean(SitePackageLoader::class.java)
        context.getBean(ColumnMapper::class.java)
        context.getBean(PartyHistoricalContentMigrationV2Service::class.java)
        val dataSource = context.getBean(DataSource::class.java)
        dataSource.connection.use { connection ->
            connection.prepareStatement("SELECT COUNT(*) FROM flyway_schema_history WHERE success=1").use { statement ->
                statement.executeQuery().use { result ->
                    require(result.next() && result.getInt(1) >= 2) { "Fresh Migration context did not apply the Generic CMS Flyway lineage" }
                }
            }
            connection.prepareStatement("SELECT COUNT(*) FROM cms_column").use { statement -> statement.executeQuery().close() }
        }
        println("EU46_CONTENT_MIGRATION_BOUNDARY_VERIFY PASS")
    } finally {
        context.close()
    }
}
