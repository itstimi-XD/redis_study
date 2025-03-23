package com.hanghae.cinema.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.utility.DockerImageName
import javax.sql.DataSource

@Configuration
@Profile("test")
class DatabaseConfig {

    companion object {
        private val mysqlContainer: MySQLContainer<*> = MySQLContainer(DockerImageName.parse("mysql:8.0"))
            .withDatabaseName("cinema")
            .withUsername("cinema")
            .withPassword("cinema")
            .apply { start() }
    }

    @Bean
    @Primary
    fun dataSource(): DataSource {
        return DataSourceBuilder.create()
            .url(mysqlContainer.jdbcUrl)
            .username(mysqlContainer.username)
            .password(mysqlContainer.password)
            .driverClassName("com.mysql.cj.jdbc.Driver")
            .build()
    }
} 