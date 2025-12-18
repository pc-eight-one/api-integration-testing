package dev.codersbox.eng.lib.cli.execution

import kotlinx.coroutines.delay
import kotlin.math.pow

class RetryExecutor(
    private val maxRetries: Int = 3,
    private val initialDelay: Long = 1000,
    private val maxDelay: Long = 10000,
    private val exponentialBackoff: Boolean = true
) {
    suspend fun <T> executeWithRetry(
        operation: suspend () -> T,
        shouldRetry: (Throwable) -> Boolean = { true }
    ): T {
        var lastException: Throwable? = null
        
        repeat(maxRetries + 1) { attempt ->
            try {
                return operation()
            } catch (e: Throwable) {
                lastException = e
                
                if (attempt < maxRetries && shouldRetry(e)) {
                    val delayTime = calculateDelay(attempt)
                    println("Attempt ${attempt + 1} failed: ${e.message}. Retrying in ${delayTime}ms...")
                    delay(delayTime)
                } else {
                    throw e
                }
            }
        }
        
        throw lastException!!
    }

    private fun calculateDelay(attempt: Int): Long {
        return if (exponentialBackoff) {
            minOf(
                (initialDelay * 2.0.pow(attempt)).toLong(),
                maxDelay
            )
        } else {
            initialDelay
        }
    }
}
