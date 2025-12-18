# Auto-Generate Tests from OpenAPI

Automatically generate complete test suites from OpenAPI (Swagger) specifications with realistic test data powered by DataFaker.

## Overview

The `generate` command analyzes your OpenAPI specification and creates ready-to-run test cases with:

- ✅ **Smart Type-Aware Data Generation**: Automatically generates realistic data based on field types and constraints
- ✅ **Comprehensive Endpoint Coverage**: Creates tests for all endpoints (GET, POST, PUT, DELETE, PATCH)
- ✅ **Organized Test Structure**: Well-structured, maintainable test code
- ✅ **Realistic Mock Data**: Uses DataFaker for contextual fake data (names, emails, addresses, etc.)
- ✅ **Parameter Handling**: Supports path, query, header, and body parameters
- ✅ **Response Validations**: Auto-generates assertions based on response schemas
- ✅ **Authentication Support**: Handles various auth schemes (Bearer, Basic, API Key, OAuth2)
- ✅ **Schema Validation**: Validates responses against OpenAPI schemas
- ✅ **Edge Cases**: Generates tests for error scenarios (400, 401, 404, 500)
- ✅ **Data Relationships**: Understands and creates tests for related resources

## Basic Usage

```bash
api-test generate --spec openapi.yaml
```

This generates tests in `src/test/kotlin/generated` with package `generated.tests`.

## Command Options

```bash
api-test generate [OPTIONS]
```

### Required Options

| Option | Short | Description |
|--------|-------|-------------|
| `--spec` | `-s` | Path to OpenAPI spec file (YAML or JSON) |

### Optional Options

| Option | Short | Description | Default |
|--------|-------|-------------|---------|
| `--output` | `-o` | Output directory | `src/test/kotlin/generated` |
| `--package` | `-p` | Package name | `generated.tests` |
| `--base-url` | `-b` | API base URL | `http://localhost:8080` |
| `--tags` | `-t` | Filter endpoints by tags | All tags |
| `--include-errors` | | Generate negative test cases | `true` |
| `--data-driven` | | Generate data-driven tests | `false` |
| `--locale` | | Locale for fake data | `en-US` |
| `--auth-header` | | Auth header name | From spec |
| `--auth-value` | | Auth value template | From spec |

## Examples

### Basic Generation

```bash
# Generate from local file
api-test generate --spec openapi.yaml

# Generate from URL
api-test generate --spec https://api.example.com/openapi.json

### Custom Output

```bash
api-test generate \
  --spec openapi.yaml \
  --output src/test/kotlin/api \
  --package com.myapp.api.tests
```

### With Base URL

```bash
api-test generate \
  --spec openapi.yaml \
  --base-url https://api.example.com
```

## Generated Test Structure

For this OpenAPI spec:

```yaml
openapi: 3.0.0
info:
  title: User API
  version: 1.0.0
paths:
  /users:
    get:
      summary: List all users
      tags: [users]
      parameters:
        - name: page
          in: query
          schema:
            type: integer
      responses:
        '200':
          description: Success
    post:
      summary: Create user
      tags: [users]
      requestBody:
        content:
          application/json:
            schema:
              type: object
              required: [email, name]
              properties:
                email:
                  type: string
                  format: email
                name:
                  type: string
                age:
                  type: integer
                  minimum: 18
      responses:
        '201':
          description: Created
```

The generator creates:

```kotlin
package generated.tests

import dev.codersbox.eng.lib.api.testing.dsl.*
import io.kotest.core.spec.style.FunSpec

class UsersTest : FunSpec({
    val suite = apiTestSuite("Users API Tests") {
        baseUrl("http://localhost:8080")
        
        defaultHeaders {
            "Content-Type" to "application/json"
            "Accept" to "application/json"
        }

        scenario("List all users") {
            step("GET /users") {
                get("/users") {
                    queryParams("page" to "1")
                    expect {
                        status(200)
                        jsonPath("$") exists()
                    }
                }
            }
        }

        scenario("Create user") {
            step("POST /users") {
                post("/users") {
                    body("""
                    {
                      "email": "john.doe@example.com",
                      "name": "John Doe",
                      "age": 25
                    }
                    """)
                    expect {
                        status(201)
                        jsonPath("$") exists()
                    }
                }
            }
        }
    }
})
```

## Smart Data Generation

### String Formats

The generator recognizes OpenAPI formats and generates appropriate data:

| Format | Example Generated Value |
|--------|------------------------|
| `email` | `john.smith@example.com` |
| `uuid` | `123e4567-e89b-12d3-a456-426614174000` |
| `date` | `2024-01-15` |
| `date-time` | `2024-01-15T10:30:00Z` |
| `uri` / `url` | `https://example.com` |
| `password` | `Secure#Pass123` |
| `ipv4` | `192.168.1.1` |
| `ipv6` | `2001:0db8:85a3::8a2e:0370:7334` |
| `hostname` | `api.example.com` |

