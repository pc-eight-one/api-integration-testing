# Test Reporting

Generate comprehensive test reports in multiple formats with detailed insights, metrics, and visualizations.

## Report Formats

The CLI supports multiple report formats to integrate with various tools and workflows:

| Format | Description | Use Case |
|--------|-------------|----------|
| **HTML** | Interactive web-based reports | Human-readable, detailed analysis |
| **JSON** | Structured data format | Programmatic analysis, custom tools |
| **XML** | Standard XML format | Legacy systems integration |
| **JUnit** | JUnit XML format | CI/CD integration (Jenkins, GitLab) |
| **Allure** | Allure test report | Advanced reporting with Allure framework |
| **Markdown** | Markdown summary | Documentation, README files |

## Generate Reports

### During Test Execution

```bash
# Generate HTML report (default)
api-test run --report-format html

# Generate multiple formats
api-test run --report-format html --report-format json --report-format junit

# Custom output directory
api-test run --report-format html --report-dir build/reports
```

### From Existing Results

```bash
# Generate report from previous results
api-test report --input test-results/results.json --format html

# Generate multiple formats
api-test report --input test-results/results.json \
  --format html \
  --format junit \
  --format allure
```

## HTML Reports

Interactive, feature-rich HTML reports with:

- ✅ Test execution summary
- ✅ Pass/fail statistics with charts
- ✅ Detailed test logs
- ✅ Request/response inspection
- ✅ Performance metrics
- ✅ Failure analysis
- ✅ Searchable and filterable

### Generate HTML Report

```bash
api-test run --report-format html --report-dir test-reports
```

Open `test-reports/index.html` in your browser.

### Features

**Dashboard Overview:**
- Total tests, pass rate, duration
- Pie chart of test results
- Trend graph (if historical data available)
- Quick filters by status/tag

**Test Details:**
- Expandable test scenarios
- Step-by-step execution
- Request/response payload
- Assertions and validations
- Stack traces for failures

**Search & Filter:**
- Search by test name
- Filter by status (passed/failed/skipped)
- Filter by tag
- Sort by duration

## JSON Reports

Structured JSON format for programmatic analysis:

```bash
api-test run --report-format json --report-dir test-reports
```

### Output Structure

```json
{
  "summary": {
    "total": 25,
    "passed": 23,
    "failed": 2,
    "skipped": 0,
    "duration": 45.678,
    "startTime": "2024-12-18T10:30:00Z",
    "endTime": "2024-12-18T10:30:45Z"
  },
  "testSuites": [
    {
      "name": "UserApiTest",
      "file": "src/test/kotlin/UserApiTest.kt",
      "scenarios": [
        {
          "name": "Create User",
          "status": "PASSED",
          "duration": 1.234,
          "tags": ["smoke", "user-api"],
          "steps": [
            {
              "name": "Send POST request",
              "status": "PASSED",
              "duration": 0.892,
              "request": {
                "method": "POST",
                "url": "https://api.example.com/users",
                "headers": {
                  "Content-Type": "application/json"
                },
                "body": "{\"name\":\"John Doe\"}"
              },
              "response": {
                "status": 201,
                "headers": {
                  "Content-Type": "application/json"
                },
                "body": "{\"id\":123,\"name\":\"John Doe\"}",
                "duration": 0.892
              },
              "assertions": [
                {
                  "type": "STATUS",
                  "expected": 201,
                  "actual": 201,
                  "passed": true
                }
              ]
            }
          ]
        }
      ]
    }
  ],
  "environment": {
    "baseUrl": "https://api.example.com",
    "environment": "staging",
    "javaVersion": "17.0.1",
    "os": "Linux"
  }
}
```

### Use Cases

**Custom Dashboards:**
```bash
# Parse JSON and create custom reports
jq '.summary' test-reports/report.json

# Extract failed tests
jq '.testSuites[].scenarios[] | select(.status=="FAILED")' test-reports/report.json
```

**Metrics Collection:**
```python
import json

with open('test-reports/report.json') as f:
    report = json.load(f)
    
pass_rate = report['summary']['passed'] / report['summary']['total'] * 100
avg_duration = report['summary']['duration'] / report['summary']['total']

print(f"Pass Rate: {pass_rate}%")
print(f"Avg Duration: {avg_duration}s")
```

## JUnit XML Reports

Standard JUnit XML format for CI/CD integration:

```bash
api-test run --report-format junit --report-dir test-reports
```

### Output Structure

```xml
<?xml version="1.0" encoding="UTF-8"?>
<testsuites tests="25" failures="2" errors="0" skipped="0" time="45.678">
  <testsuite name="UserApiTest" tests="10" failures="1" errors="0" skipped="0" time="12.345">
    <testcase name="Create User" classname="UserApiTest" time="1.234">
      <!-- Passed test -->
    </testcase>
    <testcase name="Update User" classname="UserApiTest" time="2.456">
      <failure message="Expected status 200 but got 404" type="AssertionError">
        Expected status 200 but got 404
        at UserApiTest.kt:45
      </failure>
    </testcase>
  </testsuite>
</testsuites>
```

### CI/CD Integration

**Jenkins:**
```groovy
post {
    always {
        junit 'test-reports/**/*.xml'
    }
}
```

