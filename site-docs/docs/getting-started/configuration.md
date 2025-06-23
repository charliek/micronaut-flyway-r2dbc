# Configuration

!!! warning "Early Development"
    Configuration options may change in future releases as the library evolves.

This guide covers all configuration options for Micronaut Flyway R2DBC.

## Configuration Approaches

### Approach 1: R2DBC Fallback (Recommended for Development)

The simplest approach - the library automatically converts your R2DBC configuration:

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

### Approach 2: Direct JDBC Configuration (Recommended for Production)

For full control, specify JDBC connection details directly:

```yaml
flyway-r2dbc:
  enabled: true
  url: "${FLYWAY_URL:jdbc:postgresql://localhost:5432/mydb}"
  username: "${FLYWAY_USERNAME:postgres}"
  password: "${FLYWAY_PASSWORD:password}"
```

This approach allows you to:
- Use different credentials for migrations
- Connect to a different database/schema
- Optimize JDBC connection parameters

## All Configuration Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `enabled` | Boolean | `false` | Enable/disable migrations |
| `url` | String | - | JDBC URL (optional if using R2DBC fallback) |
| `username` | String | - | Database username |
| `password` | String | - | Database password |
| `schemas` | List | `[]` | Schemas to manage |
| `table` | String | `flyway_schema_history` | Migration history table name |
| `locations` | List | `["classpath:db/migration"]` | Migration file locations |
| `baseline-on-migrate` | Boolean | `false` | Baseline on first migration |
| `baseline-version` | String | `"1"` | Initial baseline version |
| `baseline-description` | String | `"Initial version"` | Baseline description |
| `validate-on-migrate` | Boolean | `true` | Validate migrations before running |
| `clean-disabled` | Boolean | `true` | Disable clean command (safety) |
| `clean-on-validation-error` | Boolean | `false` | Clean on validation errors |
| `placeholder-replacement` | Boolean | `false` | Enable placeholder replacement |
| `placeholders` | Map | `{}` | Placeholder key-value pairs |
| `target` | String | - | Target migration version |
| `out-of-order` | Boolean | `false` | Allow out-of-order migrations |
| `connection-retries` | Integer | `3` | Connection retry attempts |
| `connection-retry-delay` | Duration | `PT1S` | Delay between retries |

## Common Configuration Examples

### Existing Database with Baseline

For databases that already have a schema:

```yaml
flyway-r2dbc:
  enabled: true
  baseline-on-migrate: true
  baseline-version: "1.0"
  baseline-description: "Existing schema"
```

### Multiple Migration Locations

```yaml
flyway-r2dbc:
  enabled: true
  locations:
    - "classpath:db/migration"
    - "classpath:db/custom"
    - "filesystem:/opt/migrations"
```

### Environment-Specific Placeholders

```yaml
flyway-r2dbc:
  enabled: true
  placeholder-replacement: true
  placeholders:
    environment: "${ENVIRONMENT:development}"
    region: "${AWS_REGION:us-east-1}"
    feature_flag: "${FEATURE_ENABLED:false}"
```

Use in migrations:

```sql
-- V2__add_environment_config.sql
INSERT INTO config (key, value) VALUES 
  ('environment', '${environment}'),
  ('region', '${region}'),
  ('feature_enabled', ${feature_flag});
```

### Schema Management

```yaml
flyway-r2dbc:
  enabled: true
  schemas:
    - "public"
    - "audit"
  table: "migration_history"  # Custom history table name
```

### Retry Configuration

For unreliable network environments:

```yaml
flyway-r2dbc:
  enabled: true
  connection-retries: 5
  connection-retry-delay: PT2S  # ISO-8601 duration
```

### Production Configuration

```yaml
flyway-r2dbc:
  enabled: "${FLYWAY_ENABLED:true}"
  url: "${FLYWAY_JDBC_URL}"
  username: "${FLYWAY_USERNAME}"
  password: "${FLYWAY_PASSWORD}"
  schemas: ["app_schema"]
  validate-on-migrate: true
  clean-disabled: true  # Never allow clean in production
  out-of-order: false   # Enforce migration order
  locations:
    - "classpath:db/migration"
    - "classpath:db/hotfixes"  # Emergency fixes
```

## Migration File Locations

### Classpath Resources

Default location for packaged migrations:

```
src/main/resources/
└── db/
    └── migration/
        ├── V1__initial_schema.sql
        ├── V2__add_users.sql
        └── V3__add_indexes.sql
```

### Filesystem Locations

For external migrations:

```yaml
flyway-r2dbc:
  locations:
    - "filesystem:/opt/app/migrations"
    - "filesystem:${MIGRATION_PATH}/current"
```

### Multiple Locations

Migrations are executed in order across all locations:

```yaml
flyway-r2dbc:
  locations:
    - "classpath:db/migration"      # Core migrations
    - "classpath:db/seed"           # Seed data
    - "filesystem:/opt/migrations"  # External migrations
```

## Disabling Migrations

### For Tests

```yaml
# application-test.yml
flyway-r2dbc:
  enabled: false
```

### Conditionally

```yaml
flyway-r2dbc:
  enabled: "${ENABLE_MIGRATIONS:true}"
```

## Database Support

### PostgreSQL ✅

PostgreSQL has full support for automatic R2DBC to JDBC URL conversion:

```yaml
r2dbc:
  datasources:
    default:
      url: "r2dbc:postgresql://localhost:5432/mydb"
      username: "postgres"
      password: "password"
```

Supported URL formats:
- `r2dbc:postgresql://host:port/database`
- `r2dbc:pool:postgresql://host:port/database`
- With options: `r2dbc:postgresql://host:port/database?ssl=true&sslmode=require`

### Other Databases 🚧

For databases without R2DBC URL conversion support (MySQL, MariaDB, SQL Server, etc.), use direct JDBC configuration:

```yaml
# Your R2DBC configuration for the application
r2dbc:
  datasources:
    default:
      url: "r2dbc:mysql://localhost:3306/mydb"
      username: "app_user"
      password: "app_password"

# Direct JDBC configuration for migrations
flyway-r2dbc:
  enabled: true
  url: "jdbc:mysql://localhost:3306/mydb"
  username: "migration_user"
  password: "migration_password"
```

## Migration Files

Place your Flyway migration files in the standard location:

```
src/main/resources/db/migration/
├── V1__create_users_table.sql
├── V2__create_posts_table.sql
└── V3__add_indexes.sql
```

Example migration:

```sql
-- V1__create_users_table.sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
```

Migration files must follow the naming convention: `V{version}__{description}.sql`

## Next Steps

- Learn [How It Works](../core-concepts/how-it-works.md)
- Check the [Quick Start](quick-start.md)