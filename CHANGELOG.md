# Changelog

## [0.0.1-SNAPSHOT] - development

### Initial Release

#### Features
- **R2DBC Fallback Configuration**: Automatically converts R2DBC connection settings to JDBC for Flyway migrations
- **Direct JDBC Configuration**: Option to specify JDBC connection directly for full control
- **PostgreSQL Support**: Full support for PostgreSQL R2DBC to JDBC URL conversion
- **Zero Persistent Connections**: Temporary JDBC connections are created only during migration and immediately cleaned up
- **Retry Logic**: Built-in connection retry mechanism with configurable attempts and delays
- **Comprehensive Error Handling**: Clear, actionable error messages for configuration issues

#### Configuration
- Configurable via `flyway-r2dbc` prefix
- Support for all core Flyway configuration options
- Environment variable substitution support
- Placeholder replacement for dynamic values

#### Testing
- Unit tests for URL conversion and configuration
- Integration tests with TestContainers
- 90%+ test coverage

#### Documentation
- Comprehensive README with usage examples
- Configuration guide
- API documentation

## [0.0.0] - 2025-06-22

#### Documentation
- Setup project in github
- Start initial development
