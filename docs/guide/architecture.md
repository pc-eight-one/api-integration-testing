# Architecture

## Overview

The framework follows a layered plugin architecture:

```
┌─────────────────────────────────────────┐
│         Test Suites (Your Tests)         │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│          Core Framework DSL              │
│  - Test Suite & Scenario Management      │
│  - Context & State Management            │
│  - Lifecycle Hooks                       │
│  - Assertions & Validation Engine        │
└──────────────────┬──────────────────────┘
                   │
        ┌──────────┼──────────┐
        │          │          │
┌───────▼────┐ ┌──▼─────┐ ┌─▼────────┐
│  Protocol  │ │ Format │ │   Auth   │
│  Plugins   │ │Plugins │ │ Plugins  │
└────────────┘ └────────┘ └──────────┘
```

## Core Components

### Test Suite DSL
Entry point for defining tests with fluent API.

### Scenario Management
Organizes tests into logical scenarios with shared context.

### Step Execution
Executes individual test steps with lifecycle hooks.

### Plugin Registry
Service discovery for protocol, format, and auth plugins.

### Context Manager
Shared state across steps and scenarios.

### Assertion Engine
Powerful validation with custom matchers.

## Plugin Architecture

Plugins extend the framework through well-defined interfaces:

- **Protocol Plugins**: Add support for new protocols
- **Format Plugins**: Handle different data formats
- **Auth Plugins**: Implement authentication strategies
- **Validator Plugins**: Custom validation logic

## Data Flow

1. Test Suite initialized
2. Plugins registered
3. Scenarios executed sequentially
4. Steps executed with hooks
5. Assertions validated
6. Results collected
7. Cleanup performed
