# Documentation

This directory contains the VitePress documentation for the API Integration Testing Framework.

## Running Documentation Locally

### Prerequisites
- Node.js 18+ installed
- npm or yarn

### Setup
```bash
# Install dependencies
npm install
```

### Development
```bash
# Start dev server with hot reload
npm run docs:dev
```

Visit http://localhost:5173

### Build
```bash
# Build static site
npm run docs:build
```

Output will be in `docs/.vitepress/dist`

### Preview Build
```bash
# Preview production build
npm run docs:preview
```

## Documentation Structure

```
docs/
├── .vitepress/
│   └── config.mts         # VitePress configuration
├── index.md               # Home page
├── guide/                 # User guides
│   ├── introduction.md
│   ├── getting-started.md
│   ├── quick-start.md
│   └── ...
├── plugins/               # Plugin documentation
│   ├── overview.md
│   ├── rest.md
│   ├── graphql.md
│   └── ...
├── examples/              # Code examples
│   ├── rest-examples.md
│   └── ...
├── api/                   # API reference
│   ├── core-api.md
│   └── ...
└── advanced/              # Advanced topics
    ├── custom-plugins.md
    └── ...
```

## Adding New Pages

1. Create a new `.md` file in the appropriate directory
2. Add the page to sidebar in `.vitepress/config.mts`
3. Write content using Markdown and Vue components

## Markdown Features

VitePress supports:
- Standard Markdown
- GitHub-flavored Markdown
- Syntax highlighting
- Custom containers
- Vue components in Markdown
- Emoji :tada:

## Deployment

The documentation can be deployed to:
- GitHub Pages
- Netlify
- Vercel
- Any static hosting service

Build the docs and deploy the `docs/.vitepress/dist` directory.
