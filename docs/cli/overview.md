# CLI Overview

The API Integration Testing Framework includes a powerful command-line interface (CLI) that provides developer-friendly tools for running tests, generating reports, automating test workflows, and auto-generating test cases from OpenAPI specifications.

## Features

- ✅ **Run Tests**: Execute test scenarios with flexible filtering
- ✅ **Auto-Generate Tests**: Create tests from OpenAPI specs with realistic data
- ✅ **Watch Mode**: Auto-run tests on file changes
- ✅ **Interactive Dashboard**: Web-based test monitoring
- ✅ **Multiple Report Formats**: HTML, JSON, JUnit, Allure, Markdown
- ✅ **CI/CD Integration**: Built-in support for Jenkins, GitLab, GitHub Actions
- ✅ **Result Comparison**: Diff test results across runs
- ✅ **Parallel Execution**: Speed up test execution
- ✅ **Environment Management**: Easy environment switching

## Installation

### Build from Source

```bash
cd cli
mvn clean package
```

This creates an executable JAR: `target/api-test.jar`

### Create Alias (Recommended)

```bash
# Add to ~/.bashrc or ~/.zshrc
alias api-test='java -jar /path/to/api-test.jar'

# Or add to PATH
sudo cp target/api-test.jar /usr/local/bin/
echo '#!/bin/bash' | sudo tee /usr/local/bin/api-test
echo 'java -jar /usr/local/bin/api-test.jar "$@"' | sudo tee -a /usr/local/bin/api-test
sudo chmod +x /usr/local/bin/api-test
```

### Verify Installation

```bash
api-test --version
# Output: API Test CLI v1.0.0-SNAPSHOT
```

## Available Commands

| Command | Description | Example |
|---------|-------------|---------|
| `run` | Execute API test scenarios | `api-test run --tag smoke` |
| `generate` | Auto-generate tests from OpenAPI specs | `api-test generate --spec openapi.yaml` |
| `watch` | Monitor and auto-run tests on changes | `api-test watch` |
| `serve` | Start interactive test dashboard | `api-test serve --port 8080` |
| `report` | Generate test reports in various formats | `api-test report --format html` |
| `ci` | CI/CD integration utilities | `api-test ci --jenkins` |
| `diff` | Compare test results across runs | `api-test diff --baseline old.json --current new.json` |
| `init` | Initialize new test project | `api-test init --template rest-api` |
| `validate` | Validate OpenAPI spec | `api-test validate --spec openapi.yaml` |

## Quick Start

### Run Tests

```bash
# Run all tests
api-test run

# Run specific test file
api-test run --file src/test/kotlin/UserApiTest.kt

# Run tests with tag
api-test run --tag smoke

# Run with custom config
api-test run --config test-config.yaml

# Run in parallel
api-test run --parallel 4
```

### Generate Tests from OpenAPI

```bash
# Generate tests from OpenAPI specification
api-test generate --spec openapi.yaml --output src/test/kotlin/generated
```

### Watch Mode

```bash
# Auto-run tests when files change
api-test watch --path src/test/kotlin
```

### Generate Reports

```bash
# Generate HTML report
api-test report --format html --output reports/

# Generate JSON report
api-test report --format json --output results.json
```

### Interactive Dashboard

```bash
# Start web-based dashboard
api-test serve --port 8080
```

## Global Options

All commands support these global options:

```bash
--verbose, -v     Enable verbose logging
--quiet, -q       Suppress output except errors
--config, -c      Path to configuration file
--help, -h        Show help message
```

## Configuration File

Create `api-test.yaml` in your project root:

```yaml
baseUrl: http://localhost:8080
timeout: 30000

headers:
  Authorization: Bearer ${API_TOKEN}
  Content-Type: application/json

reporting:
  format: html
  outputDir: reports/

execution:
  parallel: true
  threads: 4
  failFast: false
```

## Environment Variables

```bash
# Override base URL
export API_BASE_URL=https://staging.api.example.com

# Set API token
export API_TOKEN=your-token-here

# Enable debug mode
export API_TEST_DEBUG=true
```

## Examples

### CI/CD Integration

```bash
# Run tests and fail on errors
api-test run --fail-fast --report junit --output test-results.xml

# Compare with baseline
api-test diff --baseline main --current feature-branch
```

### Development Workflow

```bash
# Watch and run tests during development
api-test watch --tag unit --auto-clear

# Start dashboard for interactive testing
api-test serve --hot-reload
```

### Test Generation

```bash
# Generate comprehensive test suite
api-test generate \
  --spec openapi.yaml \
  --package com.myapp.api.tests \
  --base-url https://api.myapp.com \
  --output src/test/kotlin/generated
```

## Next Steps

- [Test Generation Guide](./test-generation.md) - Auto-generate tests from OpenAPI
- [Running Tests Guide](./running-tests.md) - Advanced test execution options
- [Reporting Guide](./reporting.md) - Generate and customize test reports
