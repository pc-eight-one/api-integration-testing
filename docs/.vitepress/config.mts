import { defineConfig } from 'vitepress'

export default defineConfig({
  title: 'API Integration Testing Framework',
  description: 'A powerful Kotlin DSL framework for comprehensive API integration and load testing',
  themeConfig: {
    nav: [
      { text: 'Home', link: '/' },
      { text: 'Guide', link: '/guide/getting-started' },
      { text: 'Plugins', link: '/plugins/overview' },
      { text: 'API Reference', link: '/api/core-api' }
    ],

    sidebar: [
      {
        text: 'Introduction',
        items: [
          { text: 'What is it?', link: '/guide/introduction' },
          { text: 'Getting Started', link: '/guide/getting-started' },
          { text: 'Quick Start', link: '/guide/quick-start' },
          { text: 'Installation', link: '/guide/installation' }
        ]
      },
      {
        text: 'Core Concepts',
        items: [
          { text: 'Architecture', link: '/guide/architecture' },
          { text: 'Test Suites & Scenarios', link: '/guide/test-suites' },
          { text: 'Shared Context', link: '/guide/shared-context' },
          { text: 'Lifecycle & Hooks', link: '/guide/lifecycle-hooks' },
          { text: 'Reusable Steps', link: '/guide/reusable-steps' },
          { text: 'Scenario Chaining', link: '/guide/scenario-chaining' }
        ]
      },
      {
        text: 'Testing Features',
        items: [
          { text: 'Data-Driven Testing', link: '/guide/data-driven-testing' },
          { text: 'Assertions & Validation', link: '/guide/assertions' },
          { text: 'Test Data Management', link: '/guide/test-data' },
          { text: 'Configuration', link: '/guide/configuration' },
          { text: 'Load Testing', link: '/guide/load-testing' }
        ]
      },
      {
        text: 'Protocol Plugins',
        items: [
          { text: 'Plugin Overview', link: '/plugins/overview' },
          { text: 'REST API', link: '/plugins/rest' },
          { text: 'GraphQL', link: '/plugins/graphql' },
          { text: 'gRPC', link: '/plugins/grpc' },
          { text: 'WebSocket', link: '/plugins/websocket' },
          { text: 'SOAP', link: '/plugins/soap' },
          { text: 'Messaging (Kafka/RabbitMQ)', link: '/plugins/messaging' }
        ]
      },
      {
        text: 'Format Plugins',
        items: [
          { text: 'Overview', link: '/plugins/formats/' },
          { text: 'JSON', link: '/plugins/formats/json' },
          { text: 'XML', link: '/plugins/formats/xml' },
          { text: 'CSV', link: '/plugins/formats/csv' },
          { text: 'Protocol Buffers', link: '/plugins/formats/protobuf' }
        ]
      },
      {
        text: 'Authentication',
        items: [
          { text: 'Overview', link: '/plugins/auth/' },
          { text: 'Basic Auth', link: '/plugins/auth/basic-auth' },
          { text: 'Bearer Token', link: '/plugins/auth/bearer-token' },
          { text: 'OAuth 2.0', link: '/plugins/auth/oauth2' },
          { text: 'API Key', link: '/plugins/auth/api-key' }
        ]
      },
      {
        text: 'CLI Tools',
        items: [
          { text: 'CLI Overview', link: '/cli/overview' },
          { text: 'Test Generation', link: '/cli/test-generation' },
          { text: 'Running Tests', link: '/cli/running-tests' },
          { text: 'Reporting', link: '/cli/reporting' }
        ]
      },
      {
        text: 'IntelliJ IDEA Plugin',
        items: [
          { text: 'Plugin Overview', link: '/intellij/overview' },
          { text: 'Installation', link: '/intellij/installation' },
          { text: 'Running Tests', link: '/intellij/running-tests' },
          { text: 'Test Explorer', link: '/intellij/test-explorer' },
          { text: 'Live Templates', link: '/intellij/live-templates' },
          { text: 'Visual Test Editor', link: '/intellij/visual-editor' },
          { text: 'Configuration', link: '/intellij/configuration' }
        ]
      },
      {
        text: 'Advanced Topics',
        items: [
          { text: 'Custom Plugins', link: '/advanced/custom-plugins' },
          { text: 'Custom Validators', link: '/advanced/custom-validators' },
          { text: 'Custom Matchers', link: '/advanced/custom-matchers' },
          { text: 'Integration with CI/CD', link: '/advanced/cicd' }
        ]
      },
      {
        text: 'Examples',
        items: [
          { text: 'REST API Examples', link: '/examples/rest-examples' },
          { text: 'GraphQL Examples', link: '/examples/graphql-examples' },
          { text: 'Load Testing Examples', link: '/examples/load-testing-examples' },
          { text: 'Complete Test Suites', link: '/examples/complete-suites' }
        ]
      },
      {
        text: 'API Reference',
        items: [
          { text: 'Core API', link: '/api/core-api' },
          { text: 'Plugin API', link: '/api/plugin-api' },
          { text: 'DSL Reference', link: '/api/dsl-reference' }
        ]
      }
    ],

    socialLinks: [
      { icon: 'github', link: 'https://github.com/codersbox/api-integration-testing' }
    ],

    search: {
      provider: 'local'
    },

    footer: {
      message: 'Released under the Apache 2.0 License.',
      copyright: 'Copyright © 2024 Codersbox Engineering'
    }
  }
})
