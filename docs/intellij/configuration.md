# IntelliJ Plugin Configuration

Configure the API Integration Testing Framework plugin to match your workflow and project requirements.

## Accessing Settings

**File** → **Settings/Preferences** → **Tools** → **API Testing Framework**

## General Settings

### Default Timeout

```
Test Execution:
  Default timeout: [30] seconds
  ☑ Apply to all tests
  ☐ Override per scenario
```

Sets the default timeout for all API requests.

### Base URL Configuration

```
Environment URLs:
  Development: http://localhost:8080
  Staging: https://staging-api.example.com
  Production: https://api.example.com
  
Active Environment: [Development ▼]
```

### Output Directories

```
Directories:
  Reports: build/reports/api-tests
  Test Data: src/test/resources/testdata
  Logs: build/logs
  
☑ Create directories if missing
```

## Test Runner Settings

### Execution

```
Execution:
  ☑ Run tests in parallel (max threads: 4)
  ☑ Stop on first failure
  ☐ Retry failed tests (attempts: 3)
  
  Before run:
    ☑ Clean previous results
    ☐ Run database migrations
```

### Reporting

```
Reports:
  Format: [HTML ▼] [JSON] [XML]
  ☑ Auto-open report after run
  ☑ Include request/response bodies
  ☑ Capture screenshots on failure
  Max body size: 1MB
```

## Editor Settings

### Code Completion

```
Code Completion:
  ☑ Enable auto-completion for DSL
  ☑ Show parameter hints
  ☑ Suggest variable names
  Case sensitivity: [First letter ▼]
```

### Live Templates

```
Live Templates:
  ☑ Enable live templates
  Template style: [Kotlin DSL ▼]
  
  Available templates: [Manage...]
```

### Visual Editor

```
Visual Editor:
  ☑ Enable visual editor
  ☑ Two-way sync with code
  Sync delay: 2 seconds
  Theme: [Follow IDE ▼]
```

## Authentication

### Default Auth Configuration

```
Authentication:
  Type: [Bearer Token ▼]
  
  Bearer Token:
    Token: ${env.API_TOKEN}
    Header name: Authorization
    Prefix: Bearer
    
  ☑ Load from environment
  ☑ Encrypt stored credentials
```

### OAuth 2.0 Setup

```
OAuth 2.0:
  Token URL: https://auth.example.com/oauth/token
  Client ID: ${env.CLIENT_ID}
  Client Secret: ${env.CLIENT_SECRET}
  Scopes: api:read, api:write
  
  ☑ Auto-refresh token
  Token cache: In-memory
```

## Test Explorer

### Display Options

```
Test Explorer:
  ☑ Show test duration
  ☑ Show tags
  ☑ Auto-scroll to running test
  ☑ Group by test suite
  ☐ Flatten hierarchy
  
  Tree indent: 20px
  Icon size: Medium
```

### Filtering

```
Filters:
  Default filter: [All tests ▼]
  ☑ Remember filter per project
  
  Quick filters:
    • @smoke
    • @critical
    • Failed tests
```

## Load Testing

### Performance Monitoring

```
Load Testing:
  Default virtual users: 10
  Default duration: 5 minutes
  Default ramp-up: 30 seconds
  
  Monitoring:
    ☑ Show real-time charts
    ☑ Record metrics
    Update interval: 1 second
```

### Resource Limits

```
Resources:
  Max heap size: 2GB
  Max threads: 100
  Connection pool size: 50
  
  ☑ Warn on high resource usage
```

## Integration

### Version Control

```
VCS Integration:
  ☑ Store run configurations in VCS
  ☑ Commit test data files
  ☐ Auto-commit test results
```

### CI/CD

```
CI/CD:
  Build tool: [Maven ▼]
  Test command: mvn test
  
  ☑ Generate JUnit XML reports
  ☑ Export for Jenkins
  ☑ Send notifications on failure
```

### External Tools

```
External Tools:
  API Client: [Postman ▼]
  ☑ Export to Postman
  ☑ Import from Swagger/OpenAPI
```

## Advanced

### Performance

```
Performance:
  ☑ Enable caching
  Cache size: 100MB
  ☑ Preload test definitions
  
  Indexing:
    ☑ Index test files on startup
    ☐ Background indexing
```

### Logging

```
Logging:
  Log level: [INFO ▼]
  ☑ Log to file
  Log file: logs/api-tests.log
  Max file size: 10MB
  
  ☑ Include request/response in logs
```

### Experimental Features

```
Experimental:
  ☐ AI-powered test generation
  ☐ Smart test suggestions
  ☐ Automatic assertion generation
```

## Import/Export Settings

### Export Configuration

1. Click **Export Settings**
2. Select components to export
3. Save to file

### Import Configuration

1. Click **Import Settings**
2. Select settings file
3. Choose components to import
4. Restart IDE

### Share with Team

```bash
# Export to version control
.idea/
  apiTestingFramework.xml  # Commit this file

# Team members get settings automatically
```

## Environment Variables

### Supported Variables

- `API_BASE_URL`: Base URL for API
- `API_TOKEN`: Authentication token
- `TEST_ENV`: Environment name (dev, staging, prod)
- `CLIENT_ID`: OAuth client ID
- `CLIENT_SECRET`: OAuth client secret

### Usage in Configuration

```
${env.VARIABLE_NAME}         # Environment variable
${sys.PROPERTY_NAME}         # System property
${project.PATH}              # Project path
```

## Troubleshooting

### Reset to Defaults

1. Settings → API Testing Framework
2. Click **Reset to Defaults**
3. Confirm and restart IDE

### Clear Cache

1. **File** → **Invalidate Caches**
2. Select **API Testing Framework Cache**
3. Restart IDE

### View Logs

**Help** → **Show Log in Files** → Search for "APITesting"

## Best Practices

1. **Store sensitive data in environment variables**
2. **Commit project-level settings to VCS**
3. **Use different configurations per environment**
4. **Enable auto-save for test results**
5. **Configure appropriate timeouts**
6. **Enable parallel execution for faster runs**
