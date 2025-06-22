package us.charliek.flyway.configuration

import io.micronaut.context.annotation.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("flyway-r2dbc")
class FlywayR2dbcConfigurationProperties {
    var enabled: Boolean = false
    var url: String? = null
    var username: String? = null
    var password: String? = null
    var locations: List<String> = listOf("classpath:db/migration")
    var baselineOnMigrate: Boolean = false
    var validateOnMigrate: Boolean = true
    var cleanDisabled: Boolean = true
    var baselineVersion: String = "1"
    var baselineDescription: String = "Initial version"
    var placeholderReplacement: Boolean = false
    var placeholders: Map<String, String> = emptyMap()
    var connectionRetries: Int = 3
    var connectionRetryDelay: Duration = Duration.ofSeconds(1)
}