package com.jilinjobs.cms.audit

import com.jilinjobs.cms.CmsApplication
import com.jilinjobs.cms.identity.CmsPrincipal
import com.jilinjobs.cms.identity.CmsRole
import com.jilinjobs.cms.security.toSpringAuthentication
import org.springframework.aop.support.AopUtils
import org.springframework.boot.WebApplicationType
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.interceptor.TransactionAspectSupport

fun main() {
    val url = requiredEnvironment("ADMIN_AUDIT_VERIFY_DB_URL")
    val username = System.getenv("ADMIN_AUDIT_VERIFY_DB_USERNAME") ?: "root"
    val password = System.getenv("ADMIN_AUDIT_VERIFY_DB_PASSWORD") ?: "root"
    SpringApplicationBuilder(CmsApplication::class.java, AdminAuditVerificationConfiguration::class.java)
        .web(WebApplicationType.SERVLET)
        .run(
            "--spring.datasource.url=$url",
            "--spring.datasource.username=$username",
            "--spring.datasource.password=$password",
            "--server.port=0",
            "--cms.storage.root=${System.getProperty("java.io.tmpdir")}/eu70-audit-uploads",
            "--cms.static.root=${System.getProperty("java.io.tmpdir")}/eu70-audit-static",
            "--cms.site-package.provision-on-start=false",
            "--cms.site-package.bootstrap-on-start=false",
        )
        .use { context ->
            val jdbc = context.getBean(JdbcTemplate::class.java)
            val probe = context.getBean(AdminAuditProbeService::class.java)
            check(AopUtils.isAopProxy(probe)) { "Audit probe is not AOP proxied" }
            jdbc.execute("DROP TABLE IF EXISTS eu70_audit_probe")
            jdbc.execute("CREATE TABLE eu70_audit_probe(id BIGINT NOT NULL PRIMARY KEY, label VARCHAR(100) NOT NULL) ENGINE=InnoDB")

            verifySchemaAndSensitiveBoundary(jdbc)
            verifySuccessAndPrincipalSnapshots(jdbc, probe)
            verifyDomainFailure(jdbc, probe)
            verifyRollbackOnly(jdbc, probe)
            verifyInitialAuditFailureStopsBusiness(jdbc, probe)
            verifySuccessAuditFailureRollsBackBusiness(jdbc, probe)
            verifyFailureTerminalFailurePreservesStarted(jdbc, probe)
        }
    SecurityContextHolder.clearContext()
    println("EU70_ADMIN_AUDIT_TRANSACTION_VERIFY PASS")
}

private fun verifySchemaAndSensitiveBoundary(jdbc: JdbcTemplate) {
    val columns = jdbc.queryForList(
        """
        SELECT column_name
        FROM information_schema.columns
        WHERE table_schema = DATABASE() AND table_name = 'cms_admin_audit_event'
        ORDER BY ordinal_position
        """.trimIndent(),
        String::class.java,
    )
    check(columns.toSet() == setOf(
        "audit_id", "request_correlation_id", "identity_source", "user_id", "action", "object_type",
        "object_id", "started_at", "completed_at", "result",
    )) { "Audit schema contains unexpected or missing fields: $columns" }
    val migration = jdbc.queryForObject(
        "SELECT COUNT(*) FROM flyway_schema_history WHERE version = '6' AND success = 1",
        Int::class.java,
    )
    check(migration == 1) { "Fresh migration chain did not apply V6" }
}

