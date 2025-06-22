package us.charliek.flyway.unit

import io.micronaut.context.ApplicationContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import us.charliek.flyway.configuration.FlywayR2dbcConfigurationProperties
import java.time.Duration

class ConfigurationPropertiesTest {

    @Test
    fun `should load default configuration values`() {
        val context = ApplicationContext.run()
        val config = context.getBean(FlywayR2dbcConfigurationProperties::class.java)
        
        assertThat(config.enabled).isFalse()
        assertThat(config.locations).containsExactly("classpath:db/migration")
        assertThat(config.baselineOnMigrate).isFalse()
        assertThat(config.validateOnMigrate).isTrue()
        assertThat(config.cleanDisabled).isTrue()
        assertThat(config.baselineVersion).isEqualTo("1")
        assertThat(config.baselineDescription).isEqualTo("Initial version")
        assertThat(config.placeholderReplacement).isFalse()
        assertThat(config.placeholders).isEmpty()
        assertThat(config.connectionRetries).isEqualTo(3)
        assertThat(config.connectionRetryDelay).isEqualTo(Duration.ofSeconds(1))
        
        context.close()
    }

    @Test
    fun `should load custom configuration values`() {
        val context = ApplicationContext.run(
            mapOf(
                "flyway-r2dbc.enabled" to false,
                "flyway-r2dbc.url" to "jdbc:postgresql://localhost:5432/test",
                "flyway-r2dbc.username" to "testuser",
                "flyway-r2dbc.password" to "testpass",
                "flyway-r2dbc.locations" to listOf("classpath:db/migration", "classpath:db/data"),
                "flyway-r2dbc.baseline-on-migrate" to true,
                "flyway-r2dbc.validate-on-migrate" to false,
                "flyway-r2dbc.clean-disabled" to false,
                "flyway-r2dbc.baseline-version" to "0",
                "flyway-r2dbc.baseline-description" to "Custom baseline",
                "flyway-r2dbc.placeholder-replacement" to true,
                "flyway-r2dbc.placeholders.env" to "test",
                "flyway-r2dbc.connection-retries" to 5,
                "flyway-r2dbc.connection-retry-delay" to "PT2S"
            )
        )
        
        val config = context.getBean(FlywayR2dbcConfigurationProperties::class.java)
        
        assertThat(config.enabled).isFalse()
        assertThat(config.url).isEqualTo("jdbc:postgresql://localhost:5432/test")
        assertThat(config.username).isEqualTo("testuser")
        assertThat(config.password).isEqualTo("testpass")
        assertThat(config.locations).containsExactly("classpath:db/migration", "classpath:db/data")
        assertThat(config.baselineOnMigrate).isTrue()
        assertThat(config.validateOnMigrate).isFalse()
        assertThat(config.cleanDisabled).isFalse()
        assertThat(config.baselineVersion).isEqualTo("0")
        assertThat(config.baselineDescription).isEqualTo("Custom baseline")
        assertThat(config.placeholderReplacement).isTrue()
        assertThat(config.placeholders).containsEntry("env", "test")
        assertThat(config.connectionRetries).isEqualTo(5)
        assertThat(config.connectionRetryDelay).isEqualTo(Duration.ofSeconds(2))
        
        context.close()
    }
}