### Number Constraints

```yaml
schema:
  type: integer
  minimum: 18
  maximum: 100
```

Generates: `42` (random value between 18-100)

### String Constraints

```yaml
schema:
  type: string
  minLength: 5
  maxLength: 20
```

Generates: Random string with length 5-20

### Enums

```yaml
schema:
  type: string
  enum: [pending, active, inactive]
```

Generates: One of `pending`, `active`, or `inactive`

### Arrays

```yaml
schema:
  type: array
  items:
    type: string
    format: email
```

Generates:
```json
["user1@example.com", "user2@example.com"]
```

### Objects with Required Fields

```yaml
schema:
  type: object
  required: [id, name]
  properties:
    id:
      type: integer
    name:
      type: string
    optional:
      type: string
```

Generates:
```json
{
  "id": 123,
  "name": "Sample Name",
  "optional": "Optional Value"  // May or may not be included
}
```

### Schema References

```yaml
components:
  schemas:
    User:
      type: object
      properties:
        email:
          type: string
          format: email

paths:
  /users:
    post:
      requestBody:
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/User'
```

The generator resolves references and generates appropriate data.

## Test Organization

Tests are grouped by:

1. **Tags**: If endpoints have tags, tests are grouped by tag
2. **Path Segments**: Otherwise, grouped by first path segment

Example:
- `/users/*` → `UsersTest.kt`
- `/orders/*` → `OrdersTest.kt`
- Tagged with `auth` → `AuthTest.kt`

## Workflow

### 1. Generate Tests

```bash
api-test generate --spec openapi.yaml
```

### 2. Review Generated Code

```bash
vim src/test/kotlin/generated/UsersTest.kt
```

### 3. Customize as Needed

Add custom validations, modify data, add more scenarios.

### 4. Run Tests

```bash
mvn test
```

### 5. Regenerate on Spec Changes

```bash
# When OpenAPI spec is updated
api-test generate --spec openapi.yaml --output src/test/kotlin/generated
```

## Best Practices

### 1. Use Version Control

Commit generated tests to track API evolution:

```bash
git add src/test/kotlin/generated/
git commit -m "Generate tests from OpenAPI v2.1.0"
```

### 2. Separate Generated and Manual Tests

```
src/test/kotlin/
├── generated/          # Auto-generated tests
│   ├── UsersTest.kt
│   └── OrdersTest.kt
└── custom/             # Manual tests
    ├── UserEdgeCasesTest.kt
    └── OrderIntegrationTest.kt
```

### 3. Enhance Generated Tests

Use generated tests as a foundation:

```kotlin
// Generated code
scenario("Create user") {
    step("POST /users") {
        post("/users") {
            body(fakeUser())
            expect {
                status(201)
            }
        }
    }
}

// Add custom validations
scenario("Create user") {
    step("POST /users") {
        post("/users") {
            body(fakeUser())
            expect {
                status(201)
                jsonPath("$.id") exists()
                jsonPath("$.email") isValidEmail()
                jsonPath("$.createdAt") matches("\\d{4}-\\d{2}-\\d{2}T.*")
            }
        }
    }
}
```

### 4. CI/CD Integration

```yaml
# .github/workflows/test.yml
name: API Tests
on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      
      - name: Generate tests
        run: |
          java -jar api-test.jar generate \
            --spec openapi.yaml \
            --output src/test/kotlin/generated
      
      - name: Run tests
        run: mvn test
```

## Troubleshooting

### Issue: Generated tests don't compile

**Solution**: Ensure core framework dependencies are in your `pom.xml`:

