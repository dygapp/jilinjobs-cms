package com.jilinjobs.cms.audit

import com.jilinjobs.cms.identity.CmsPrincipal
import com.jilinjobs.cms.identity.CmsRole
import com.jilinjobs.cms.resource.FileMutationJournal
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.lang.reflect.Method
import java.time.Clock
import java.time.Instant
import java.util.UUID
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.LoggerFactory
import org.springframework.aop.support.AopUtils
import org.springframework.beans.BeanWrapperImpl
import org.springframework.core.DefaultParameterNameDiscoverer
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.HandlerMapping
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

internal data class AdminAuditRequestState(
    val attempt: AdminAuditAttempt,
    val descriptor: AdminAuditOperation,
    var invoked: Boolean = false,
    var terminal: Boolean = false,
)

internal const val ADMIN_AUDIT_STATE_ATTRIBUTE = "com.jilinjobs.cms.audit.requestState"

@Component
class AdminAuditTransactions(
    transactionManager: PlatformTransactionManager,
    private val persistence: AdminAuditPersistence,
) {
    private val requiresNew = TransactionTemplate(transactionManager).apply {
        propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRES_NEW
    }
    private val business = TransactionTemplate(transactionManager).apply {
        propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRED
    }

    fun start(attempt: AdminAuditAttempt) {
        requiresNew.executeWithoutResult { persistence.start(attempt) }
    }

    fun <T> business(block: () -> T): T = business.execute { status ->
        val value = block()
        if (status.isRollbackOnly) throw AdminAuditRollbackOnlyException()
        value
    }

    fun completeInBusiness(auditId: String, objectId: String?, completedAt: Instant) {
        persistence.complete(auditId, AdminAuditResult.SUCCEEDED, objectId, completedAt)
    }

    fun completeFailure(auditId: String, result: AdminAuditResult, objectId: String?, completedAt: Instant) {
        require(result == AdminAuditResult.FAILED || result == AdminAuditResult.ROLLED_BACK)
        requiresNew.executeWithoutResult { persistence.complete(auditId, result, objectId, completedAt) }
    }
}

class AdminAuditRollbackOnlyException : RuntimeException("管理写事务已标记回滚")

@Component
class AdminAuditDescriptorResolver {
    private val parameterNames = DefaultParameterNameDiscoverer()

    fun descriptor(method: Method): AdminAuditOperation? =
        AnnotatedElementUtils.findMergedAnnotation(method, AdminAuditOperation::class.java)

    fun effectiveAction(descriptor: AdminAuditOperation, method: Method, args: Array<Any?>): AdminAuditAction {
        val switch = descriptor.alternateActionParameter
        if (switch.isBlank()) return descriptor.action
        val value = argument(method, args, switch)
        return if (value == true) descriptor.alternateAction else descriptor.action
    }

    fun initialObjectId(descriptor: AdminAuditOperation, request: HttpServletRequest): String? {
        val name = if (descriptor.objectIdSource == AdminAuditObjectIdSource.ARGUMENT) {
            descriptor.objectIdParameter
        } else {
            descriptor.fallbackObjectIdParameter
        }
        if (name.isBlank()) return null
        @Suppress("UNCHECKED_CAST")
        val pathVariables = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE) as? Map<String, String>
        return normalizeObjectId(pathVariables?.get(name) ?: request.getParameter(name))
    }

    fun objectId(
        descriptor: AdminAuditOperation,
        method: Method,
        args: Array<Any?>,
        returned: Any?,
    ): String? = when (descriptor.objectIdSource) {
        AdminAuditObjectIdSource.ARGUMENT -> normalizeObjectId(argument(method, args, descriptor.objectIdParameter))
        AdminAuditObjectIdSource.RETURN_PROPERTY ->
            property(returned, descriptor.returnProperty)
                ?: normalizeObjectId(argument(method, args, descriptor.fallbackObjectIdParameter))
    }

    private fun argument(method: Method, args: Array<Any?>, name: String): Any? {
        val names = parameterNames.getParameterNames(method).orEmpty()
        val index = names.indexOf(name)
        return if (index in args.indices) args[index] else null
    }

    private fun property(returned: Any?, name: String): String? {
        val value = if (returned is ResponseEntity<*>) returned.body else returned
        if (value == null || name.isBlank()) return null
        val wrapper = BeanWrapperImpl(value)
        return if (wrapper.isReadableProperty(name)) normalizeObjectId(wrapper.getPropertyValue(name)) else null
    }

    fun normalizeObjectId(value: Any?): String? {
        val text = value?.toString()?.trim()?.takeIf(String::isNotEmpty) ?: return null
        require(text.length <= 500 && text.none(Char::isISOControl)) { "审计对象标识无效" }
        return text
    }
}

