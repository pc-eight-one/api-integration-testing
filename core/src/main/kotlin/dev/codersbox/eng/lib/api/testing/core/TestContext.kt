package dev.codersbox.eng.lib.api.testing.core

/**
 * Test execution context for sharing state between steps
 */
interface TestContext {
    /**
     * Get a value from context
     */
    fun <T> get(key: String): T?
    
    /**
     * Set a value in context
     */
    fun set(key: String, value: Any)
    
    /**
     * Check if context contains a key
     */
    fun contains(key: String): Boolean
    
    /**
     * Clear all context data
     */
    fun clear()
}
