# Integration with CI/CD

## GitHub Actions

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
        env:
          API_TOKEN: ${{ secrets.API_TOKEN }}
        run: mvn test
```

## Jenkins

```groovy
pipeline {
    agent any
    stages {
        stage('API Tests') {
            steps {
                withCredentials([string(credentialsId: 'api-token', variable: 'API_TOKEN')]) {
                    sh 'mvn clean test'
                }
            }
        }
    }
}
```

## GitLab CI

```yaml
test:
  script:
    - mvn test
  variables:
    API_TOKEN: $API_TOKEN
```

See [Configuration Guide](../guide/configuration.md) for managing secrets.
