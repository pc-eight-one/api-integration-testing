# Running Tests with CLI

The `run` command executes your API test scenarios with flexible filtering, environment configuration, and real-time feedback.

## Basic Usage

```bash
# Run all tests in current directory
api-test run

# Run specific test file
api-test run --file src/test/kotlin/UserApiTest.kt

# Run multiple files
api-test run --file Test1.kt --file Test2.kt
```

## Command Options

### Test Selection

| Option | Short | Description | Example |
|--------|-------|-------------|---------|
| `--file` | `-f` | Run specific test file(s) | `--file UserTest.kt` |
| `--tag` | `-t` | Filter tests by tag | `--tag smoke` |
| `--scenario` | `-s` | Run specific scenario(s) | `--scenario "User Login"` |
| `--exclude-tag` | | Exclude tests with tag | `--exclude-tag slow` |

### Environment Configuration

| Option | Short | Description | Example |
|--------|-------|-------------|---------|
| `--config` | `-c` | Configuration file | `--config prod.yaml` |
| `--env` | `-e` | Environment name | `--env staging` |
| `--base-url` | `-u` | Override base URL | `--base-url https://api.example.com` |
| `--var` | `-v` | Set variable | `--var API_KEY=secret123` |

### Execution Control

| Option | Short | Description | Default |
|--------|-------|-------------|---------|
| `--parallel` | `-p` | Number of parallel threads | `1` |
| `--timeout` | | Global timeout (seconds) | `300` |
| `--fail-fast` | | Stop on first failure | `false` |
| `--retry` | `-r` | Retry failed tests (max attempts) | `0` |

### Output & Reporting

| Option | Short | Description | Default |
|--------|-------|-------------|---------|
| `--report-format` | | Report format(s): html, json, xml, junit | `html` |
| `--report-dir` | | Report output directory | `test-reports` |
| `--verbose` | `-v` | Verbose output | `false` |
| `--quiet` | `-q` | Minimal output | `false` |
| `--no-color` | | Disable colored output | `false` |

## Examples

### Run Tests by Tags

```bash
# Run smoke tests only
api-test run --tag smoke

# Run regression tests, excluding slow ones
api-test run --tag regression --exclude-tag slow

# Run tests with multiple tags
api-test run --tag api --tag integration
```

### Environment-Specific Tests

```bash
# Run against staging environment
api-test run --env staging

# Run with custom config
api-test run --config configs/qa-config.yaml

# Override base URL
api-test run --base-url https://qa.api.example.com

# Set custom variables
api-test run --var API_KEY=xyz123 --var TIMEOUT=60
```

### Parallel Execution

```bash
# Run tests in parallel (4 threads)
api-test run --parallel 4

# Run with fail-fast mode
api-test run --parallel 4 --fail-fast

# Run with retries
api-test run --retry 3
```

### Generate Reports

```bash
# Generate HTML report
api-test run --report-format html

# Generate multiple report formats
api-test run --report-format html --report-format json --report-format junit

# Custom report directory
api-test run --report-dir build/test-results
```

### Advanced Examples

```bash
# Run specific scenarios from a file
api-test run --file UserApiTest.kt --scenario "Create User" --scenario "Delete User"

# Run with timeout and retries
api-test run --timeout 600 --retry 2 --parallel 3

# Full CI/CD setup
api-test run \
  --env production \
  --tag smoke \
  --parallel 4 \
  --fail-fast \
  --report-format junit \
  --report-format json \
  --report-dir test-results \
  --verbose
```

## Configuration Files

### YAML Configuration

Create `api-test-config.yaml`:

```yaml
baseUrl: https://api.example.com
environment: staging
timeout: 300
parallel: 4
failFast: false
retry: 2

variables:
  API_KEY: ${API_KEY}
  API_SECRET: ${API_SECRET}
  
headers:
  X-API-Version: "1.0"
  User-Agent: "API-Test-CLI/1.0"

tags:
  include:
    - smoke
    - regression
  exclude:
    - wip
    - manual

reporting:
  formats:
    - html
    - json
    - junit
  directory: test-reports
  includeStackTraces: true
```

Use with:

```bash
api-test run --config api-test-config.yaml
```

### JSON Configuration

Create `api-test-config.json`:

