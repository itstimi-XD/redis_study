package com.hanghae.cinema.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.utility.DockerImageName

@TestConfiguration
class TestContainersConfig {

    companion object {
        val postgresContainer: PostgreSQLContainer<*> = PostgreSQLContainer(DockerImageName.parse("postgres:15-alpine"))
            .withDatabaseName("cinema")
            .withUsername("postgres")
            .withPassword("postgres")
            .apply { start() }
    }

    @Bean
    @ServiceConnection
    fun postgresContainer() = postgresContainer
} 