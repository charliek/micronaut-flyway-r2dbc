package us.charliek.flyway.integration

import io.micronaut.context.ApplicationContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import us.charliek.flyway.exception.UnsupportedDatabaseException
import us.charliek.flyway.exception.FlywayR2dbcMigrationException
import javax.sql.DataSource

@Testcontainers
class FlywayR2dbcIntegrationTest {

    companion object {
        @Container
        @JvmStatic
        val postgres = PostgreSQLContainer<Nothing>("postgres:16-alpine").apply {
            withDatabaseName("testdb")
            withUsername("testuser")
            withPassword("testpass")
        }
    }

    @Test
    fun `should execute migrations using R2DBC fallback configuration`() {
        val applicationContext = ApplicationContext.run(
            mapOf(
                "flyway-r2dbc.enabled" to "true",
                "flyway-r2dbc.baseline-on-migrate" to "true",
                "r2dbc.datasources.default.url" to "r2dbc:postgresql://${postgres.host}:${postgres.getMappedPort(5432)}/${postgres.databaseName}",
                "r2dbc.datasources.default.username" to postgres.username,
                "r2dbc.datasources.default.password" to postgres.password
            )
        )

        // Verify migrations executed
        postgres.createConnection("").use { conn ->
            val stmt = conn.createStatement()
            val rs = stmt.executeQuery("SELECT version FROM flyway_schema_history ORDER BY installed_rank")
            
            val appliedVersions = mutableListOf<String>()
            while (rs.next()) {
                appliedVersions.add(rs.getString("version"))
            }
            
            assertThat(appliedVersions).contains("1", "2")
        }

        // Verify test data was inserted
        postgres.createConnection("").use { conn ->
            val stmt = conn.createStatement()
            val rs = stmt.executeQuery("SELECT COUNT(*) FROM test_users")
            rs.next()
            assertThat(rs.getInt(1)).isEqualTo(2)
        }

        // Verify no persistent JDBC beans exist
        assertThat(applicationContext.findBean(DataSource::class.java)).isEmpty

        applicationContext.close()
    }

    @Test
    fun `should execute migrations using direct JDBC configuration`() {
        val applicationContext = ApplicationContext.run(
            mapOf(
                "flyway-r2dbc.enabled" to "true",
                "flyway-r2dbc.url" to "jdbc:postgresql://${postgres.host}:${postgres.getMappedPort(5432)}/${postgres.databaseName}",
                "flyway-r2dbc.username" to postgres.username,
                "flyway-r2dbc.password" to postgres.password,
                "flyway-r2dbc.baseline-on-migrate" to "true"
            )
        )

        // Verify migrations executed without R2DBC configuration
        postgres.createConnection("").use { conn ->
            val stmt = conn.createStatement()
            val rs = stmt.executeQuery("SELECT COUNT(*) FROM flyway_schema_history")
            rs.next()
            assertThat(rs.getInt(1)).isGreaterThan(0)
        }

        applicationContext.close()
    }

    @Test
    fun `should throw exception for unsupported database`() {
        val exception = assertThrows<FlywayR2dbcMigrationException> {
            ApplicationContext.run(
                mapOf(
                    "flyway-r2dbc.enabled" to "true",
                    "r2dbc.datasources.default.url" to "r2dbc:mysql://localhost:3306/testdb",
                    "r2dbc.datasources.default.username" to "user",
                    "r2dbc.datasources.default.password" to "pass"
                )
            )
        }
        
        assertThat(exception.message).contains("Database migration failed")
        assertThat(exception.cause).isInstanceOf(UnsupportedDatabaseException::class.java)
        assertThat(exception.cause?.message).contains("mysql")
        assertThat(exception.cause?.message).contains("not yet supported")
    }

    @Test
    fun `should not run when disabled`() {
        val applicationContext = ApplicationContext.run(
            mapOf(
                "flyway-r2dbc.enabled" to "false",
                "r2dbc.datasources.default.url" to "r2dbc:postgresql://localhost:5432/testdb",
                "r2dbc.datasources.default.username" to "user",
                "r2dbc.datasources.default.password" to "pass"
            )
        )

        // Should start without error but not run migrations
        assertThat(applicationContext.isRunning).isTrue()
        
        applicationContext.close()
    }
}