private fun verifySuccessAndPrincipalSnapshots(jdbc: JdbcTemplate, probe: AdminAuditProbeService) {
    authenticate("source-a", "same-user", CmsRole.ADMIN)
    check(probe.create(101, AuditSensitiveInput("token-A", "password-A", "<p>body-A</p>")) == AuditProbeResult(101))
    authenticate("source-b", "same-user", CmsRole.SUPER)
    probe.create(102, AuditSensitiveInput("token-B", "password-B", "<p>body-B</p>"))

    val rows = jdbc.queryForList(
        """
        SELECT identity_source, user_id, action, object_type, object_id, result
        FROM cms_admin_audit_event
        WHERE user_id = 'same-user'
        ORDER BY started_at, audit_id
        """.trimIndent(),
    )
    check(rows.map { it["identity_source"] } == listOf("source-a", "source-b")) { "Identity sources collapsed: $rows" }
    check(rows.all { it["result"] == "SUCCEEDED" && it["action"] == "CREATE" && it["object_type"] == "COLUMN" })
    check(rows.map { it["object_id"] } == listOf("101", "102"))
    val roles = jdbc.queryForList(
        """
        SELECT e.identity_source, r.role_code
        FROM cms_admin_audit_event e
        JOIN cms_admin_audit_role_snapshot r ON r.audit_id = e.audit_id
        WHERE e.user_id = 'same-user'
        ORDER BY e.identity_source
        """.trimIndent(),
    )
    check(roles.map { "${it["identity_source"]}:${it["role_code"]}" } == listOf("source-a:admin", "source-b:super"))
    val serialized = rows.joinToString("|") + roles.joinToString("|")
    check(listOf("token-A", "password-A", "body-A", "token-B", "password-B", "body-B").none(serialized::contains)) {
        "Sensitive request content reached audit persistence"
    }
}

private fun verifyDomainFailure(jdbc: JdbcTemplate, probe: AdminAuditProbeService) {
    authenticate("source-a", "domain-failure-user", CmsRole.ADMIN)
    val failure = runCatching { probe.fail(201) }.exceptionOrNull()
    check(failure is AuditProbeFailure && failure.message == "domain probe failed")
    check(probeCount(jdbc, 201) == 0) { "Domain failure left business data" }
    check(eventResult(jdbc, "domain-failure-user") == "FAILED")
}

private fun verifyRollbackOnly(jdbc: JdbcTemplate, probe: AdminAuditProbeService) {
    authenticate("source-a", "rollback-only-user", CmsRole.ADMIN)
    val failure = runCatching { probe.rollbackOnly(301) }.exceptionOrNull()
    check(failure is AdminAuditRollbackOnlyException) { "Rollback-only did not fail the call: $failure" }
    check(probeCount(jdbc, 301) == 0) { "Rollback-only left business data" }
    check(eventResult(jdbc, "rollback-only-user") == "ROLLED_BACK")
}

private fun verifyInitialAuditFailureStopsBusiness(jdbc: JdbcTemplate, probe: AdminAuditProbeService) {
    jdbc.execute(
        """
        CREATE TRIGGER eu70_fail_audit_start BEFORE INSERT ON cms_admin_audit_event
        FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'audit start unavailable'
        """.trimIndent(),
    )
    try {
        authenticate("source-a", "start-failure-user", CmsRole.ADMIN)
        check(runCatching { probe.create(401, AuditSensitiveInput("t", "p", "b")) }.isFailure)
        check(probeCount(jdbc, 401) == 0) { "Business executed without STARTED audit" }
    } finally {
        jdbc.execute("DROP TRIGGER IF EXISTS eu70_fail_audit_start")
    }
}

private fun verifySuccessAuditFailureRollsBackBusiness(jdbc: JdbcTemplate, probe: AdminAuditProbeService) {
    jdbc.execute(
        """
        CREATE TRIGGER eu70_fail_audit_success BEFORE UPDATE ON cms_admin_audit_event
        FOR EACH ROW
        BEGIN
            IF NEW.result = 'SUCCEEDED' THEN
                SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'audit success unavailable';
            END IF;
        END
        """.trimIndent(),
    )
    try {
        authenticate("source-a", "success-failure-user", CmsRole.ADMIN)
        check(runCatching { probe.create(501, AuditSensitiveInput("t", "p", "b")) }.isFailure)
        check(probeCount(jdbc, 501) == 0) { "Business data committed when SUCCEEDED audit failed" }
        check(eventResult(jdbc, "success-failure-user") == "ROLLED_BACK")
    } finally {
        jdbc.execute("DROP TRIGGER IF EXISTS eu70_fail_audit_success")
    }
}

