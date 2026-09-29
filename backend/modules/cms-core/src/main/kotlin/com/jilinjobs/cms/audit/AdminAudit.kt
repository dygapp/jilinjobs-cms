package com.jilinjobs.cms.audit

import java.sql.Timestamp
import java.time.Instant
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

enum class AdminAuditResult {
    STARTED,
    SUCCEEDED,
    FAILED,
    ROLLED_BACK,
}

data class AdminAuditAttempt(
    val auditId: String,
    val requestCorrelationId: String,
    val identitySource: String,
    val userId: String,
    val roles: Set<String>,
    val action: String,
    val objectType: String,
    val objectId: String?,
    val startedAt: Instant,
)

/** Framework-neutral append/terminal-transition contract. It intentionally exposes no query API. */
interface AdminAuditPersistence {
    fun start(attempt: AdminAuditAttempt)

    fun complete(auditId: String, result: AdminAuditResult, objectId: String?, completedAt: Instant)
}

@Repository
class JdbcAdminAuditPersistence(
    private val jdbc: JdbcTemplate,
) : AdminAuditPersistence {
    override fun start(attempt: AdminAuditAttempt) {
        require(attempt.roles.isNotEmpty()) { "审计角色快照不能为空" }
        require(attempt.auditId.length == 36 && attempt.requestCorrelationId.length == 36) { "审计标识格式无效" }
        jdbc.update(
            """
            INSERT INTO cms_admin_audit_event(
                audit_id, request_correlation_id, identity_source, user_id,
                action, object_type, object_id, started_at, result
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'STARTED')
            """.trimIndent(),
            attempt.auditId,
            attempt.requestCorrelationId,
            attempt.identitySource,
            attempt.userId,
            attempt.action,
            attempt.objectType,
            attempt.objectId,
            Timestamp.from(attempt.startedAt),
        )
        attempt.roles.toSortedSet().forEach { role ->
            jdbc.update(
                "INSERT INTO cms_admin_audit_role_snapshot(audit_id, role_code) VALUES (?, ?)",
                attempt.auditId,
                role,
            )
        }
    }

    override fun complete(auditId: String, result: AdminAuditResult, objectId: String?, completedAt: Instant) {
        require(result != AdminAuditResult.STARTED) { "STARTED 不是终态" }
        val updated = jdbc.update(
            """
            UPDATE cms_admin_audit_event
            SET result = ?, object_id = COALESCE(?, object_id), completed_at = ?
            WHERE audit_id = ? AND result = 'STARTED'
            """.trimIndent(),
            result.name,
            objectId,
            Timestamp.from(completedAt),
            auditId,
        )
        check(updated == 1) { "审计尝试不存在或已终结：$auditId" }
    }
}
