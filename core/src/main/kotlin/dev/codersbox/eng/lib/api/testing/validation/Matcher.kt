package dev.codersbox.eng.lib.api.testing.validation

interface Matcher<T> {
    fun matches(actual: T): MatchResult
    
    infix fun and(other: Matcher<T>): Matcher<T> = object : Matcher<T> {
        override fun matches(actual: T): MatchResult {
            val first = this@Matcher.matches(actual)
            if (!first.passed) return first
            return other.matches(actual)
        }
    }
    
    infix fun or(other: Matcher<T>): Matcher<T> = object : Matcher<T> {
        override fun matches(actual: T): MatchResult {
            val first = this@Matcher.matches(actual)
            if (first.passed) return first
            return other.matches(actual)
        }
    }
}

data class MatchResult(
    val passed: Boolean,
    val message: String,
    val expected: Any? = null,
    val actual: Any? = null
) {
    companion object {
        fun success(message: String = "Match successful"): MatchResult = 
            MatchResult(true, message)
            
        fun failure(message: String, expected: Any? = null, actual: Any? = null): MatchResult = 
            MatchResult(false, message, expected, actual)
    }
}

class MatcherException(val result: MatchResult) : AssertionError(result.message)
