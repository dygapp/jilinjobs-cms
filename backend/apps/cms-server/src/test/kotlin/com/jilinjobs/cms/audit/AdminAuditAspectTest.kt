package com.jilinjobs.cms.audit

import com.jilinjobs.cms.identity.CmsPrincipal
import com.jilinjobs.cms.identity.CmsRole
import com.jilinjobs.cms.resource.FileMutationJournal
import com.jilinjobs.cms.security.toSpringAuthentication
import java.time.Instant
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.AbstractPlatformTransactionManager
import org.springframework.transaction.support.DefaultTransactionStatus

class AdminAuditAspectTest {
    private val persistence = RecordingAuditPersistence()
    private val transactionManager = RecordingTransactionManager()
    private val transactions = AdminAuditTransactions(transactionManager, persistence)
    private val journal = FileMutationJournal()
    private val target = AuditedTarget(journal)
    private val proxy: AuditedTarget = AspectJProxyFactory(target).apply {
        isProxyTargetClass = true
        addAspect(AdminAuditAspect(transactions, AdminAuditDescriptorResolver(), journal))
    }.getProxy() as AuditedTarget

    @AfterEach
    fun clearSecurityContext() {
        SecurityContextHolder.clearContext()
        journal.abandon()
    }

    @Test
    fun `trusted principal snapshot and returned object id produce succeeded event`() {
        authenticate("review-a", "same-user", CmsRole.ADMIN)

        assertEquals(AuditResultObject(41), proxy.create(SensitiveRequest("token-secret", "password-secret", "rich-body")))

        val attempt = persistence.attempts.single()
        assertEquals("review-a", attempt.identitySource)
        assertEquals("same-user", attempt.userId)
        assertEquals(setOf("admin"), attempt.roles)
        assertEquals("CREATE", attempt.action)
        assertEquals("COLUMN", attempt.objectType)
        assertEquals(AdminAuditResult.SUCCEEDED, persistence.completions.single().result)
        assertEquals("41", persistence.completions.single().objectId)
        val persistedText = listOf(attempt, persistence.completions).joinToString("|")
        assertFalse(persistedText.contains("token-secret"))
        assertFalse(persistedText.contains("password-secret"))
        assertFalse(persistedText.contains("rich-body"))
    }

    @Test
    fun `same user id from different sources remains distinct and roles are snapshots`() {
        authenticate("source-a", "same-user", CmsRole.ADMIN)
        proxy.create(SensitiveRequest("a", "b", "c"))
        authenticate("source-b", "same-user", CmsRole.SUPER)
        proxy.create(SensitiveRequest("d", "e", "f"))

        assertEquals(listOf("source-a", "source-b"), persistence.attempts.map { it.identitySource })
        assertEquals(listOf(setOf("admin"), setOf("super")), persistence.attempts.map { it.roles })
    }

    @Test
    fun `fallback path rejects authenticated cms principal without admin role`() {
        SecurityContextHolder.getContext().authentication =
            CmsPrincipal.fromVerified("source-a", "roleless-user", emptySet()).toSpringAuthentication()

        val error = assertThrows(IllegalStateException::class.java) {
            proxy.create(SensitiveRequest("token", "password", "body"))
        }

        assertEquals("管理审计切点缺少可信 CmsPrincipal", error.message)
        assertEquals(0, target.calls)
        assertTrue(persistence.attempts.isEmpty())
    }

    @Test
    fun `explicit static replacement uses replace action and safe path identity`() {
        authenticate("source-a", "operator", CmsRole.ADMIN)

        proxy.uploadStatic("brand/logo.png", true)

        assertEquals("REPLACE", persistence.attempts.single().action)
        assertEquals("STATIC_RESOURCE", persistence.attempts.single().objectType)
        assertEquals("brand/logo.png", persistence.completions.single().objectId)
    }

    @Test
    fun `domain failure is failed and preserves controlled argument object id`() {
        authenticate("source-a", "operator", CmsRole.ADMIN)

        val error = assertThrows(DomainFailure::class.java) { proxy.fail(73, SensitiveRequest("token", "password", "body")) }

        assertEquals("domain failure", error.message)
        assertEquals(AdminAuditResult.FAILED, persistence.completions.single().result)
        assertEquals("73", persistence.completions.single().objectId)
    }

    @Test
    fun `initial started persistence failure prevents business execution`() {
        authenticate("source-a", "operator", CmsRole.ADMIN)
        persistence.failStart = true

        assertThrows(AuditStoreFailure::class.java) { proxy.create(SensitiveRequest("token", "password", "body")) }

        assertEquals(0, target.calls)
    }

    @Test
    fun `commit failure compensates files and records rolled back`() {
        authenticate("source-a", "operator", CmsRole.ADMIN)
        transactionManager.failCommitNumber = 2

        assertThrows(CommitFailure::class.java) { proxy.mutateFile(82) }

        assertTrue(target.fileRestored)
        assertEquals(
            listOf(AdminAuditResult.SUCCEEDED, AdminAuditResult.ROLLED_BACK),
            persistence.completions.map { it.result },
        )
    }

