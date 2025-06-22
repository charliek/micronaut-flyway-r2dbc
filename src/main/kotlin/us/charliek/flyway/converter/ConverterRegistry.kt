package us.charliek.flyway.converter

import jakarta.inject.Singleton
import us.charliek.flyway.exception.UnsupportedDatabaseException

@Singleton
class ConverterRegistry(
    private val converters: List<R2dbcToJdbcConverter>
) {
    fun findConverter(r2dbcUrl: String): R2dbcToJdbcConverter {
        return converters.find { it.supports(r2dbcUrl) }
            ?: throw UnsupportedDatabaseException(extractDatabase(r2dbcUrl))
    }
    
    private fun extractDatabase(r2dbcUrl: String): String {
        // Extract database name from r2dbc URL pattern: r2dbc:database://...
        val pattern = Regex("r2dbc:([^:]+)://")
        val match = pattern.find(r2dbcUrl)
        return match?.groups?.get(1)?.value ?: "unknown"
    }
}