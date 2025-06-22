package us.charliek.flyway.converter

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import javax.sql.DataSource

interface R2dbcToJdbcConverter {
    fun supports(r2dbcUrl: String): Boolean
    fun convert(r2dbcUrl: String, username: String, password: String): JdbcConnectionInfo
}

data class JdbcConnectionInfo(
    val url: String,
    val username: String,
    val password: String
) {
    fun toDataSource(): DataSource {
        val config = HikariConfig().apply {
            jdbcUrl = url
            this.username = this@JdbcConnectionInfo.username
            this.password = this@JdbcConnectionInfo.password
            maximumPoolSize = 2 // Flyway needs at least 2 connections
            minimumIdle = 0
            connectionTimeout = 30000
            idleTimeout = 10000 // 10 seconds idle timeout
            maxLifetime = 60000 // 1 minute max lifetime
            leakDetectionThreshold = 0 // Disable leak detection
        }
        return HikariDataSource(config)
    }
}