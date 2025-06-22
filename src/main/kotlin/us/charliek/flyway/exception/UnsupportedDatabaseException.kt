package us.charliek.flyway.exception

class UnsupportedDatabaseException(database: String) : 
    RuntimeException(
        """Database '$database' is not yet supported by micronaut-flyway-r2dbc.
Currently supported databases: PostgreSQL
To use $database, provide direct JDBC connection properties in flyway-r2dbc configuration:
flyway-r2dbc:
  url: jdbc:$database://...
  username: user
  password: pass"""
    )