```xml
<dependency>
    <groupId>dev.codersbox.eng.lib</groupId>
    <artifactId>core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

### Issue: Circular reference in schemas

The generator detects and prevents infinite loops, returning `null` for circular refs.

### Issue: Data doesn't match expected format

Override generated data in your custom tests or use `.example` in OpenAPI spec.

## Next Steps

- [Running Tests](./running-tests.md) - Execute generated tests
- [Reporting](./reporting.md) - Generate test reports
- [Custom Validators](../advanced/custom-validators.md) - Add custom validations

# Custom output directory and package
api-test generate \
  --spec openapi.yaml \
  --output src/test/kotlin/api \
  --package com.example.api.tests

# With base URL
api-test generate \
  --spec openapi.yaml \
  --base-url https://staging.api.example.com
```

### Filter by Tags

```bash
# Generate tests only for user-related endpoints
api-test generate --spec openapi.yaml --tags user

# Multiple tags
api-test generate --spec openapi.yaml --tags user,admin,product
```

### Generate with Authentication

```bash
# Bearer token
api-test generate \
  --spec openapi.yaml \
  --auth-header "Authorization" \
  --auth-value "Bearer \${API_TOKEN}"

# API Key
api-test generate \
  --spec openapi.yaml \
  --auth-header "X-API-Key" \
  --auth-value "\${API_KEY}"
```

### Data-Driven Tests

```bash
# Generate parameterized tests
api-test generate --spec openapi.yaml --data-driven
```

### Custom Locale for Fake Data

```bash
# Generate tests with Japanese locale
api-test generate --spec openapi.yaml --locale ja-JP

# Generate with German locale
api-test generate --spec openapi.yaml --locale de-DE
```

## Generated Test Structure

### Directory Layout

```
src/test/kotlin/generated/
├── UserApiTest.kt          # User endpoint tests
├── ProductApiTest.kt       # Product endpoint tests
├── OrderApiTest.kt         # Order endpoint tests
├── models/
│   ├── User.kt             # Data models
│   ├── Product.kt
│   └── Order.kt
└── fixtures/
    ├── UserFixtures.kt     # Test data fixtures
    ├── ProductFixtures.kt
    └── OrderFixtures.kt
```

### Sample Generated Test

Given this OpenAPI spec:

```yaml
paths:
  /users:
    post:
      tags:
        - user
      summary: Create a new user
      requestBody:
        required: true
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/CreateUserRequest'
      responses:
        '201':
          description: User created
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/User'
        '400':
          description: Invalid request
        '409':
          description: User already exists

components:
  schemas:
    CreateUserRequest:
      type: object
      required:
        - username
        - email
        - firstName
        - lastName
      properties:
        username:
          type: string
          minLength: 3
          maxLength: 20
        email:
          type: string
          format: email
        firstName:
          type: string
        lastName:
          type: string
        age:
          type: integer
          minimum: 18
          maximum: 100
    
    User:
      allOf:
        - $ref: '#/components/schemas/CreateUserRequest'
        - type: object
          properties:
            id:
              type: integer
            createdAt:
              type: string
              format: date-time
```

The generated test will look like:

