package us.charliek.flyway.configuration

import io.micronaut.context.BeanContext
import io.micronaut.context.annotation.Value
import jakarta.inject.Singleton
import org.slf4j.LoggerFactory
import us.charliek.flyway.converter.ConverterRegistry
import us.charliek.flyway.converter.JdbcConnectionInfo
import us.charliek.flyway.exception.FlywayR2dbcConfigurationException

@Singleton
class FlywayR2dbcConnectionResolver(
    private val config: FlywayR2dbcConfigurationProperties,
    private val converterRegistry: ConverterRegistry,
    private val beanContext: BeanContext,
    @Value("\${r2dbc.datasources.default.url:}") private val r2dbcUrl: String?,
    @Value("\${r2dbc.datasources.default.username:}") private val r2dbcUsername: String?,
    @Value("\${r2dbc.datasources.default.password:}") private val r2dbcPassword: String?
) {
    
    companion object {
        private val logger = LoggerFactory.getLogger(FlywayR2dbcConnectionResolver::class.java)
    }
    
    fun resolveJdbcConnection(): JdbcConnectionInfo {
        // Priority 1: Direct JDBC configuration
        if (!config.url.isNullOrBlank()) {
            logger.info("Using direct JDBC configuration from flyway-r2dbc.url")
            
            if (config.username.isNullOrBlank() || config.password.isNullOrBlank()) {
                throw FlywayR2dbcConfigurationException(
                    """Direct JDBC configuration requires all connection properties:
                    |flyway-r2dbc:
                    |  url: jdbc:postgresql://localhost:5432/mydb
                    |  username: myuser
                    |  password: mypassword""".trimMargin()
                )
            }
            
            return JdbcConnectionInfo(
                url = config.url!!,
                username = config.username!!,
                password = config.password!!
            )
        }
        
        // Priority 2: R2DBC fallback
        if (!r2dbcUrl.isNullOrBlank()) {
            logger.info("No direct JDBC configuration found, converting from R2DBC datasource")
            
            if (r2dbcUsername.isNullOrBlank() || r2dbcPassword.isNullOrBlank()) {
                throw FlywayR2dbcConfigurationException(
                    """R2DBC configuration requires all connection properties:
                    |r2dbc:
                    |  datasources:
                    |    default:
                    |      url: r2dbc:postgresql://localhost:5432/mydb
                    |      username: myuser
                    |      password: mypassword""".trimMargin()
                )
            }
            
            val converter = converterRegistry.findConverter(r2dbcUrl)
            return converter.convert(r2dbcUrl, r2dbcUsername, r2dbcPassword)
        }
        
        // No configuration found
        throw FlywayR2dbcConfigurationException(
            """No database connection configuration found. Please provide either:
            |
            |1. Direct JDBC configuration:
            |   flyway-r2dbc:
            |     url: jdbc:postgresql://localhost:5432/mydb
            |     username: myuser
            |     password: mypassword
            |
            |2. R2DBC configuration (for automatic conversion):
            |   r2dbc:
            |     datasources:
            |       default:
            |         url: r2dbc:postgresql://localhost:5432/mydb
            |         username: myuser
            |         password: mypassword""".trimMargin()
        )
    }
}