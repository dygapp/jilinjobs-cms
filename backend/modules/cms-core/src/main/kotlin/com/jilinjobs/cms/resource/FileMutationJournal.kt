package com.jilinjobs.cms.resource

import org.springframework.stereotype.Component

/**
 * Coordinates compensating filesystem mutations with the Server-owned outer transaction.
 * Outside an explicitly opened scope Core and Content Migration preserve their existing behavior.
 */
@Component
class FileMutationJournal {
    private data class Mutation(val compensate: () -> Unit, val cleanup: () -> Unit)

    private val mutations = ThreadLocal<MutableList<Mutation>?>()

    fun begin() {
        check(mutations.get() == null) { "文件补偿范围不能嵌套" }
        mutations.set(mutableListOf())
    }

    fun isActive(): Boolean = mutations.get() != null

    fun register(compensate: () -> Unit, cleanup: () -> Unit = {}) {
        mutations.get()?.add(Mutation(compensate, cleanup))
    }

    fun compensate() {
        val current = mutations.get() ?: return
        var failure: Throwable? = null
        current.asReversed().forEach { mutation ->
            try {
                mutation.compensate()
            } catch (error: Throwable) {
                if (failure == null) failure = error else failure!!.addSuppressed(error)
            }
        }
        mutations.remove()
        failure?.let { throw it }
    }

    fun complete(): List<Throwable> {
        val current = mutations.get() ?: return emptyList()
        val failures = current.mapNotNull { mutation -> runCatching(mutation.cleanup).exceptionOrNull() }
        mutations.remove()
        return failures
    }

    fun abandon() {
        mutations.remove()
    }
}
