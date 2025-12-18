package dev.codersbox.eng.lib.api.testing.context

import java.util.concurrent.ConcurrentHashMap

/**
 * Shared context for storing and retrieving data across test steps.
 * Thread-safe implementation for parallel test execution.
 */
class ScenarioContext {
    @PublishedApi
    internal val data = ConcurrentHashMap<String, Any>()

    /**
     * Retrieve a value from the context
     */
    operator fun get(key: String): Any? = data[key]

    /**
     * Store a value in the context
     */
    fun save(key: String, value: Any) {
        data[key] = value
    }

    /**
     * Retrieve a typed value from the context
     */
    inline fun <reified T> getTyped(key: String): T? {
        return data[key] as? T
    }

    /**
     * Check if a key exists in the context
     */
    fun contains(key: String): Boolean = data.containsKey(key)

    /**
     * Remove a value from the context
     */
    fun remove(key: String): Any? = data.remove(key)

    /**
     * Clear all data from the context
     */
    fun clear() {
        data.clear()
    }

    /**
     * Get all keys in the context
     */
    fun keys(): Set<String> = data.keys.toSet()

    /**
     * Get the size of the context
     */
    fun size(): Int = data.size
}
