package dev.codersbox.eng.lib.api.testing.validation

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

// Equality matchers
fun <T> equals(expected: T): Matcher<T> = object : Matcher<T> {
    override fun matches(actual: T): MatchResult {
        return if (actual == expected) {
            MatchResult.success("$actual equals $expected")
        } else {
            MatchResult.failure("Expected $expected but got $actual", expected, actual)
        }
    }
}

fun <T> notEquals(expected: T): Matcher<T> = object : Matcher<T> {
    override fun matches(actual: T): MatchResult {
        return if (actual != expected) {
            MatchResult.success("$actual does not equal $expected")
        } else {
            MatchResult.failure("Expected value different from $expected", null, actual)
        }
    }
}

// Comparison matchers
fun <T : Comparable<T>> greaterThan(threshold: T): Matcher<T> = object : Matcher<T> {
    override fun matches(actual: T): MatchResult {
        return if (actual > threshold) {
            MatchResult.success("$actual > $threshold")
        } else {
            MatchResult.failure("Expected value > $threshold but got $actual", threshold, actual)
        }
    }
}

fun <T : Comparable<T>> lessThan(threshold: T): Matcher<T> = object : Matcher<T> {
    override fun matches(actual: T): MatchResult {
        return if (actual < threshold) {
            MatchResult.success("$actual < $threshold")
        } else {
            MatchResult.failure("Expected value < $threshold but got $actual", threshold, actual)
        }
    }
}

fun <T : Comparable<T>> greaterThanOrEqual(threshold: T): Matcher<T> = object : Matcher<T> {
    override fun matches(actual: T): MatchResult {
        return if (actual >= threshold) {
            MatchResult.success("$actual >= $threshold")
        } else {
            MatchResult.failure("Expected value >= $threshold but got $actual", threshold, actual)
        }
    }
}

fun <T : Comparable<T>> lessThanOrEqual(threshold: T): Matcher<T> = object : Matcher<T> {
    override fun matches(actual: T): MatchResult {
        return if (actual <= threshold) {
            MatchResult.success("$actual <= $threshold")
        } else {
            MatchResult.failure("Expected value <= $threshold but got $actual", threshold, actual)
        }
    }
}

fun <T : Comparable<T>> between(min: T, max: T): Matcher<T> = object : Matcher<T> {
    override fun matches(actual: T): MatchResult {
        return if (actual in min..max) {
            MatchResult.success("$actual is between $min and $max")
        } else {
            MatchResult.failure("Expected value between $min and $max but got $actual", "$min..$max", actual)
        }
    }
}

// String matchers
fun contains(substring: String): Matcher<String> = object : Matcher<String> {
    override fun matches(actual: String): MatchResult {
        return if (actual.contains(substring)) {
            MatchResult.success("'$actual' contains '$substring'")
        } else {
            MatchResult.failure("Expected string to contain '$substring' but got '$actual'", substring, actual)
        }
    }
}

fun startsWith(prefix: String): Matcher<String> = object : Matcher<String> {
    override fun matches(actual: String): MatchResult {
        return if (actual.startsWith(prefix)) {
            MatchResult.success("'$actual' starts with '$prefix'")
        } else {
            MatchResult.failure("Expected string to start with '$prefix' but got '$actual'", prefix, actual)
        }
    }
}

fun endsWith(suffix: String): Matcher<String> = object : Matcher<String> {
    override fun matches(actual: String): MatchResult {
        return if (actual.endsWith(suffix)) {
            MatchResult.success("'$actual' ends with '$suffix'")
        } else {
            MatchResult.failure("Expected string to end with '$suffix' but got '$actual'", suffix, actual)
        }
    }
}

fun matchesRegex(pattern: String): Matcher<String> = object : Matcher<String> {
    override fun matches(actual: String): MatchResult {
        val regex = Regex(pattern)
        return if (regex.matches(actual)) {
            MatchResult.success("'$actual' matches pattern '$pattern'")
        } else {
            MatchResult.failure("Expected string to match pattern '$pattern' but got '$actual'", pattern, actual)
        }
    }
}

fun isBlank(): Matcher<String> = object : Matcher<String> {
    override fun matches(actual: String): MatchResult {
        return if (actual.isBlank()) {
            MatchResult.success("String is blank")
        } else {
            MatchResult.failure("Expected blank string but got '$actual'", "", actual)
        }
    }
}

fun isNotBlank(): Matcher<String> = object : Matcher<String> {
    override fun matches(actual: String): MatchResult {
        return if (actual.isNotBlank()) {
            MatchResult.success("String is not blank")
        } else {
            MatchResult.failure("Expected non-blank string but got blank string", null, actual)
        }
    }
}

// Validation matchers
fun isValidEmail(): Matcher<String> = object : Matcher<String> {
    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    
    override fun matches(actual: String): MatchResult {
        return if (emailRegex.matches(actual)) {
            MatchResult.success("'$actual' is a valid email")
        } else {
            MatchResult.failure("Expected valid email but got '$actual'", "valid email format", actual)
        }
    }
}

