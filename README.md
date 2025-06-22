# Micronaut Flyway R2DBC

A Micronaut library that provides Flyway database migrations for R2DBC applications without the complexity and memory overhead of dual datasource configuration.

## Problem Statement

Current R2DBC applications using Flyway face several challenges:

- **Dual Datasource Complexity**: Requires both JDBC and R2DBC datasources configured simultaneously
- **Memory Overhead**: Additional JDBC connection pools consume unnecessary memory
- **Transaction Manager Issues**: Multiple datasources complicate transaction management
- **Testing Complications**: MicronautTest requires disabling transactions due to datasource conflicts
- **Configuration Redundancy**: Database connection details must be specified twice

## Solution

`micronaut-flyway-r2dbc` eliminates these issues by:

1. **Single Configuration**: Uses only R2DBC connection properties
2. **Temporary JDBC**: Creates JDBC connections only during migration execution
3. **Clean Lifecycle**: No persistent JDBC datasources in the application context
4. **Flyway Compatibility**: Leverages proven Flyway core functionality
5. **Database Agnostic**: Supports multiple databases through pluggable URL conversion

## Installation

Add the following to your build:

### Gradle (Kotlin)

```kotlin
// Add GitHub Packages repository
repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/charliek/micronaut-flyway-r2dbc")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
        }
    }
}

dependencies {
    implementation("us.charliek:micronaut-flyway-r2dbc:0.0.1")

    // Add your JDBC driver (required for migrations)
    runtimeOnly("org.postgresql:postgresql:42.7.3")
}
```

**Note**: GitHub Packages requires authentication for ALL packages (including releases from public repositories). This is a GitHub limitation. To access this library:

1. Create a [personal access token](https://github.com/settings/tokens) with `read:packages` scope
2. Add credentials to `~/.gradle/gradle.properties`:

```properties
gpr.user=YOUR_GITHUB_USERNAME
gpr.key=YOUR_PERSONAL_ACCESS_TOKEN
```

Alternatively, use environment variables `USERNAME` and `TOKEN`.

## Configuration

### Option 1: R2DBC Fallback (Recommended for Development)

The simplest configuration - just enable the library and it will use your existing R2DBC configuration:

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

### Option 2: Direct JDBC Configuration (Recommended for Production)

For full control over the JDBC connection used for migrations:

```yaml
# application.yml
flyway-r2dbc:
  enabled: true
  url: "${FLYWAY_URL:jdbc:postgresql://localhost:5432/mydb}"
  username: "${FLYWAY_USERNAME:postgres}"
  password: "${FLYWAY_PASSWORD:password}"

  # Standard Flyway properties
  locations: ["classpath:db/migration"]
  baseline-on-migrate: true
  validate-on-migrate: true
  clean-disabled: true
  baseline-version: "1"
  baseline-description: "Initial version"
  placeholder-replacement: true
  placeholders:
    environment: "${ENVIRONMENT:development}"

# Separate R2DBC configuration for application runtime
r2dbc:
  datasources:
    default:
      url: "${R2DBC_URL:r2dbc:postgresql://localhost:5432/mydb}"
      username: "${R2DBC_USERNAME:postgres}"
      password: "${R2DBC_PASSWORD:password}"
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

## How It Works

1. **Application Startup**: The library listens for Micronaut's `ApplicationStartupEvent`
2. **Connection Resolution**:
   - First checks for direct JDBC configuration (`flyway-r2dbc.url`)
   - Falls back to converting R2DBC configuration if no JDBC config is found
3. **Migration Execution**:
   - Creates a temporary JDBC connection pool (size=1)
   - Executes Flyway migrations
   - Immediately closes and releases the connection pool
4. **Clean State**: No JDBC datasources remain in the application context

## Database Support

### Currently Supported
- **PostgreSQL**: Full R2DBC to JDBC URL conversion

### Using Unsupported Databases
For databases not yet supported for automatic conversion, provide direct JDBC configuration:

```yaml
flyway-r2dbc:
  enabled: true
  url: "jdbc:mysql://localhost:3306/mydb"
  username: "root"
  password: "password"
```

## Configuration Properties

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `flyway-r2dbc.enabled` | Boolean | `false` | Enable/disable migrations |
| `flyway-r2dbc.url` | String | - | JDBC URL (optional, uses R2DBC fallback if not set) |
| `flyway-r2dbc.username` | String | - | Database username |
| `flyway-r2dbc.password` | String | - | Database password |
| `flyway-r2dbc.locations` | List | `["classpath:db/migration"]` | Migration file locations |
| `flyway-r2dbc.baseline-on-migrate` | Boolean | `false` | Baseline on first migration |
| `flyway-r2dbc.validate-on-migrate` | Boolean | `true` | Validate migrations |
| `flyway-r2dbc.clean-disabled` | Boolean | `true` | Disable clean command |
| `flyway-r2dbc.baseline-version` | String | `"1"` | Initial baseline version |
| `flyway-r2dbc.baseline-description` | String | `"Initial version"` | Baseline description |
| `flyway-r2dbc.placeholder-replacement` | Boolean | `false` | Enable placeholders |
| `flyway-r2dbc.placeholders` | Map | `{}` | Placeholder values |
| `flyway-r2dbc.connection-retries` | Integer | `3` | Connection retry attempts |
| `flyway-r2dbc.connection-retry-delay` | Duration | `PT1S` | Delay between retries |

## Benefits

- ✅ **Zero Persistent JDBC Connections**: No memory overhead from unused connection pools
- ✅ **Simple Configuration**: Single set of database credentials
- ✅ **Transaction Manager Compatibility**: No conflicts with R2DBC transaction management
- ✅ **Test-Friendly**: Works seamlessly with `@MicronautTest`
- ✅ **Production-Ready**: Retry logic and comprehensive error handling

## License

This project is licensed under the Apache License 2.0 - see the LICENSE file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Support

For issues and feature requests, please use the [GitHub issue tracker](https://github.com/charliek/micronaut-flyway-r2dbc/issues).
