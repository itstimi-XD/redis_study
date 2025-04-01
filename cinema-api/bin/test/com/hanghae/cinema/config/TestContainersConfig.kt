package com.hanghae.cinema.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.context.annotation.Bean
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.utility.DockerImageName

@TestConfiguration
class TestContainersConfig {

    companion object {
        val mysqlContainer: MySQLContainer<*> = MySQLContainer(DockerImageName.parse("mysql:8.0"))
            .withDatabaseName("cinema")
            .withUsername("cinema")
            .withPassword("cinema")
            .apply { start() }
    }

    @Bean
    @ServiceConnection
    fun mysqlContainer() = mysqlContainer
} 