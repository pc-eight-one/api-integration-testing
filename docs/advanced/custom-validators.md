# Custom Validators

## Create Custom Validator

```kotlin
class EmailValidator : Validator {
    override fun validate(value: Any): ValidationResult {
        val email = value.toString()
        return if (email.matches(Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"))) {
            ValidationResult.success()
        } else {
            ValidationResult.failure("Invalid email format: $email")
        }
    }
}
```

## Use Custom Validator

```kotlin
scenario("Validate email") {
    step("Check user email") {
        get("/api/users/1").expect {
            jsonPath("$.email").validate(EmailValidator())
        }
    }
}
```

See [Assertions & Validation](../guide/assertions.md) for more examples.