private fun verifyFailureTerminalFailurePreservesStarted(jdbc: JdbcTemplate, probe: AdminAuditProbeService) {
    jdbc.execute(
        """
        CREATE TRIGGER eu70_fail_audit_terminal BEFORE UPDATE ON cms_admin_audit_event
        FOR EACH ROW SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'audit terminal unavailable'
        """.trimIndent(),
    )
    try {
        authenticate("source-a", "terminal-failure-user", CmsRole.ADMIN)
        val failure = runCatching { probe.fail(601) }.exceptionOrNull()
        check(failure is AuditProbeFailure && failure.message == "domain probe failed") { "Original failure was masked: $failure" }
        check(failure.suppressed.isNotEmpty()) { "Terminal audit failure was not retained as diagnostic" }
        check(probeCount(jdbc, 601) == 0)
        check(eventResult(jdbc, "terminal-failure-user") == "STARTED")
    } finally {
        jdbc.execute("DROP TRIGGER IF EXISTS eu70_fail_audit_terminal")
    }
}

private fun authenticate(source: String, userId: String, role: CmsRole) {
    SecurityContextHolder.getContext().authentication =
        CmsPrincipal.fromVerified(source, userId, setOf(role)).toSpringAuthentication()
}

private fun probeCount(jdbc: JdbcTemplate, id: Long): Int =
    jdbc.queryForObject("SELECT COUNT(*) FROM eu70_audit_probe WHERE id = ?", Int::class.java, id) ?: 0

private fun eventResult(jdbc: JdbcTemplate, userId: String): String =
    requireNotNull(jdbc.queryForObject(
        "SELECT result FROM cms_admin_audit_event WHERE user_id = ? ORDER BY started_at DESC, audit_id DESC LIMIT 1",
        String::class.java,
        userId,
    ))

private fun requiredEnvironment(name: String): String =
    System.getenv(name)?.takeIf(String::isNotBlank) ?: error("Missing required environment variable: $name")

@TestConfiguration(proxyBeanMethods = false)
private class AdminAuditVerificationConfiguration {
    @Bean fun rollbackMarker(jdbc: JdbcTemplate) = AuditRollbackMarker(jdbc)
    @Bean fun adminAuditProbeService(jdbc: JdbcTemplate, marker: AuditRollbackMarker) = AdminAuditProbeService(jdbc, marker)
}

private data class AuditSensitiveInput(val token: String, val password: String, val bodyHtml: String)
private data class AuditProbeResult(val id: Long)
private class AuditProbeFailure(message: String) : RuntimeException(message)

private open class AdminAuditProbeService(
    private val jdbc: JdbcTemplate,
    private val rollbackMarker: AuditRollbackMarker,
) {
    @AdminAuditOperation(AdminAuditAction.CREATE, AdminAuditObjectType.COLUMN)
    open fun create(id: Long, @Suppress("UNUSED_PARAMETER") input: AuditSensitiveInput): AuditProbeResult {
        jdbc.update("INSERT INTO eu70_audit_probe(id, label) VALUES (?, 'success')", id)
        return AuditProbeResult(id)
    }

    @AdminAuditOperation(
        AdminAuditAction.UPDATE,
        AdminAuditObjectType.COLUMN,
        AdminAuditObjectIdSource.ARGUMENT,
        "id",
    )
    open fun fail(id: Long): AuditProbeResult {
        jdbc.update("INSERT INTO eu70_audit_probe(id, label) VALUES (?, 'failure')", id)
        throw AuditProbeFailure("domain probe failed")
    }

    @AdminAuditOperation(
        AdminAuditAction.UPDATE,
        AdminAuditObjectType.COLUMN,
        AdminAuditObjectIdSource.ARGUMENT,
        "id",
    )
    open fun rollbackOnly(id: Long): AuditProbeResult {
        rollbackMarker.writeAndMarkRollback(id)
        return AuditProbeResult(id)
    }
}

private open class AuditRollbackMarker(
    private val jdbc: JdbcTemplate,
) {
    @Transactional
    open fun writeAndMarkRollback(id: Long) {
        jdbc.update("INSERT INTO eu70_audit_probe(id, label) VALUES (?, 'rollback')", id)
        TransactionAspectSupport.currentTransactionStatus().setRollbackOnly()
    }
}