```kotlin
package generated.tests

import dev.codersbox.eng.lib.api.testing.dsl.apiTestSuite
import dev.codersbox.eng.lib.api.testing.dsl.scenario
import dev.codersbox.eng.lib.api.testing.dsl.step
import generated.tests.fixtures.UserFixtures
import io.kotest.core.spec.style.FunSpec

class UserApiTest : FunSpec({
    
    apiTestSuite("User API Tests") {
        baseUrl = "http://localhost:8080"
        
        scenario("Create User - Success") {
            step("POST /users with valid data") {
                val request = UserFixtures.createUserRequest()
                
                post("/users") {
                    headers {
                        "Content-Type" to "application/json"
                    }
                    body = request
                }.expect {
                    status(201)
                    jsonPath("$.id") exists()
                    jsonPath("$.username") equals request.username
                    jsonPath("$.email") equals request.email
                    jsonPath("$.firstName") equals request.firstName
                    jsonPath("$.lastName") equals request.lastName
                    jsonPath("$.age") equals request.age
                    jsonPath("$.createdAt") matchesPattern "\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}"
                    
                    // Schema validation
                    matchesSchema("User")
                }
            }
        }
        
        scenario("Create User - Invalid Email") {
            step("POST /users with invalid email") {
                val request = UserFixtures.createUserRequest().copy(
                    email = "invalid-email"
                )
                
                post("/users") {
                    headers {
                        "Content-Type" to "application/json"
                    }
                    body = request
                }.expect {
                    status(400)
                    jsonPath("$.error") exists()
                }
            }
        }
        
        scenario("Create User - Duplicate Username") {
            step("Create initial user") {
                val request = UserFixtures.createUserRequest()
                
                post("/users") {
                    body = request
                }.expect {
                    status(201)
                }
            }
            
            step("Attempt to create duplicate user") {
                val request = UserFixtures.createUserRequest(username = context["username"])
                
                post("/users") {
                    body = request
                }.expect {
                    status(409)
                    jsonPath("$.error") contains "already exists"
                }
            }
        }
        
        scenario("Create User - Missing Required Fields") {
            step("POST /users without username") {
                post("/users") {
                    body = mapOf(
                        "email" to "test@example.com",
                        "firstName" to "John",
                        "lastName" to "Doe"
                    )
                }.expect {
                    status(400)
                }
            }
        }
    }
})
```

### Generated Fixtures

```kotlin
package generated.tests.fixtures

import net.datafaker.Faker

object UserFixtures {
    private val faker = Faker()
    
    fun createUserRequest(
        username: String = faker.internet().username(),
        email: String = faker.internet().emailAddress(),
        firstName: String = faker.name().firstName(),
        lastName: String = faker.name().lastName(),
        age: Int = faker.number().numberBetween(18, 100)
    ) = CreateUserRequest(
        username = username,
        email = email,
        firstName = firstName,
        lastName = lastName,
        age = age
    )
    
    fun invalidUserRequest() = createUserRequest(
        email = "invalid-email",
        age = 15
    )
}
```

## Smart Data Generation

The generator intelligently creates fake data based on:

### Field Names

| Field Name Pattern | Generated Data |
|-------------------|----------------|
| `*name`, `*Name` | Person name |
| `*email*`, `*Email*` | Valid email |
| `*phone*`, `*Phone*` | Phone number |
| `*address*`, `*Address*` | Street address |
| `*city*` | City name |
| `*country*` | Country name |
| `*zip*`, `*postal*` | Postal code |
| `*url*`, `*Url*`, `*URL*` | Valid URL |
| `*price*`, `*amount*` | Decimal number |
| `*date*`, `*Date*` | ISO date |
| `*time*`, `*Time*` | ISO timestamp |
| `*description*` | Lorem ipsum text |
| `*company*` | Company name |
| `*username*` | Username |
| `*password*` | Secure password |
| `*uuid*`, `*id*` | UUID |

### Field Types

| OpenAPI Type | Format | Generated Data |
|--------------|--------|----------------|
| `string` | - | Random text |
| `string` | `email` | Valid email |
| `string` | `uri` | Valid URL |
| `string` | `date` | ISO date (YYYY-MM-DD) |
| `string` | `date-time` | ISO timestamp |
| `string` | `uuid` | Valid UUID |
| `string` | `password` | Secure password |
| `integer` | - | Random integer |
| `integer` | `int32` | 32-bit integer |
| `integer` | `int64` | 64-bit integer |
| `number` | - | Random decimal |
| `number` | `float` | Float |
| `number` | `double` | Double |
| `boolean` | - | true/false |
| `array` | - | Array with 2-5 items |
| `object` | - | Nested object |

### Constraints

Respects OpenAPI constraints:

- `minLength`, `maxLength` - String length
- `minimum`, `maximum` - Numeric bounds
- `pattern` - Regex patterns
- `minItems`, `maxItems` - Array size
- `enum` - Picks from enum values
- `required` - Ensures required fields

## Configuration File

Create `api-test-generate.yaml` for reusable configuration:

```yaml
spec: openapi.yaml
output: src/test/kotlin/api
package: com.example.api.tests
baseUrl: https://api.example.com

# Filter endpoints
tags:
  - user
  - product

# Authentication
auth:
  type: bearer
  header: Authorization
  value: "Bearer ${API_TOKEN}"

# Data generation
dataGeneration:
  locale: en-US
  includeEdgeCases: true
  dataDriven: true
  
# Test generation options
testOptions:
  includeErrorCases: true
  includeValidation: true
  includeSchemaValidation: true
  generateFixtures: true
  
# Naming conventions
naming:
  testClassSuffix: ApiTest
  fixtureClassSuffix: Fixtures
```

