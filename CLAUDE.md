# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build and Development Commands

### Building the Project
```bash
./gradlew build
```

### Running Tests
```bash
# Run all tests
./gradlew test

# Run a specific test class
./gradlew test --tests "us.charliek.flyway.integration.FlywayR2dbcIntegrationTest"

# Run a specific test method
./gradlew test --tests "us.charliek.flyway.unit.PostgresqlConverterTest.shouldConvertBasicR2dbcUrl"

# Run tests with full output
./gradlew test --info
```

### Linting and Code Quality
```bash
# No explicit linting tool configured - Kotlin compiler handles basic checks during build
./gradlew compileKotlin compileTestKotlin
```

### Publishing
```bash
# Publish release version (requires tag starting with 'v')
./gradlew publish

# Publish snapshot (add -Psnapshot=true)
./gradlew publish -Psnapshot=true
```

### Documentation
```bash
# Serve documentation locally (with hot reload)
cd site-docs
uv run mkdocs serve

# Build documentation static files
cd site-docs
uv run mkdocs build

# The built documentation will be in site-docs/build/
```

## Architecture Overview

### Core Purpose
This library provides Flyway database migrations for Micronaut R2DBC applications without requiring dual datasource configuration. It creates temporary JDBC connections only during migration execution, eliminating the memory overhead of persistent JDBC connection pools.

### Key Architectural Decisions

1. **Single Configuration Source**: The library can use either direct JDBC configuration or automatically convert R2DBC configuration to JDBC, avoiding duplicate database credentials.

2. **Temporary Connections**: JDBC connections are created only during migration and immediately released, keeping the application context clean.

3. **Event-Driven Execution**: Migrations run on `StartupEvent` using Micronaut's `ApplicationEventListener` pattern.

4. **Pluggable Database Support**: URL conversion is handled through a registry pattern, making it easy to add support for new databases.

### Component Structure

**FlywayR2dbcMigrator** (`src/main/kotlin/us/charliek/flyway/FlywayR2dbcMigrator.kt`)
- Main entry point that listens for application startup
- Orchestrates migration execution with retry logic
- Ensures proper cleanup of temporary resources

**FlywayR2dbcConfigurationProperties** (`src/main/kotlin/us/charliek/flyway/configuration/FlywayR2dbcConfigurationProperties.kt`)
- Configuration binding using `@ConfigurationProperties("flyway-r2dbc")`
- Uses mutable properties for Micronaut's property binding mechanism
- Provides all standard Flyway configuration options

**FlywayR2dbcConnectionResolver** (`src/main/kotlin/us/charliek/flyway/configuration/FlywayR2dbcConnectionResolver.kt`)
- Resolves database connections with fallback logic:
  1. Direct JDBC configuration if provided
  2. R2DBC configuration conversion as fallback
  3. Clear error messages if neither exists

**R2dbcToJdbcConverter** (`src/main/kotlin/us/charliek/flyway/converter/R2dbcToJdbcConverter.kt`)
- Interface for database-specific URL conversion
- Creates temporary HikariCP connection pools with:
  - `maximumPoolSize = 2` (Flyway requires at least 2 connections)
  - Short idle timeout (10 seconds) for quick cleanup
  - 1 minute max lifetime to ensure connections don't persist

**PostgresqlConverter** (`src/main/kotlin/us/charliek/flyway/converter/PostgresqlConverter.kt`)
- Converts R2DBC PostgreSQL URLs to JDBC format
- Handles connection options with warnings for unsupported parameters
- Preserves SSL and other connection properties

### Testing Strategy

- **Unit Tests**: Test individual components like URL conversion
- **Integration Tests**: Use TestContainers with real PostgreSQL to verify full migration flow
- Tests require Docker to be running for TestContainers

### Version Management

- Version is set in `build.gradle.kts` with snapshot support
- Snapshots append `-SNAPSHOT` when built with `-Psnapshot=true`
- Release process documented in `docs/RELEASE.md`

### GitHub Actions Workflows

- **CI**: Runs on pushes and PRs to `main` and `develop` branches
- **Snapshot**: Publishes snapshots on every push to `main`
- **Release**: Triggered by tags starting with 'v' (e.g., `v0.0.1`)
- All workflows use GraalVM Community Edition 21