**GitLab CI:**
```yaml
artifacts:
  reports:
    junit: test-reports/**/*.xml
```

**GitHub Actions:**
```yaml
- name: Publish Test Results
  uses: EnricoMi/publish-unit-test-result-action@v2
  with:
    files: test-reports/**/*.xml
```

## Allure Reports

Advanced reporting with Allure framework:

```bash
# Generate Allure results
api-test run --report-format allure --report-dir allure-results

# Generate Allure HTML report
allure generate allure-results --clean -o allure-report
allure open allure-report
```

### Features

- Beautiful, interactive reports
- Historical trends
- Test categorization
- Attachments (requests/responses)
- Test retries tracking
- Flaky tests detection

### Setup

Install Allure CLI:

```bash
# macOS
brew install allure

# Linux
wget https://github.com/allure-framework/allure2/releases/download/2.24.0/allure-2.24.0.tgz
tar -zxvf allure-2.24.0.tgz
export PATH=$PATH:$PWD/allure-2.24.0/bin

# Verify
allure --version
```

## Markdown Reports

Generate markdown summaries for documentation:

```bash
api-test run --report-format markdown --report-dir test-reports
```

### Output

```markdown
# API Test Results

**Date:** 2024-12-18 10:30:00  
**Duration:** 45.678s  
**Environment:** staging

## Summary

| Metric | Value |
|--------|-------|
| Total Tests | 25 |
| Passed | 23 (92%) |
| Failed | 2 (8%) |
| Skipped | 0 (0%) |

## Failed Tests

### UserApiTest > Update User

**Duration:** 2.456s  
**Error:** Expected status 200 but got 404

```

## Report Command

Generate reports from existing test results:

```bash
api-test report [OPTIONS]
```

### Options

| Option | Short | Description |
|--------|-------|-------------|
| `--input` | `-i` | Input results file (JSON) |
| `--format` | `-f` | Output format(s) |
| `--output` | `-o` | Output directory |
| `--open` | | Open report in browser (HTML only) |

### Examples

```bash
# Generate HTML report from results
api-test report --input results.json --format html --open

# Generate multiple formats
api-test report -i results.json -f html -f junit -f json

# Custom output directory
api-test report -i results.json -f html -o build/reports
```

## Report Comparison

Compare test results across different runs:

```bash
api-test diff --baseline results-baseline.json --current results-current.json
```

### Output

```
Test Result Comparison

Summary:
  Baseline: 25 tests, 23 passed, 2 failed
  Current:  25 tests, 24 passed, 1 failed
  
Changes:
  ✓ UserApiTest > Update User (FAILED → PASSED)
  
New Failures:
  (none)
  
Fixed:
  ✓ UserApiTest > Update User
  
Performance:
  Baseline: 45.678s
  Current:  42.123s
  Improvement: 3.555s (7.8%)
```

## CI Dashboard Integration

### Export Metrics

```bash
# Export summary metrics
api-test run --report-format json | jq '.summary'

# Export for Prometheus
api-test run --export-metrics prometheus > metrics.txt
```

### Prometheus Metrics

```
# HELP api_tests_total Total number of API tests
# TYPE api_tests_total gauge
api_tests_total 25

# HELP api_tests_passed Number of passed tests
# TYPE api_tests_passed gauge
api_tests_passed 23

# HELP api_tests_failed Number of failed tests
# TYPE api_tests_failed gauge
api_tests_failed 2

# HELP api_tests_duration_seconds Test execution duration
# TYPE api_tests_duration_seconds gauge
api_tests_duration_seconds 45.678
```

### Grafana Dashboard

Import metrics into Grafana for visualization:

1. Configure Prometheus to scrape metrics
2. Create Grafana dashboard
3. Add panels for:
   - Pass rate trend
   - Test duration
   - Failed tests count
   - Success rate by test suite

## Custom Report Templates

Create custom HTML templates:

```bash
api-test run --report-format html --template custom-template.html
```

### Template Variables

Available variables in templates:

- `{{summary}}` - Test summary object
- `{{testSuites}}` - List of test suites
- `{{environment}}` - Environment info
- `{{timestamp}}` - Report generation time

## Best Practices

1. **Generate Multiple Formats**: Create HTML for humans, JUnit for CI, JSON for analysis
2. **Archive Reports**: Store historical reports for trend analysis
3. **Attach to CI Builds**: Make reports accessible from CI dashboard
4. **Include Screenshots**: Attach request/response payloads for debugging
5. **Track Metrics**: Monitor pass rate, duration trends over time
6. **Share Reports**: Make reports accessible to team members
7. **Clean Old Reports**: Implement retention policy for reports

## Troubleshooting

### Report Generation Failed

```bash
# Ensure output directory is writable
mkdir -p test-reports
chmod 755 test-reports

# Check disk space
df -h

# Run with verbose logging
api-test run --report-format html --verbose
```

### Missing Test Data

```bash
# Verify results file exists
ls -la test-results/

# Check JSON structure
jq '.' results.json

# Regenerate with fresh run
api-test run --report-format html
```

### Browser Can't Open HTML Report

```bash
# Use absolute path
open $(pwd)/test-reports/index.html

# Or use file:// URL
file://$(pwd)/test-reports/index.html

# Start local server
cd test-reports && python3 -m http.server 8000
# Then open http://localhost:8000
```