Use with:

```bash
api-test generate --config api-test-generate.yaml
```

## Advanced Features

### Custom Templates

Override default templates:

```bash
api-test generate \
  --spec openapi.yaml \
  --template custom-test-template.kt
```

### Incremental Generation

Update tests when spec changes:

```bash
# Only regenerate changed endpoints
api-test generate --spec openapi.yaml --incremental
```

### Dry Run

Preview what will be generated:

```bash
api-test generate --spec openapi.yaml --dry-run
```

### Generate Specific Operations

```bash
# Generate tests only for POST operations
api-test generate --spec openapi.yaml --methods POST

# Generate for multiple methods
api-test generate --spec openapi.yaml --methods GET,POST,PUT
```

## Integration with Existing Tests

### Extend Generated Tests

Generated tests are normal Kotest tests - extend them:

```kotlin
class ExtendedUserApiTest : UserApiTest() {
    init {
        apiTestSuite("Extended User Tests") {
            scenario("Custom test case") {
                // Your custom test logic
            }
        }
    }
}
```

### Use Generated Fixtures

```kotlin
import generated.tests.fixtures.UserFixtures

class MyCustomTest : FunSpec({
    test("custom user test") {
        val user = UserFixtures.createUserRequest()
        // Use in your test
    }
})
```

## CI/CD Integration

### GitHub Actions

```yaml
name: Generate and Run Tests

on:
  push:
    paths:
      - 'openapi.yaml'

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Generate Tests
        run: |
          api-test generate \
            --spec openapi.yaml \
            --base-url https://staging.api.example.com
      
      - name: Run Generated Tests
        run: |
          api-test run \
            --file src/test/kotlin/generated \
            --report-format junit
      
      - name: Commit Generated Tests
        run: |
          git config user.name "GitHub Actions"
          git config user.email "actions@github.com"
          git add src/test/kotlin/generated
          git commit -m "Update generated tests"
          git push
```

### Pre-commit Hook

```bash
#!/bin/bash
# .git/hooks/pre-commit

if git diff --cached --name-only | grep -q "openapi.yaml"; then
    echo "OpenAPI spec changed, regenerating tests..."
    api-test generate --spec openapi.yaml --incremental
    git add src/test/kotlin/generated
fi
```

## Best Practices

1. **Version Control**: Commit generated tests to track changes
2. **Review Generated Code**: Always review before committing
3. **Customize Fixtures**: Extend generated fixtures for complex scenarios
4. **Keep Spec Updated**: Regenerate when OpenAPI spec changes
5. **Tag Endpoints**: Use OpenAPI tags for better organization
6. **Document Custom Tests**: Add comments to extended tests
7. **Use Data-Driven Mode**: For comprehensive coverage
8. **Validate Spec First**: Use `api-test validate` before generating

## Troubleshooting

### Invalid OpenAPI Spec

```bash
# Validate spec first
api-test validate --spec openapi.yaml

# Common issues:
# - Missing required fields
# - Invalid references
# - Malformed YAML/JSON
```

### Generation Fails

```bash
# Enable verbose output
api-test generate --spec openapi.yaml --verbose

# Check spec version
# Only OpenAPI 3.0+ supported
```

### Missing Data Models

```bash
# Ensure all schemas are defined in components/schemas
# Use $ref for reusable schemas
```

### Incorrect Test Data

```bash
# Use custom locale
api-test generate --spec openapi.yaml --locale ja-JP

# Or customize fixtures after generation
```

## Examples

See complete examples:

- [Basic API Generation](../examples/generation/basic-api.md)
- [Complex Schema Generation](../examples/generation/complex-schema.md)
- [Microservices Suite](../examples/generation/microservices.md)
- [E-commerce API](../examples/generation/ecommerce.md)

## Next Steps

- [Run Generated Tests](./running-tests.md)
- [View Test Reports](./reporting.md)
- [Customize Generated Tests](../advanced/custom-plugins.md)
