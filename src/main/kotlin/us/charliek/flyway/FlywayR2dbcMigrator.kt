package us.charliek.flyway

import com.zaxxer.hikari.HikariDataSource
import io.micronaut.context.annotation.Requires
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.context.event.StartupEvent
import jakarta.inject.Singleton
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult
import org.slf4j.LoggerFactory
import us.charliek.flyway.configuration.FlywayR2dbcConfigurationProperties
import us.charliek.flyway.configuration.FlywayR2dbcConnectionResolver
import us.charliek.flyway.exception.FlywayR2dbcMigrationException
import java.io.Closeable
import java.sql.SQLException
import javax.sql.DataSource

@Singleton
@Requires(property = "flyway-r2dbc.enabled", value = "true")
class FlywayR2dbcMigrator(
    private val config: FlywayR2dbcConfigurationProperties,
    private val connectionResolver: FlywayR2dbcConnectionResolver
) : ApplicationEventListener<StartupEvent> {
    
    companion object {
        private val logger = LoggerFactory.getLogger(FlywayR2dbcMigrator::class.java)
    }
    
    override fun onApplicationEvent(event: StartupEvent) {
        try {
            logger.info("Starting Flyway R2DBC migrations...")
            val startTime = System.currentTimeMillis()
            
            val migrationResult = executeMigrationsWithRetry()
            
            val duration = System.currentTimeMillis() - startTime
            logger.info(
                "Flyway R2DBC migrations completed successfully in {}ms. " +
                "Applied {} migrations to version '{}'",
                duration, 
                migrationResult.migrationsExecuted,
                migrationResult.targetSchemaVersion ?: "none"
            )
            
        } catch (e: Exception) {
            logger.error("Flyway R2DBC migration failed", e)
            throw FlywayR2dbcMigrationException("Database migration failed during application startup", e)
        }
    }
    
    private fun executeMigrationsWithRetry(): MigrateResult {
        var lastException: Exception? = null
        
        repeat(config.connectionRetries) { attempt ->
            try {
                return executeMigrations()
            } catch (e: SQLException) {
                lastException = e
                if (attempt < config.connectionRetries - 1) {
                    logger.warn(
                        "Migration attempt {} failed, retrying in {}ms", 
                        attempt + 1, 
                        config.connectionRetryDelay.toMillis(), 
                        e
                    )
                    Thread.sleep(config.connectionRetryDelay.toMillis())
                }
            }
        }
        
        throw FlywayR2dbcMigrationException(
            "Failed to execute migrations after ${config.connectionRetries} attempts", 
            lastException!!
        )
    }
    
    private fun executeMigrations(): MigrateResult {
        val connectionInfo = connectionResolver.resolveJdbcConnection()
        var dataSource: DataSource? = null
        
        try {
            dataSource = connectionInfo.toDataSource()
            validateConnection(dataSource)
            
            val flyway = configureFlyway(dataSource)
            return flyway.migrate()
            
        } finally {
            // Ensure temporary datasource is properly closed
            dataSource?.let { closeDataSource(it) }
        }
    }
    
    private fun validateConnection(dataSource: DataSource) {
        dataSource.connection.use { connection ->
            if (!connection.isValid(30)) {
                throw SQLException("Database connection validation failed")
            }
            logger.debug("Database connection validated successfully")
        }
    }
    
    private fun configureFlyway(dataSource: DataSource): Flyway {
        return Flyway.configure()
            .dataSource(dataSource)
            .locations(*config.locations.toTypedArray())
            .baselineOnMigrate(config.baselineOnMigrate)
            .validateOnMigrate(config.validateOnMigrate)
            .cleanDisabled(config.cleanDisabled)
            .baselineVersion(config.baselineVersion)
            .baselineDescription(config.baselineDescription)
            .placeholderReplacement(config.placeholderReplacement)
            .placeholders(config.placeholders)
            .load()
    }
    
    private fun closeDataSource(dataSource: DataSource) {
        try {
            when (dataSource) {
                is HikariDataSource -> {
                    dataSource.close()
                    logger.debug("Closed temporary HikariDataSource")
                }
                is Closeable -> {
                    dataSource.close()
                    logger.debug("Closed temporary DataSource")
                }
                else -> {
                    logger.debug("DataSource type {} does not implement Closeable", dataSource.javaClass.simpleName)
                }
            }
        } catch (e: Exception) {
            logger.warn("Error closing temporary DataSource", e)
            // Don't propagate - this is cleanup
        }
    }
}