    @Test
    fun `failed terminal persistence preserves started and original failure`() {
        authenticate("source-a", "operator", CmsRole.ADMIN)
        persistence.failTerminal = true

        val error = assertThrows(DomainFailure::class.java) { proxy.fail(9, SensitiveRequest("token", "password", "body")) }

        assertEquals("domain failure", error.message)
        assertEquals(1, error.suppressed.size)
        assertTrue(persistence.completions.isEmpty())
        assertEquals(1, persistence.attempts.size)
    }

    @Test
    fun `unconfirmed file compensation keeps started and propagates operation failure`() {
        authenticate("source-a", "operator", CmsRole.ADMIN)

        val error = assertThrows(DomainFailure::class.java) { proxy.uncertainFileMutation(10) }

        assertEquals("uncertain file operation", error.message)
        assertEquals(1, error.suppressed.size)
        assertTrue(persistence.completions.isEmpty())
        assertEquals(1, persistence.attempts.size)
    }

    private fun authenticate(source: String, userId: String, role: CmsRole) {
        SecurityContextHolder.getContext().authentication =
            CmsPrincipal.fromVerified(source, userId, setOf(role)).toSpringAuthentication()
    }
}

private data class SensitiveRequest(val token: String, val password: String, val bodyHtml: String)
private data class AuditResultObject(val id: Long)
private data class StaticAuditResult(val path: String)
private class DomainFailure(message: String) : RuntimeException(message)
private class AuditStoreFailure : RuntimeException("audit store unavailable")
private class CommitFailure : RuntimeException("commit failed")

private open class AuditedTarget(
    private val journal: FileMutationJournal,
) {
    var calls = 0
    var fileRestored = false

    @AdminAuditOperation(AdminAuditAction.CREATE, AdminAuditObjectType.COLUMN)
    open fun create(@Suppress("UNUSED_PARAMETER") request: SensitiveRequest): AuditResultObject {
        calls++
        return AuditResultObject(41)
    }

    @AdminAuditOperation(
        AdminAuditAction.UPDATE,
        AdminAuditObjectType.COLUMN,
        AdminAuditObjectIdSource.ARGUMENT,
        "id",
    )
    open fun fail(id: Long, @Suppress("UNUSED_PARAMETER") request: SensitiveRequest): AuditResultObject {
        calls++
        throw DomainFailure("domain failure")
    }

    @AdminAuditOperation(
        AdminAuditAction.UPDATE,
        AdminAuditObjectType.COLUMN,
        AdminAuditObjectIdSource.ARGUMENT,
        "id",
    )
    open fun mutateFile(id: Long): AuditResultObject {
        calls++
        fileRestored = false
        journal.register(compensate = { fileRestored = true })
        return AuditResultObject(id)
    }

    @AdminAuditOperation(
        action = AdminAuditAction.UPLOAD,
        objectType = AdminAuditObjectType.STATIC_RESOURCE,
        returnProperty = "path",
        fallbackObjectIdParameter = "path",
        alternateAction = AdminAuditAction.REPLACE,
        alternateActionParameter = "replace",
    )
    open fun uploadStatic(path: String, @Suppress("UNUSED_PARAMETER") replace: Boolean): StaticAuditResult {
        calls++
        return StaticAuditResult(path)
    }

    @AdminAuditOperation(
        AdminAuditAction.UPDATE,
        AdminAuditObjectType.COLUMN,
        AdminAuditObjectIdSource.ARGUMENT,
        "id",
    )
    open fun uncertainFileMutation(@Suppress("UNUSED_PARAMETER") id: Long): AuditResultObject {
        calls++
        journal.register(compensate = { throw IllegalStateException("compensation unavailable") })
        throw DomainFailure("uncertain file operation")
    }
}

private data class Completion(val auditId: String, val result: AdminAuditResult, val objectId: String?)

private class RecordingAuditPersistence : AdminAuditPersistence {
    val attempts = mutableListOf<AdminAuditAttempt>()
    val completions = mutableListOf<Completion>()
    var failStart = false
    var failTerminal = false

    override fun start(attempt: AdminAuditAttempt) {
        if (failStart) throw AuditStoreFailure()
        attempts += attempt
    }

    override fun complete(auditId: String, result: AdminAuditResult, objectId: String?, completedAt: Instant) {
        if (failTerminal && result != AdminAuditResult.SUCCEEDED) throw AuditStoreFailure()
        completions += Completion(auditId, result, objectId)
    }
}

private class RecordingTransactionManager : AbstractPlatformTransactionManager() {
    var commitCount = 0
    var failCommitNumber: Int? = null

    override fun doGetTransaction(): Any = Any()
    override fun doBegin(transaction: Any, definition: TransactionDefinition) = Unit
    override fun doCommit(status: DefaultTransactionStatus) {
        commitCount++
        if (commitCount == failCommitNumber) throw CommitFailure()
    }
    override fun doRollback(status: DefaultTransactionStatus) = Unit
}
