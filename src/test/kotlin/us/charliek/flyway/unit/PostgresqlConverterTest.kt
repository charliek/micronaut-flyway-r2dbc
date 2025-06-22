package us.charliek.flyway.unit

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import us.charliek.flyway.converter.PostgresqlConverter

class PostgresqlConverterTest {

    private val converter = PostgresqlConverter()

    @Test
    fun `should support PostgreSQL R2DBC URLs`() {
        assertThat(converter.supports("r2dbc:postgresql://localhost:5432/test")).isTrue()
        assertThat(converter.supports("r2dbc:mysql://localhost:3306/test")).isFalse()
        assertThat(converter.supports("jdbc:postgresql://localhost:5432/test")).isFalse()
    }

    @Test
    fun `should convert basic R2DBC URL to JDBC`() {
        val result = converter.convert(
            "r2dbc:postgresql://localhost:5432/testdb",
            "user",
            "pass"
        )

        assertThat(result.url).isEqualTo("jdbc:postgresql://localhost:5432/testdb")
        assertThat(result.username).isEqualTo("user")
        assertThat(result.password).isEqualTo("pass")
    }

    @Test
    fun `should convert R2DBC URL with supported options`() {
        val result = converter.convert(
            "r2dbc:postgresql://localhost:5432/testdb?currentSchema=app&sslmode=require",
            "user",
            "pass"
        )

        assertThat(result.url).isEqualTo("jdbc:postgresql://localhost:5432/testdb?currentSchema=app&sslmode=require")
    }

    @Test
    fun `should handle R2DBC URL without database name`() {
        val result = converter.convert(
            "r2dbc:postgresql://localhost:5432",
            "user",
            "pass"
        )

        assertThat(result.url).isEqualTo("jdbc:postgresql://localhost:5432")
    }

    @Test
    fun `should handle R2DBC URL with port and no database`() {
        val result = converter.convert(
            "r2dbc:postgresql://db.example.com:5432/",
            "user",
            "pass"
        )

        assertThat(result.url).isEqualTo("jdbc:postgresql://db.example.com:5432/")
    }

    @Test
    fun `should filter out unsupported options`() {
        val result = converter.convert(
            "r2dbc:postgresql://localhost:5432/testdb?unsupported=value&currentSchema=app",
            "user",
            "pass"
        )

        assertThat(result.url).isEqualTo("jdbc:postgresql://localhost:5432/testdb?currentSchema=app")
    }

    @Test
    fun `should handle empty option values`() {
        val result = converter.convert(
            "r2dbc:postgresql://localhost:5432/testdb?currentSchema=&sslmode=disable",
            "user",
            "pass"
        )

        assertThat(result.url).isEqualTo("jdbc:postgresql://localhost:5432/testdb?currentSchema=&sslmode=disable")
    }

    @Test
    fun `should handle malformed options gracefully`() {
        val result = converter.convert(
            "r2dbc:postgresql://localhost:5432/testdb?&currentSchema=app&&",
            "user",
            "pass"
        )

        assertThat(result.url).isEqualTo("jdbc:postgresql://localhost:5432/testdb?currentSchema=app")
    }
}