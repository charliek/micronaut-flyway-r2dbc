# Configuration Guide

## Basic Configuration

The simplest configuration uses your existing R2DBC setup:

```yaml
r2dbc:
  datasources:
    default:
      url: "r2dbc:postgresql://localhost:5432/mydb"
      username: "postgres"
      password: "password"

flyway-r2dbc:
  enabled: true
```

## Full Configuration Options

```yaml
flyway-r2dbc:
  enabled: true                              # Enable/disable migrations
  url: "jdbc:postgresql://..."              # Direct JDBC URL (optional)
  username: "user"                          # Database username
  password: "pass"                          # Database password
  locations:                                # Migration file locations
    - "classpath:db/migration"
    - "classpath:db/data"
  baseline-on-migrate: true                 # Baseline on first migration
  validate-on-migrate: true                 # Validate migrations
  clean-disabled: true                      # Disable clean command
  baseline-version: "1"                     # Initial baseline version
  baseline-description: "Initial version"   # Baseline description
  placeholder-replacement: true             # Enable placeholders
  placeholders:                             # Placeholder values
    environment: "production"
    schema: "public"
  connection-retries: 3                     # Connection retry attempts
  connection-retry-delay: "PT1S"            # Delay between retries
```

## Environment Variables

All configuration properties support environment variable substitution:

```yaml
flyway-r2dbc:
  enabled: true
  url: "${FLYWAY_URL:jdbc:postgresql://localhost:5432/mydb}"
  username: "${FLYWAY_USERNAME:postgres}"
  password: "${FLYWAY_PASSWORD:password}"
```

## Connection Resolution

The library resolves database connections in the following order:

1. **Direct JDBC Configuration**: If `flyway-r2dbc.url` is provided
2. **R2DBC Fallback**: If no JDBC config, uses `r2dbc.datasources.default`
3. **Error**: If neither exists, throws configuration exception

## Database Support

### PostgreSQL
Full support for automatic R2DBC to JDBC URL conversion.

### Other Databases
Provide direct JDBC configuration:

```yaml
flyway-r2dbc:
  enabled: true
  url: "jdbc:mysql://localhost:3306/mydb"
  username: "root"
  password: "password"
```