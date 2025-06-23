# Micronaut Flyway R2DBC

!!! warning "Early Development Notice"
    This project is in early development and is not yet recommended for production use. APIs and functionality may change significantly between releases.

## Overview

Micronaut Flyway R2DBC provides seamless database migrations for R2DBC applications without the complexity and memory overhead of maintaining dual datasource configurations.

## Key Features

- **Single Configuration**: Use your existing R2DBC configuration for migrations
- **Zero Runtime Overhead**: JDBC connections exist only during migration execution
- **Production Ready**: Built-in retry logic and comprehensive error handling
- **Fully Compatible**: Leverages the proven Flyway migration engine

## Why This Library?

Traditional approaches to using Flyway with R2DBC applications require:

- Configuring both JDBC and R2DBC datasources
- Maintaining duplicate database credentials
- Additional memory overhead from unused JDBC connection pools
- Complex transaction manager configuration
- Disabled transactions in tests due to datasource conflicts

This library eliminates all these issues by creating temporary JDBC connections only when needed for migrations.

## Quick Example

```yaml
# application.yml
r2dbc:
  datasources:
    default:
      url: "r2dbc:postgresql://localhost:5432/mydb"
      username: "postgres"
      password: "password"

flyway-r2dbc:
  enabled: true
```

That's it! Your migrations will run automatically on application startup.

## Next Steps

- [Installation Guide](getting-started/installation.md) - Add the library to your project
- [Quick Start](getting-started/quick-start.md) - Get up and running in minutes
- [Configuration](getting-started/configuration.md) - Customize migration behavior