package us.charliek.flyway.converter

import jakarta.inject.Singleton
import org.slf4j.LoggerFactory

@Singleton
class PostgresqlConverter : R2dbcToJdbcConverter {
    
    companion object {
        private val logger = LoggerFactory.getLogger(PostgresqlConverter::class.java)
        private val POSTGRES_R2DBC_PATTERN = Regex("r2dbc:postgresql://([^/]+)(/.+)?")
        private val SUPPORTED_OPTIONS = setOf("currentSchema", "sslmode", "sslcert", "sslkey", "sslrootcert")
    }
    
    override fun supports(r2dbcUrl: String): Boolean {
        return r2dbcUrl.startsWith("r2dbc:postgresql://")
    }
    
    override fun convert(r2dbcUrl: String, username: String, password: String): JdbcConnectionInfo {
        val jdbcUrl = convertUrl(r2dbcUrl)
        logger.debug("Converted R2DBC URL '{}' to JDBC URL '{}'", r2dbcUrl, jdbcUrl)
        return JdbcConnectionInfo(jdbcUrl, username, password)
    }
    
    private fun convertUrl(r2dbcUrl: String): String {
        // Convert r2dbc:postgresql://host:port/database?options to jdbc:postgresql://host:port/database?options
        val baseJdbcUrl = r2dbcUrl.replace("r2dbc:postgresql://", "jdbc:postgresql://")
        
        return if (baseJdbcUrl.contains("?")) {
            val (baseUrl, optionsString) = baseJdbcUrl.split("?", limit = 2)
            val convertedOptions = convertConnectionOptions(optionsString)
            if (convertedOptions.isNotEmpty()) {
                "$baseUrl?$convertedOptions"
            } else {
                baseUrl
            }
        } else {
            baseJdbcUrl
        }
    }
    
    private fun convertConnectionOptions(optionsString: String): String {
        val options = optionsString.split("&").mapNotNull { option ->
            if (option.isBlank()) return@mapNotNull null
            
            val parts = option.split("=", limit = 2)
            if (parts.isEmpty()) return@mapNotNull null
            
            val key = parts[0]
            val value = parts.getOrNull(1) ?: ""
            
            when {
                key in SUPPORTED_OPTIONS -> option // Pass through supported options
                key.startsWith("ssl") -> {
                    logger.debug("SSL option '{}' passed through to JDBC", option)
                    option
                }
                else -> {
                    logger.warn("R2DBC option '{}={}' is not supported in JDBC conversion and will be ignored", key, value)
                    null
                }
            }
        }
        return options.joinToString("&")
    }
}