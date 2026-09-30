package com.jilinjobs.cms.audit

import java.sql.Timestamp
import java.time.Instant
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

data class AdminAuditEvent(
    val auditId: String,
    val requestCorrelationId: String,
    val identitySource: String,
    val userId: String,
    val roles: List<String>,
    val action: String,
    val objectType: String,
    val objectId: String?,
    val startedAt: Instant,
    val completedAt: Instant?,
    val result: AdminAuditResult,
)

data class AdminAuditCriteria(
    val identitySource: String? = null,
    val userId: String? = null,
    val action: String? = null,
    val objectType: String? = null,
    val objectId: String? = null,
    val result: AdminAuditResult? = null,
    val requestCorrelationId: String? = null,
    val startedFrom: Instant? = null,
    val startedBefore: Instant? = null,
)

data class AdminAuditCursor(
    val startedAt: Instant,
    val auditId: String,
)

/** Read-only audit contract. Mutation remains exclusively on [AdminAuditPersistence]. */
interface AdminAuditQuery {
    fun list(criteria: AdminAuditCriteria, after: AdminAuditCursor?, limit: Int): List<AdminAuditEvent>

    fun find(auditId: String): AdminAuditEvent?
}

@Repository
class JdbcAdminAuditQuery(
    private val jdbc: JdbcTemplate,
) : AdminAuditQuery {
    override fun list(criteria: AdminAuditCriteria, after: AdminAuditCursor?, limit: Int): List<AdminAuditEvent> {
        require(limit in 1..101) { "审计查询内部 limit 必须在 1 到 101 之间" }
        val predicates = mutableListOf<String>()
        val arguments = mutableListOf<Any>()

        fun exact(column: String, value: String?) {
            value?.let {
                predicates += "e.$column = ?"
                arguments += it
            }
        }

        exact("identity_source", criteria.identitySource)
        exact("user_id", criteria.userId)
        exact("action", criteria.action)
        exact("object_type", criteria.objectType)
        exact("object_id", criteria.objectId)
        exact("result", criteria.result?.name)
        exact("request_correlation_id", criteria.requestCorrelationId)
        criteria.startedFrom?.let {
            predicates += "e.started_at >= ?"
            arguments += Timestamp.from(it)
        }
        criteria.startedBefore?.let {
            predicates += "e.started_at < ?"
            arguments += Timestamp.from(it)
        }
        after?.let {
            predicates += "(e.started_at < ? OR (e.started_at = ? AND e.audit_id < ?))"
            arguments += Timestamp.from(it.startedAt)
            arguments += Timestamp.from(it.startedAt)
            arguments += it.auditId
        }

        val where = predicates.takeIf { it.isNotEmpty() }?.joinToString(" AND ", prefix = "WHERE ").orEmpty()
        arguments += limit
        val rows = jdbc.query(
            """
            SELECT e.audit_id, e.request_correlation_id, e.identity_source, e.user_id,
                   e.action, e.object_type, e.object_id, e.started_at, e.completed_at, e.result
            FROM cms_admin_audit_event e
            $where
            ORDER BY e.started_at DESC, e.audit_id DESC
            LIMIT ?
            """.trimIndent(),
            { result, _ ->
                AdminAuditEventRow(
                    auditId = result.getString("audit_id"),
                    requestCorrelationId = result.getString("request_correlation_id"),
                    identitySource = result.getString("identity_source"),
                    userId = result.getString("user_id"),
                    action = result.getString("action"),
                    objectType = result.getString("object_type"),
                    objectId = result.getString("object_id"),
                    startedAt = result.getTimestamp("started_at").toInstant(),
                    completedAt = result.getTimestamp("completed_at")?.toInstant(),
                    result = AdminAuditResult.valueOf(result.getString("result")),
                )
            },
            *arguments.toTypedArray(),
        )
        return attachRoles(rows)
    }

    override fun find(auditId: String): AdminAuditEvent? {
        val rows = jdbc.query(
            """
            SELECT e.audit_id, e.request_correlation_id, e.identity_source, e.user_id,
                   e.action, e.object_type, e.object_id, e.started_at, e.completed_at, e.result
            FROM cms_admin_audit_event e
            WHERE e.audit_id = ?
            """.trimIndent(),
            { result, _ ->
                AdminAuditEventRow(
                    auditId = result.getString("audit_id"),
                    requestCorrelationId = result.getString("request_correlation_id"),
                    identitySource = result.getString("identity_source"),
                    userId = result.getString("user_id"),
                    action = result.getString("action"),
                    objectType = result.getString("object_type"),
                    objectId = result.getString("object_id"),
                    startedAt = result.getTimestamp("started_at").toInstant(),
                    completedAt = result.getTimestamp("completed_at")?.toInstant(),
                    result = AdminAuditResult.valueOf(result.getString("result")),
                )
            },
            auditId,
        )
        return attachRoles(rows).singleOrNull()
    }

    private fun attachRoles(rows: List<AdminAuditEventRow>): List<AdminAuditEvent> {
        if (rows.isEmpty()) return emptyList()
        val placeholders = List(rows.size) { "?" }.joinToString(",")
        val roles = linkedMapOf<String, MutableList<String>>()
        jdbc.query(
            """
            SELECT audit_id, role_code
            FROM cms_admin_audit_role_snapshot
            WHERE audit_id IN ($placeholders)
            ORDER BY audit_id, role_code
            """.trimIndent(),
            { result -> roles.getOrPut(result.getString("audit_id")) { mutableListOf() }.add(result.getString("role_code")) },
            *rows.map { it.auditId }.toTypedArray(),
        )
        return rows.map { row ->
            val roleSnapshot = roles[row.auditId]?.toList().orEmpty()
            check(roleSnapshot.isNotEmpty()) { "审计角色快照缺失：${row.auditId}" }
            AdminAuditEvent(
                auditId = row.auditId,
                requestCorrelationId = row.requestCorrelationId,
                identitySource = row.identitySource,
                userId = row.userId,
                roles = roleSnapshot,
                action = row.action,
                objectType = row.objectType,
                objectId = row.objectId,
                startedAt = row.startedAt,
                completedAt = row.completedAt,
                result = row.result,
            )
        }
    }
}

private data class AdminAuditEventRow(
    val auditId: String,
    val requestCorrelationId: String,
    val identitySource: String,
    val userId: String,
    val action: String,
    val objectType: String,
    val objectId: String?,
    val startedAt: Instant,
    val completedAt: Instant?,
    val result: AdminAuditResult,
)
