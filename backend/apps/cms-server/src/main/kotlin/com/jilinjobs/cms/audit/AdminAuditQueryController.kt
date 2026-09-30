package com.jilinjobs.cms.audit

import com.jilinjobs.cms.security.CmsSuperAccess
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.time.format.DateTimeParseException
import java.util.Base64
import java.util.UUID
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

data class AdminAuditEventPage(
    val items: List<AdminAuditEvent>,
    val nextCursor: String?,
)

class AdminAuditQueryValidationException(message: String) : RuntimeException(message)
class AdminAuditEventNotFoundException(auditId: String) : RuntimeException("审计记录不存在：$auditId")

@CmsSuperAccess
@RestController
@RequestMapping("/api/admin/audit-events")
class AdminAuditQueryController(
    private val service: AdminAuditQueryService,
) {
    @GetMapping
    fun list(
        @RequestParam(required = false) identitySource: String?,
        @RequestParam(required = false) userId: String?,
        @RequestParam(required = false) action: String?,
        @RequestParam(required = false) objectType: String?,
        @RequestParam(required = false) objectId: String?,
        @RequestParam(required = false) result: String?,
        @RequestParam(required = false) requestCorrelationId: String?,
        @RequestParam(required = false) startedFrom: String?,
        @RequestParam(required = false) startedBefore: String?,
        @RequestParam(required = false) cursor: String?,
        @RequestParam(required = false) limit: String?,
    ): AdminAuditEventPage = service.list(
        identitySource = identitySource,
        userId = userId,
        action = action,
        objectType = objectType,
        objectId = objectId,
        result = result,
        requestCorrelationId = requestCorrelationId,
        startedFrom = startedFrom,
        startedBefore = startedBefore,
        cursor = cursor,
        limit = limit,
    )

    @GetMapping("/{auditId}")
    fun get(@PathVariable auditId: String): AdminAuditEvent = service.get(auditId)
}

@Service
class AdminAuditQueryService(
    private val query: AdminAuditQuery,
) {
    fun list(
        identitySource: String?,
        userId: String?,
        action: String?,
        objectType: String?,
        objectId: String?,
        result: String?,
        requestCorrelationId: String?,
        startedFrom: String?,
        startedBefore: String?,
        cursor: String?,
        limit: String?,
    ): AdminAuditEventPage {
        val sourceValue = normalized(identitySource, 100, "身份来源")
        val userValue = normalized(userId, 200, "用户 ID")
        if ((sourceValue == null) != (userValue == null)) {
            throw AdminAuditQueryValidationException("身份来源和用户 ID 必须同时提供")
        }

        val objectTypeValue = enumToken<AdminAuditObjectType>(objectType, "对象类型")
        val objectIdValue = normalized(objectId, 500, "对象标识")
        if (objectIdValue != null && objectTypeValue == null) {
            throw AdminAuditQueryValidationException("按对象标识查询时必须同时提供对象类型")
        }

        val from = instant(startedFrom, "开始时间下界")
        val before = instant(startedBefore, "开始时间上界")
        if (from != null && before != null && !from.isBefore(before)) {
            throw AdminAuditQueryValidationException("开始时间下界必须早于上界")
        }

        val pageLimit = limit?.trim()?.takeIf { it.isNotEmpty() }?.toIntOrNull()
            ?: if (limit.isNullOrBlank()) DEFAULT_LIMIT else throw AdminAuditQueryValidationException("每页数量必须是整数")
        if (pageLimit !in 1..MAX_LIMIT) throw AdminAuditQueryValidationException("每页数量必须在 1 到 100 之间")

        val criteria = AdminAuditCriteria(
            identitySource = sourceValue,
            userId = userValue,
            action = enumToken<AdminAuditAction>(action, "动作"),
            objectType = objectTypeValue,
            objectId = objectIdValue,
            result = enumValue<AdminAuditResult>(result, "结果"),
            requestCorrelationId = normalized(requestCorrelationId, 36, "请求关联标识"),
            startedFrom = from,
            startedBefore = before,
        )
        val rows = query.list(criteria, decodeCursor(cursor), pageLimit + 1)
        val items = rows.take(pageLimit)
        return AdminAuditEventPage(
            items = items,
            nextCursor = if (rows.size > pageLimit) items.lastOrNull()?.let(::encodeCursor) else null,
        )
    }

    fun get(auditId: String): AdminAuditEvent = query.find(auditId)
        ?: throw AdminAuditEventNotFoundException(auditId)

    private fun normalized(value: String?, maxLength: Int, label: String): String? {
        val normalized = value?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        if (normalized.length > maxLength) throw AdminAuditQueryValidationException("$label 长度不能超过 $maxLength")
        return normalized
    }

    private fun instant(value: String?, label: String): Instant? {
        val normalized = value?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        return try {
            Instant.parse(normalized)
        } catch (_: DateTimeParseException) {
            throw AdminAuditQueryValidationException("$label 必须是 ISO 时间")
        }
    }

    private inline fun <reified T : Enum<T>> enumToken(value: String?, label: String): String? =
        enumValue<T>(value, label)?.name

    private inline fun <reified T : Enum<T>> enumValue(value: String?, label: String): T? {
        val normalized = value?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        return enumValues<T>().singleOrNull { it.name == normalized }
            ?: throw AdminAuditQueryValidationException("$label 无效：$normalized")
    }

    private fun decodeCursor(value: String?): AdminAuditCursor? {
        val normalized = value?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        return try {
            val decoded = String(Base64.getUrlDecoder().decode(normalized), StandardCharsets.UTF_8)
            val parts = decoded.split('|', limit = 2)
            require(parts.size == 2)
            val instant = Instant.ofEpochMilli(parts[0].toLong())
            val auditId = UUID.fromString(parts[1]).toString()
            require(auditId == parts[1].lowercase())
            AdminAuditCursor(instant, auditId)
        } catch (_: RuntimeException) {
            throw AdminAuditQueryValidationException("分页游标无效")
        }
    }

    private fun encodeCursor(event: AdminAuditEvent): String {
        val raw = "${event.startedAt.toEpochMilli()}|${event.auditId}"
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.toByteArray(StandardCharsets.UTF_8))
    }

    private companion object {
        const val DEFAULT_LIMIT = 20
        const val MAX_LIMIT = 100
    }
}
