# Custom Matchers

## Create Custom Matcher

```kotlin
fun beValidEmail() = object : Matcher<String> {
    override fun test(value: String) = MatcherResult(
        value.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")),
        { "$value should be a valid email" },
        { "$value should not be a valid email" }
    )
}
```

## Use Custom Matcher

```kotlin
scenario("Custom matcher") {
    step("Validate email") {
        get("/api/users/1").expect {
            jsonPath("$.email") should beValidEmail()
        }
    }
}
```

## More Matchers

```kotlin
fun beValidUrl() = object : Matcher<String> {
    override fun test(value: String) = try {
        URL(value)
        MatcherResult(true, { "$value is a valid URL" }, { "" })
    } catch (e: Exception) {
        MatcherResult(false, { "$value is not a valid URL" }, { "" })
    }
}

fun beInRange(min: Int, max: Int) = object : Matcher<Int> {
    override fun test(value: Int) = MatcherResult(
        value in min..max,
        { "$value should be in range $min..$max" },
        { "$value should not be in range $min..$max" }
    )
}
```