@Component
class AdminAuditRequestInterceptor(
    private val transactions: AdminAuditTransactions,
    private val resolver: AdminAuditDescriptorResolver,
) : HandlerInterceptor {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val clock = Clock.systemUTC()

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val handlerMethod = handler as? HandlerMethod ?: return true
        val descriptor = resolver.descriptor(handlerMethod.method) ?: return true
        val principal = currentTrustedAdminPrincipal() ?: return true
        val attempt = AdminAuditAttempt(
            auditId = UUID.randomUUID().toString(),
            requestCorrelationId = UUID.randomUUID().toString(),
            identitySource = principal.identitySource,
            userId = principal.userId,
            roles = principal.roles.mapTo(linkedSetOf()) { it.code },
            action = effectiveRequestAction(descriptor, request).name,
            objectType = descriptor.objectType.name,
            objectId = resolver.initialObjectId(descriptor, request),
            startedAt = Instant.now(clock),
        )
        transactions.start(attempt)
        request.setAttribute(ADMIN_AUDIT_STATE_ATTRIBUTE, AdminAuditRequestState(attempt, descriptor))
        return true
    }

    override fun afterCompletion(request: HttpServletRequest, response: HttpServletResponse, handler: Any, ex: Exception?) {
        val state = request.getAttribute(ADMIN_AUDIT_STATE_ATTRIBUTE) as? AdminAuditRequestState ?: return
        if (state.terminal || state.invoked || response.status < 400) return
        try {
            transactions.completeFailure(
                state.attempt.auditId,
                AdminAuditResult.FAILED,
                state.attempt.objectId,
                Instant.now(clock),
            )
            state.terminal = true
        } catch (_: Throwable) {
            logger.error(
                "Admin audit input failure terminal write failed auditId={} correlationId={}",
                state.attempt.auditId,
                state.attempt.requestCorrelationId,
            )
        }
    }

    private fun effectiveRequestAction(descriptor: AdminAuditOperation, request: HttpServletRequest): AdminAuditAction {
        val switch = descriptor.alternateActionParameter
        val enabled = request.getParameter(switch)?.trim()?.lowercase() in setOf("true", "on", "yes", "1")
        return if (switch.isNotBlank() && enabled) {
            descriptor.alternateAction
        } else {
            descriptor.action
        }
    }
}

@Component
class AdminAuditWebConfiguration(
    private val interceptor: AdminAuditRequestInterceptor,
) : WebMvcConfigurer {
    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(interceptor).addPathPatterns("/api/admin/**")
    }
}

@Aspect
@Component
class AdminAuditAspect(
    private val transactions: AdminAuditTransactions,
    private val resolver: AdminAuditDescriptorResolver,
    private val fileMutations: FileMutationJournal,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val clock = Clock.systemUTC()

    @Around("@annotation(descriptor)")
    fun audit(joinPoint: ProceedingJoinPoint, descriptor: AdminAuditOperation): Any? {
        val signature = joinPoint.signature as MethodSignature
        val method = AopUtils.getMostSpecificMethod(signature.method, joinPoint.target.javaClass)
        val request = currentRequest()
        val state = (request?.getAttribute(ADMIN_AUDIT_STATE_ATTRIBUTE) as? AdminAuditRequestState)
            ?: standaloneState(descriptor, method, joinPoint.args)
        state.invoked = true
        fileMutations.begin()
        var methodReturned = false
        var objectId = state.attempt.objectId
        try {
            val returned = transactions.business {
                val value = joinPoint.proceed()
                methodReturned = true
                objectId = resolver.objectId(descriptor, method, joinPoint.args, value) ?: objectId
                transactions.completeInBusiness(state.attempt.auditId, objectId, Instant.now(clock))
                value
            }
            state.terminal = true
            fileMutations.complete().forEach {
                logger.error(
                    "Admin audit file-stage cleanup failed auditId={} correlationId={}",
                    state.attempt.auditId,
                    state.attempt.requestCorrelationId,
                )
            }
            return returned
        } catch (original: Throwable) {
            val compensationFailure = runCatching { fileMutations.compensate() }.exceptionOrNull()
            if (compensationFailure != null) {
                original.addSuppressed(compensationFailure)
                logger.error(
                    "Admin audit file compensation failed auditId={} correlationId={}",
                    state.attempt.auditId,
                    state.attempt.requestCorrelationId,
                )
                throw original
            }
            val result = if (methodReturned) AdminAuditResult.ROLLED_BACK else AdminAuditResult.FAILED
            try {
                transactions.completeFailure(state.attempt.auditId, result, objectId, Instant.now(clock))
                state.terminal = true
            } catch (terminalFailure: Throwable) {
                original.addSuppressed(terminalFailure)
                logger.error(
                    "Admin audit failure terminal write failed auditId={} correlationId={}",
                    state.attempt.auditId,
                    state.attempt.requestCorrelationId,
                )
            }
            throw original
        }
    }

    private fun standaloneState(
        descriptor: AdminAuditOperation,
        method: Method,
        args: Array<Any?>,
    ): AdminAuditRequestState {
        val principal = currentTrustedAdminPrincipal()
            ?: error("管理审计切点缺少可信 CmsPrincipal")
        val attempt = AdminAuditAttempt(
            auditId = UUID.randomUUID().toString(),
            requestCorrelationId = UUID.randomUUID().toString(),
            identitySource = principal.identitySource,
            userId = principal.userId,
            roles = principal.roles.mapTo(linkedSetOf()) { it.code },
            action = resolver.effectiveAction(descriptor, method, args).name,
            objectType = descriptor.objectType.name,
            objectId = resolver.objectId(descriptor, method, args, null),
            startedAt = Instant.now(clock),
        )
        transactions.start(attempt)
        return AdminAuditRequestState(attempt, descriptor)
    }

    private fun currentRequest(): HttpServletRequest? =
        (org.springframework.web.context.request.RequestContextHolder.getRequestAttributes()
            as? org.springframework.web.context.request.ServletRequestAttributes)?.request
}

private fun currentTrustedAdminPrincipal(): CmsPrincipal? {
    val authentication = SecurityContextHolder.getContext().authentication ?: return null
    if (!authentication.isAuthenticated) return null
    val principal = authentication.principal as? CmsPrincipal ?: return null
    return principal.takeIf { CmsRole.ADMIN in it.roles || CmsRole.SUPER in it.roles }
}