fun isValidUrl(): Matcher<String> = object : Matcher<String> {
    private val urlRegex = Regex("^(https?|ftp)://[^\\s/$.?#].[^\\s]*$", RegexOption.IGNORE_CASE)
    
    override fun matches(actual: String): MatchResult {
        return if (urlRegex.matches(actual)) {
            MatchResult.success("'$actual' is a valid URL")
        } else {
            MatchResult.failure("Expected valid URL but got '$actual'", "valid URL format", actual)
        }
    }
}

fun isValidUUID(): Matcher<String> = object : Matcher<String> {
    override fun matches(actual: String): MatchResult {
        return try {
            UUID.fromString(actual)
            MatchResult.success("'$actual' is a valid UUID")
        } catch (e: IllegalArgumentException) {
            MatchResult.failure("Expected valid UUID but got '$actual'", "valid UUID format", actual)
        }
    }
}

fun isValidDate(pattern: String = "yyyy-MM-dd"): Matcher<String> = object : Matcher<String> {
    private val formatter = DateTimeFormatter.ofPattern(pattern)
    
    override fun matches(actual: String): MatchResult {
        return try {
            LocalDate.parse(actual, formatter)
            MatchResult.success("'$actual' is a valid date with pattern '$pattern'")
        } catch (e: Exception) {
            MatchResult.failure("Expected valid date (pattern: $pattern) but got '$actual'", pattern, actual)
        }
    }
}

fun isValidDateTime(pattern: String = "yyyy-MM-dd'T'HH:mm:ss"): Matcher<String> = object : Matcher<String> {
    private val formatter = DateTimeFormatter.ofPattern(pattern)
    
    override fun matches(actual: String): MatchResult {
        return try {
            LocalDateTime.parse(actual, formatter)
            MatchResult.success("'$actual' is a valid datetime with pattern '$pattern'")
        } catch (e: Exception) {
            MatchResult.failure("Expected valid datetime (pattern: $pattern) but got '$actual'", pattern, actual)
        }
    }
}

// Collection matchers
fun <T> hasSize(expectedSize: Int): Matcher<Collection<T>> = object : Matcher<Collection<T>> {
    override fun matches(actual: Collection<T>): MatchResult {
        return if (actual.size == expectedSize) {
            MatchResult.success("Collection has size $expectedSize")
        } else {
            MatchResult.failure("Expected collection size $expectedSize but got ${actual.size}", expectedSize, actual.size)
        }
    }
}

fun <T> isEmpty(): Matcher<Collection<T>> = object : Matcher<Collection<T>> {
    override fun matches(actual: Collection<T>): MatchResult {
        return if (actual.isEmpty()) {
            MatchResult.success("Collection is empty")
        } else {
            MatchResult.failure("Expected empty collection but got ${actual.size} elements", 0, actual.size)
        }
    }
}

fun <T> isNotEmpty(): Matcher<Collection<T>> = object : Matcher<Collection<T>> {
    override fun matches(actual: Collection<T>): MatchResult {
        return if (actual.isNotEmpty()) {
            MatchResult.success("Collection is not empty")
        } else {
            MatchResult.failure("Expected non-empty collection but got empty collection", "> 0", 0)
        }
    }
}

fun <T> containsElement(element: T): Matcher<Collection<T>> = object : Matcher<Collection<T>> {
    override fun matches(actual: Collection<T>): MatchResult {
        return if (actual.contains(element)) {
            MatchResult.success("Collection contains $element")
        } else {
            MatchResult.failure("Expected collection to contain $element", element, actual)
        }
    }
}

fun <T> containsAll(vararg elements: T): Matcher<Collection<T>> = object : Matcher<Collection<T>> {
    override fun matches(actual: Collection<T>): MatchResult {
        val missing = elements.filter { it !in actual }
        return if (missing.isEmpty()) {
            MatchResult.success("Collection contains all expected elements")
        } else {
            MatchResult.failure("Collection missing elements: $missing", elements.toList(), actual)
        }
    }
}

// Null matchers
fun <T> isNull(): Matcher<T?> = object : Matcher<T?> {
    override fun matches(actual: T?): MatchResult {
        return if (actual == null) {
            MatchResult.success("Value is null")
        } else {
            MatchResult.failure("Expected null but got $actual", null, actual)
        }
    }
}

fun <T> isNotNull(): Matcher<T?> = object : Matcher<T?> {
    override fun matches(actual: T?): MatchResult {
        return if (actual != null) {
            MatchResult.success("Value is not null")
        } else {
            MatchResult.failure("Expected non-null value but got null", "not null", null)
        }
    }
}

// Boolean matchers
fun isTrue(): Matcher<Boolean> = object : Matcher<Boolean> {
    override fun matches(actual: Boolean): MatchResult {
        return if (actual) {
            MatchResult.success("Value is true")
        } else {
            MatchResult.failure("Expected true but got false", true, false)
        }
    }
}

fun isFalse(): Matcher<Boolean> = object : Matcher<Boolean> {
    override fun matches(actual: Boolean): MatchResult {
        return if (!actual) {
            MatchResult.success("Value is false")
        } else {
            MatchResult.failure("Expected false but got true", false, true)
        }
    }
}
