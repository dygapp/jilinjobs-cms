package com.jilinjobs.cms.audit

import com.jilinjobs.cms.CmsApplication
import java.time.Instant
import org.springframework.boot.WebApplicationType
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.jdbc.core.JdbcTemplate

fun main() {
    val url = requiredQueryEnvironment("ADMIN_AUDIT_QUERY_VERIFY_DB_URL")
    val username = System.getenv("ADMIN_AUDIT_QUERY_VERIFY_DB_USERNAME") ?: "root"
    val password = System.getenv("ADMIN_AUDIT_QUERY_VERIFY_DB_PASSWORD") ?: "root"
    SpringApplicationBuilder(CmsApplication::class.java)
        .web(WebApplicationType.SERVLET)
        .run(
            "--spring.datasource.url=$url",
            "--spring.datasource.username=$username",
            "--spring.datasource.password=$password",
            "--server.port=0",
            "--cms.storage.root=${System.getProperty("java.io.tmpdir")}/eu71-audit-query-uploads",
            "--cms.static.root=${System.getProperty("java.io.tmpdir")}/eu71-audit-query-static",
            "--cms.site-package.provision-on-start=false",
            "--cms.site-package.bootstrap-on-start=false",
        )
        .use { context ->
            val jdbc = context.getBean(JdbcTemplate::class.java)
            val query = context.getBean(AdminAuditQuery::class.java)
            verifyQueryMigration(jdbc)
            insertQueryFixtures(jdbc)
            verifyCursorAndSnapshots(query)
            verifyFiltersAndDetail(query)
        }
    println("EU71_ADMIN_AUDIT_QUERY_VERIFY PASS")
}

private fun verifyQueryMigration(jdbc: JdbcTemplate) {
    val migration = jdbc.queryForObject(
        "SELECT COUNT(*) FROM flyway_schema_history WHERE version = '7' AND success = 1",
        Int::class.java,
    )
    check(migration == 1) { "Fresh migration chain did not apply V7" }
    val indexes = jdbc.queryForList(
        """
        SELECT DISTINCT index_name
        FROM information_schema.statistics
        WHERE table_schema = DATABASE() AND table_name = 'cms_admin_audit_event'
        """.trimIndent(),
        String::class.java,
    ).toSet()
    check("idx_cms_admin_audit_started" in indexes && "idx_cms_admin_audit_action" in indexes) {
        "EU-71 query indexes missing: $indexes"
    }
}

private fun insertQueryFixtures(jdbc: JdbcTemplate) {
    data class Fixture(
        val sequence: Int,
        val source: String,
        val user: String,
        val action: String,
        val objectType: String,
        val objectId: String?,
        val startedAt: String,
        val completedAt: String?,
        val result: String,
        val role: String,
    )
    val fixtures = listOf(
        Fixture(1, "source-a", "user-a", "CREATE", "ARTICLE", "10", "2026-09-30 01:00:00.000", "2026-09-30 01:00:00.100", "SUCCEEDED", "admin"),
        Fixture(2, "source-b", "user-a", "UPDATE", "ARTICLE", "10", "2026-09-30 02:00:00.000", "2026-09-30 02:00:00.100", "FAILED", "super"),
        Fixture(3, "source-a", "user-b", "DELETE", "PAGE", "20", "2026-09-30 03:00:00.000", "2026-09-30 03:00:00.100", "ROLLED_BACK", "super"),
        Fixture(4, "source-a", "user-a", "UPDATE", "ARTICLE", null, "2026-09-30 03:00:00.000", null, "STARTED", "admin"),
    )
    fixtures.forEach { fixture ->
        val auditId = id(fixture.sequence)
        jdbc.update(
            """
            INSERT INTO cms_admin_audit_event(
                audit_id, request_correlation_id, identity_source, user_id,
                action, object_type, object_id, started_at, completed_at, result
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            auditId,
            requestId(fixture.sequence),
            fixture.source,
            fixture.user,
            fixture.action,
            fixture.objectType,
            fixture.objectId,
            fixture.startedAt,
            fixture.completedAt,
            fixture.result,
        )
        jdbc.update(
            "INSERT INTO cms_admin_audit_role_snapshot(audit_id, role_code) VALUES (?, ?)",
            auditId,
            fixture.role,
        )
    }
}

private fun verifyCursorAndSnapshots(query: AdminAuditQuery) {
    val first = query.list(AdminAuditCriteria(), after = null, limit = 3)
    check(first.map { it.auditId } == listOf(id(4), id(3), id(2))) { "Unexpected first cursor page: $first" }
    check(first.all { it.roles.size == 1 }) { "Role snapshot was not batch loaded: $first" }

    val cursor = AdminAuditCursor(first[1].startedAt, first[1].auditId)
    val second = query.list(AdminAuditCriteria(), after = cursor, limit = 10)
    check(second.map { it.auditId } == listOf(id(2), id(1))) { "Cursor page repeated or skipped rows: $second" }
    check(first[0].result == AdminAuditResult.STARTED && first[0].completedAt == null)
}

private fun verifyFiltersAndDetail(query: AdminAuditQuery) {
    val filtered = query.list(
        AdminAuditCriteria(
            identitySource = "source-b",
            userId = "user-a",
            action = "UPDATE",
            objectType = "ARTICLE",
            objectId = "10",
            result = AdminAuditResult.FAILED,
            requestCorrelationId = requestId(2),
            startedFrom = Instant.parse("2026-09-30T01:30:00Z"),
            startedBefore = Instant.parse("2026-09-30T02:30:00Z"),
        ),
        after = null,
        limit = 10,
    )
    check(filtered.map { it.auditId } == listOf(id(2))) { "Combined audit filters are incorrect: $filtered" }

    val detail = requireNotNull(query.find(id(3)))
    check(detail.roles == listOf("super") && detail.objectId == "20" && detail.result == AdminAuditResult.ROLLED_BACK)
    check(query.find("ffffffff-ffff-ffff-ffff-ffffffffffff") == null)
    val serialized = (filtered + detail).joinToString("|")
    check(listOf("password", "token", "bodyHtml", "stackTrace", "exceptionMessage").none(serialized::contains)) {
        "Audit query projection exposed excluded fields"
    }
}

private fun id(sequence: Int) = "00000000-0000-0000-0000-${sequence.toString().padStart(12, '0')}"
private fun requestId(sequence: Int) = "10000000-0000-0000-0000-${sequence.toString().padStart(12, '0')}"

private fun requiredQueryEnvironment(name: String): String =
    System.getenv(name)?.takeIf(String::isNotBlank) ?: error("Missing required environment variable: $name")
