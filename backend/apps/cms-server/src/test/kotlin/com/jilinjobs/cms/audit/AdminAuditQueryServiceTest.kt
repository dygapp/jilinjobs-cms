package com.jilinjobs.cms.audit

import java.time.Instant
import java.util.UUID
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class AdminAuditQueryServiceTest {
    private val query = RecordingAdminAuditQuery()
    private val service = AdminAuditQueryService(query)

    @Test
    fun `bounded page returns opaque cursor and decodes it for the next request`() {
        val first = event(3, Instant.parse("2026-09-30T00:00:03Z"))
        val second = event(2, Instant.parse("2026-09-30T00:00:02Z"))
        val third = event(1, Instant.parse("2026-09-30T00:00:01Z"))
        query.rows = listOf(first, second, third)

        val page = service.list(
            identitySource = "source-a",
            userId = "same-user",
            action = "UPDATE",
            objectType = "ARTICLE",
            objectId = "42",
            result = "SUCCEEDED",
            requestCorrelationId = first.requestCorrelationId,
            startedFrom = "2026-09-01T00:00:00Z",
            startedBefore = "2026-10-01T00:00:00Z",
            cursor = null,
            limit = "2",
        )

        assertEquals(listOf(first, second), page.items)
        assertNotNull(page.nextCursor)
        assertEquals(3, query.limit)
        assertEquals("source-a", query.criteria?.identitySource)
        assertEquals("same-user", query.criteria?.userId)
        assertEquals(AdminAuditResult.SUCCEEDED, query.criteria?.result)

        query.rows = emptyList()
        service.list(null, null, null, null, null, null, null, null, null, page.nextCursor, "2")
        assertEquals(AdminAuditCursor(second.startedAt, second.auditId), query.after)
    }

    @Test
    fun `invalid combinations and tokens fail closed before querying storage`() {
        assertThrows(AdminAuditQueryValidationException::class.java) {
            service.list("source-only", null, null, null, null, null, null, null, null, null, null)
        }
        assertThrows(AdminAuditQueryValidationException::class.java) {
            service.list(null, null, "NOT_AN_ACTION", null, null, null, null, null, null, null, null)
        }
        assertThrows(AdminAuditQueryValidationException::class.java) {
            service.list(null, null, null, null, "42", null, null, null, null, null, null)
        }
        assertThrows(AdminAuditQueryValidationException::class.java) {
            service.list(null, null, null, null, null, null, null, "2026-10-01T00:00:00Z", "2026-09-01T00:00:00Z", null, null)
        }
        assertThrows(AdminAuditQueryValidationException::class.java) {
            service.list(null, null, null, null, null, null, null, null, null, "not-a-cursor", null)
        }
        assertThrows(AdminAuditQueryValidationException::class.java) {
            service.list(null, null, null, null, null, null, null, null, null, null, "101")
        }
        assertEquals(0, query.calls)
    }

    @Test
    fun `detail returns immutable persisted projection and unknown id is not found`() {
        val found = event(1, Instant.parse("2026-09-30T00:00:00Z"))
        query.details[found.auditId] = found

        assertEquals(found, service.get(found.auditId))
        assertThrows(AdminAuditEventNotFoundException::class.java) { service.get(UUID.randomUUID().toString()) }
    }

    private fun event(sequence: Int, startedAt: Instant) = AdminAuditEvent(
        auditId = UUID.nameUUIDFromBytes("audit-$sequence".toByteArray()).toString(),
        requestCorrelationId = UUID.nameUUIDFromBytes("request-$sequence".toByteArray()).toString(),
        identitySource = "source-a",
        userId = "same-user",
        roles = listOf("super"),
        action = "UPDATE",
        objectType = "ARTICLE",
        objectId = sequence.toString(),
        startedAt = startedAt,
        completedAt = startedAt.plusMillis(1),
        result = AdminAuditResult.SUCCEEDED,
    )
}

private class RecordingAdminAuditQuery : AdminAuditQuery {
    var rows: List<AdminAuditEvent> = emptyList()
    val details = mutableMapOf<String, AdminAuditEvent>()
    var criteria: AdminAuditCriteria? = null
    var after: AdminAuditCursor? = null
    var limit: Int = 0
    var calls: Int = 0

    override fun list(criteria: AdminAuditCriteria, after: AdminAuditCursor?, limit: Int): List<AdminAuditEvent> {
        calls += 1
        this.criteria = criteria
        this.after = after
        this.limit = limit
        return rows
    }

    override fun find(auditId: String): AdminAuditEvent? = details[auditId]
}
