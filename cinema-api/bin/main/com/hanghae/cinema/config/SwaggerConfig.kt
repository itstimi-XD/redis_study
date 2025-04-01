package com.hanghae.cinema.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.servers.Server
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    @Bean
    fun openAPI(@Value("\${spring.application.name}") appName: String): OpenAPI {
        return OpenAPI()
            .components(Components())
            .info(
                Info()
                    .title("$appName API Documentation")
                    .description("API documentation for Cinema Application")
                    .version("v1.0.0")
            )
            .addServersItem(Server().url("/").description("Default Server URL"))
    }
} 