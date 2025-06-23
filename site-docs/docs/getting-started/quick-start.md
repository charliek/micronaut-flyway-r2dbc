# Quick Start

!!! warning "Early Development"
    This library is in early development and should not be used in production environments yet.

This guide will help you get Flyway migrations running in your R2DBC application in just a few minutes.

## Step 1: Enable Migrations

Add this minimal configuration to your `application.yml`:

```yaml
flyway-r2dbc:
  enabled: true
```

That's it! The library will automatically use your R2DBC configuration for migrations.

## Step 2: Create Your First Migration

Create a directory for your SQL migrations:

```bash
mkdir -p src/main/resources/db/migration
```

Add your first migration file `V1__create_users_table.sql`:

```sql
-- src/main/resources/db/migration/V1__create_users_table.sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_users_email ON users(email);
```

## Step 3: Run Your Application

Start your application normally:

```bash
./gradlew run
```

You'll see migration logs during startup:

```
INFO  us.charliek.flyway.FlywayR2dbcMigrator - Starting Flyway migrations...
INFO  org.flywaydb.core.internal.command.DbMigrate - Current version of schema "public": << Empty Schema >>
INFO  org.flywaydb.core.internal.command.DbMigrate - Migrating schema "public" to version "1 - create users table"
INFO  us.charliek.flyway.FlywayR2dbcMigrator - Flyway migrations completed successfully
```

## Step 4: Add More Migrations

As your application evolves, add new migrations with incrementing version numbers:

```sql
-- V2__add_user_profile.sql
CREATE TABLE user_profiles (
    user_id BIGINT PRIMARY KEY REFERENCES users(id),
    bio TEXT,
    avatar_url VARCHAR(500),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);
```

```sql
-- V3__add_posts_table.sql
CREATE TABLE posts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    title VARCHAR(200) NOT NULL,
    content TEXT,
    published_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_posts_user_id ON posts(user_id);
CREATE INDEX idx_posts_published_at ON posts(published_at);
```

## Complete Example

Here's a complete Micronaut application configuration:

```yaml
# application.yml
micronaut:
  application:
    name: myapp

r2dbc:
  datasources:
    default:
      url: "r2dbc:postgresql://localhost:5432/myapp"
      username: "${DB_USERNAME:postgres}"
      password: "${DB_PASSWORD:password}"
      initial-size: 5
      max-size: 20

flyway-r2dbc:
  enabled: true
  baseline-on-migrate: true  # Useful for existing databases
```

## Troubleshooting

### Migration Not Running?

Check that:
1. `flyway-r2dbc.enabled` is set to `true`
2. Your migration files are in `src/main/resources/db/migration/`
3. Migration files follow the naming pattern: `V{version}__{description}.sql`

### Connection Issues?

The library will log detailed error messages. Common issues:
- Database not running
- Incorrect credentials
- Network connectivity

### Need More Control?

See the [Configuration Guide](configuration.md) for advanced options like:
- Custom migration locations
- Placeholders
- Baseline configuration
- Direct JDBC configuration

## Next Steps

- Learn about [Configuration Options](configuration.md)
- Understand [How It Works](../core-concepts/how-it-works.md)
- Explore [Database Support](../user-guide/database-support.md)