```json
{
  "baseUrl": "https://api.example.com",
  "environment": "staging",
  "timeout": 300,
  "parallel": 4,
  "variables": {
    "API_KEY": "${API_KEY}",
    "TIMEOUT": "30"
  },
  "reporting": {
    "formats": ["html", "json"],
    "directory": "test-reports"
  }
}
```

## Exit Codes

| Code | Description |
|------|-------------|
| `0` | All tests passed |
| `1` | One or more tests failed |
| `2` | Invalid command line arguments |
| `3` | Test execution error |
| `4` | Configuration error |

## Real-Time Output

### Default Output

```
Running API Integration Tests...

✓ UserApiTest > Create User [1.234s]
✓ UserApiTest > Get User by ID [0.892s]
✗ UserApiTest > Update User [2.145s]
  Expected status 200 but got 404

Tests: 3, Passed: 2, Failed: 1, Duration: 4.271s
```

### Verbose Output

```bash
api-test run --verbose
```

```
[INFO] Loading configuration from api-test-config.yaml
[INFO] Base URL: https://api.example.com
[INFO] Parallel threads: 4
[INFO] Running 15 scenarios from 3 test files

[TEST] UserApiTest > Create User
  [STEP] POST /users
    Request: {"name": "John Doe", "email": "john@example.com"}
    Response: 201 Created
    Duration: 1.234s
  ✓ PASSED

[TEST] UserApiTest > Get User by ID
  [STEP] GET /users/123
    Response: 200 OK
    Duration: 0.892s
  ✓ PASSED
  
[TEST] UserApiTest > Update User
  [STEP] PUT /users/123
    Response: 404 Not Found
    Duration: 2.145s
  ✗ FAILED: Expected status 200 but got 404

Summary:
  Total: 3
  Passed: 2 (66.7%)
  Failed: 1 (33.3%)
  Duration: 4.271s
```

### Quiet Output

```bash
api-test run --quiet
```

```
Tests: 3, Passed: 2, Failed: 1
```

## Watch Mode

Monitor file changes and auto-run tests:

```bash
# Watch current directory
api-test watch

# Watch with specific tags
api-test watch --tag smoke

# Watch with custom config
api-test watch --config test-config.yaml
```

## CI/CD Integration

### GitHub Actions

```yaml
name: API Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
      
      - name: Run API Tests
        run: |
          java -jar cli/target/api-test.jar run \
            --env ci \
            --parallel 4 \
            --fail-fast \
            --report-format junit \
            --report-dir test-results
      
      - name: Publish Test Results
        uses: EnricoMi/publish-unit-test-result-action@v2
        if: always()
        with:
          files: test-results/**/*.xml
```

### Jenkins

```groovy
pipeline {
    agent any
    
    stages {
        stage('API Tests') {
            steps {
                sh '''
                    java -jar cli/target/api-test.jar run \
                        --env ${ENVIRONMENT} \
                        --parallel 4 \
                        --report-format junit \
                        --report-dir test-results
                '''
            }
        }
    }
    
    post {
        always {
            junit 'test-results/**/*.xml'
        }
    }
}
```

### GitLab CI

```yaml
api-tests:
  stage: test
  script:
    - java -jar cli/target/api-test.jar run
        --env ${CI_ENVIRONMENT_NAME}
        --parallel 4
        --report-format junit
        --report-dir test-results
  artifacts:
    reports:
      junit: test-results/**/*.xml
```

## Performance Tips

1. **Use Parallel Execution**: Set `--parallel` based on CPU cores
2. **Tag Your Tests**: Use tags for efficient test selection
3. **Fail Fast in CI**: Use `--fail-fast` to save CI time
4. **Retry Flaky Tests**: Use `--retry 2` for network-dependent tests
5. **Optimize Timeouts**: Set appropriate `--timeout` values

## Troubleshooting

### Tests Not Found

```bash
# Ensure you're in the correct directory
pwd

# Check for test files
find . -name "*Test.kt"

# Run with verbose output
api-test run --verbose
```

### Connection Errors

```bash
# Verify base URL is accessible
curl -I https://api.example.com

# Use custom timeout
api-test run --timeout 60

# Check configuration
api-test run --verbose --config api-test-config.yaml
```

### Report Generation Failed

```bash
# Ensure report directory exists and is writable
mkdir -p test-reports
chmod 755 test-reports

# Use absolute path
api-test run --report-dir $(pwd)/test-reports
```
