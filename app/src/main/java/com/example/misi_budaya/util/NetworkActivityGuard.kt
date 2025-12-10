package com.example.misi_budaya.util

import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

/**
 * Simple guard to indicate when authentication is in progress so background syncs
 * can avoid running concurrently and causing network contention or timeouts.
 */
object NetworkActivityGuard {
    private val authInProgress = AtomicBoolean(false)

    fun setAuthInProgress(value: Boolean) {
        authInProgress.set(value)
    }

    fun isAuthInProgress(): Boolean = authInProgress.get()

    /**
     * Suspend until auth is not in progress or timeout occurs. Returns true if auth finished, false on timeout.
     */
    suspend fun waitForAuthToFinish(timeoutMs: Long = 15_000L): Boolean {
        val start = System.currentTimeMillis()
        while (isAuthInProgress()) {
            val elapsed = System.currentTimeMillis() - start
            if (elapsed >= timeoutMs) return false
            delay(200)
        }
        return true
    }
}
