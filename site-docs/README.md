# Micronaut Flyway R2DBC Documentation

This directory contains the documentation source for the Micronaut Flyway R2DBC project, built with [MkDocs](https://www.mkdocs.org/) and the [Material theme](https://squidfunk.github.io/mkdocs-material/).

## Prerequisites

- Python 3.8+
- uv (Python package manager)

## Quick Start

### Serve Documentation Locally

```bash
# From the site-docs directory
uv run mkdocs serve
```

The documentation will be available at http://localhost:8000 with hot reloading enabled.

### Build Static Documentation

```bash
# From the site-docs directory
uv run mkdocs build
```

The built documentation will be in the `build/` directory.

## Project Structure

```
site-docs/
├── mkdocs.yml          # MkDocs configuration
├── docs/               # Documentation source files
│   ├── index.md        # Home page
│   ├── getting-started/
│   │   ├── installation.md
│   │   ├── quick-start.md
│   │   └── configuration.md
│   ├── core-concepts/
│   │   └── how-it-works.md
│   └── development/
│       └── release-process.md
└── build/              # Generated static site (git ignored)
```

## Writing Documentation

### Markdown Extensions

The documentation supports several markdown extensions:

- **Admonitions** for callouts:
  ```markdown
  !!! warning "Title"
      Content of the warning
  ```

- **Code blocks** with syntax highlighting:
  ````markdown
  ```yaml
  flyway-r2dbc:
    enabled: true
  ```
  ````

- **Tabs** for alternative content:
  ```markdown
  === "Gradle"
      ```kotlin
      implementation("us.charliek:micronaut-flyway-r2dbc:0.0.1")
      ```
  
  === "Maven"
      ```xml
      <dependency>
          <groupId>us.charliek</groupId>
          <artifactId>micronaut-flyway-r2dbc</artifactId>
          <version>0.0.1</version>
      </dependency>
      ```
  ```

### Style Guidelines

1. **Page Titles**: Use H1 (`#`) for page titles
2. **Sections**: Use H2 (`##`) for main sections, H3 (`###`) for subsections
3. **Code Examples**: Always specify the language for syntax highlighting
4. **Links**: Use relative links for internal documentation
5. **Images**: Store images in `docs/images/` (if needed)

### Adding New Pages

1. Create the markdown file in the appropriate directory
2. Add the page to the navigation in `mkdocs.yml`:
   ```yaml
   nav:
     - Section:
       - New Page: section/new-page.md
   ```

## MkDocs Configuration

Key configuration in `mkdocs.yml`:

- **Theme**: Material theme with light/dark mode toggle
- **Plugins**: Search and mkdocstrings for API documentation
- **Extensions**: Various PyMdown extensions for enhanced markdown
- **Navigation**: Organized hierarchical structure

## Deployment

Documentation is automatically deployed to GitHub Pages when changes are merged to the `main` branch. The workflow:

1. Builds the documentation
2. Deploys to the `gh-pages` branch
3. Available at https://charliek.github.io/micronaut-flyway-r2dbc/

## Local Development Tips

### Live Reload

MkDocs automatically reloads when you save changes to:
- Markdown files in `docs/`
- The `mkdocs.yml` configuration

### Search Testing

The search functionality works in the served version. Test it to ensure your content is searchable.

### Link Checking

Before committing, check for broken links:
```bash
# Install linkchecker if needed
pip install linkchecker

# Run from site-docs directory
linkchecker http://localhost:8000
```

## Troubleshooting

### Port Already in Use

If port 8000 is already in use:
```bash
uv run mkdocs serve -a localhost:8001
```

### Build Warnings

Address any warnings during build:
- Missing references
- Duplicate labels
- Invalid markdown syntax

### Theme Issues

If the theme doesn't load properly:
1. Ensure all dependencies are installed: `uv sync`
2. Clear browser cache
3. Try incognito/private browsing mode

## Contributing to Documentation

1. Follow the project's contributing guidelines
2. Keep documentation in sync with code changes
3. Test all code examples
4. Review in both light and dark themes
5. Check mobile responsiveness

## Resources

- [MkDocs Documentation](https://www.mkdocs.org/)
- [Material for MkDocs](https://squidfunk.github.io/mkdocs-material/)
- [Python Markdown Extensions](https://python-markdown.github.io/extensions/)
- [MkDocs Best Practices](https://www.mkdocs.org/user-guide/writing-your-docs/)