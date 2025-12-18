# IntelliJ IDEA Plugin for API Integration Testing Framework

⚠️ **Important**: This plugin module is currently Maven-based but IntelliJ IDEA plugins require Gradle with the IntelliJ Platform Plugin. The code structure is complete but needs Gradle conversion to build and run. See "Building as Gradle Project" section below.

This IntelliJ IDEA plugin provides comprehensive support for the API Integration Testing Framework.

## Features

### 1. **Test Execution**
- Run individual scenarios directly from the editor
- Execute entire test suites with a single click
- Run load tests with configuration
- Gutter icons for quick test execution

### 2. **Visual Test Results**
- Dedicated tool window for test results
- Real-time test execution status
- Detailed test reports with duration and results
- Color-coded status indicators (Pass/Fail/Running)

### 3. **Code Assistance**
- Smart code completion for DSL keywords
- Live templates for common test patterns:
  - `apisuite` - Complete API test suite
  - `apiscenario` - API scenario
  - `apiget` - GET request with expectations
  - `apipost` - POST request with body
- Syntax highlighting for test DSL
- Code inspections and validations

### 4. **Navigation**
- Quick navigation between test definitions
- Go to scenario/suite from test results
- Find usages of reusable steps

### 5. **Test Generation**
- Generate tests from OpenAPI specifications
- Create test scaffolding from existing APIs

## Installation

### From Source
1. Clone the repository
2. Build the plugin: `mvn clean package`
3. In IntelliJ IDEA: **Settings → Plugins → ⚙️ → Install Plugin from Disk**
4. Select the generated JAR from `target/`

### From Marketplace (Coming Soon)
Search for "API Integration Testing Framework" in the IntelliJ IDEA plugin marketplace.

## Usage

### Running Tests

#### From Editor
1. Open a test file
2. Click the gutter icon next to `scenario` or `apiTestSuite`
3. Select "Run Scenario" or "Run Suite"

#### From Context Menu
1. Right-click in the editor or on a file
2. Select **API Testing → Run Scenario/Suite/Load Test**

#### Using Run Configuration
1. **Run → Edit Configurations**
2. Click **+** → **API Test**
3. Configure test path, scenario name, or suite name
4. Click **Run**

### Live Templates

Type the template abbreviation and press **Tab**:

- `apisuite` + **Tab** - Create complete test suite
- `apiscenario` + **Tab** - Create scenario
- `apiget` + **Tab** - GET request template
- `apipost` + **Tab** - POST request template

### Test Results Window

View test results in the **API Tests** tool window (usually at the bottom):
- See all executed scenarios
- Check status (✓ Passed / ✗ Failed / ⏱ Running)
- View execution duration
- Click on a result to navigate to the test

## Configuration

### Run Configuration Options
- **Test Type**: Scenario, Suite, or Load Test
- **Test Path**: Path to the test file
- **Scenario Name**: Name of the scenario to run
- **Suite Name**: Name of the test suite
- **Load Test**: Enable load testing mode

## Development

### Building
```bash
## Building as Gradle Project

To properly build and run this plugin, convert to Gradle:

1. Create `build.gradle.kts`:
```kotlin
plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.22"
    id("org.jetbrains.intellij") version "1.16.1"
}

group = "dev.codersbox.eng.lib"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

intellij {
    version.set("2023.2.5")
    type.set("IC") // IntelliJ IDEA Community Edition
    plugins.set(listOf("org.jetbrains.kotlin"))
}

dependencies {
    // Add core framework as compile dependency
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.22")
}

tasks {
    patchPluginXml {
        sinceBuild.set("232")
        untilBuild.set("241.*")
    }
    
    runIde {
        ideDir.set(file("/path/to/idea"))
    }
}
```

2. Create `src/main/resources/META-INF/plugin.xml` (descriptor file)
3. Build with: `./gradlew buildPlugin`
4. Test with: `./gradlew runIde`

## Current Build (Maven)

**Note**: The Maven build will not produce a working plugin without IntelliJ SDK dependencies configured.

```bash
mvn clean package
```

## Requirements
- IntelliJ IDEA 2023.2 or later
- Kotlin plugin enabled
- API Integration Testing Framework in project dependencies

## Alternative: Use Framework as Library

Until the Gradle plugin is built, use the framework as a library with Kotest's IntelliJ plugin for test execution support.

## Support
For issues and feature requests, please visit our GitHub repository.

## License
Apache License 2.0
