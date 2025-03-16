package com.hanghae.cinema.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName
import javax.sql.DataSource

@Configuration
@Profile("test")
class DatabaseConfig {

    companion object {
        private val postgresContainer: PostgreSQLContainer<*> = PostgreSQLContainer(DockerImageName.parse("postgres:15-alpine"))
            .withDatabaseName("cinema")
            .withUsername("postgres")
            .withPassword("postgres")
            .apply { start() }
    }

    @Bean
    @Primary
    fun dataSource(): DataSource {
        return DataSourceBuilder.create()
            .url(postgresContainer.jdbcUrl)
            .username(postgresContainer.username)
            .password(postgresContainer.password)
            .driverClassName("org.postgresql.Driver")
            .build()
    }
} 