package com.hanghae.cinema.application.config

import com.hanghae.cinema.application.reservation.ReservationFacade
import com.hanghae.cinema.domain.message.MessageService
import com.hanghae.cinema.domain.message.TestMessageService
import com.hanghae.cinema.domain.reservation.ReservationService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Bean

@TestConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = [
    "com.hanghae.cinema.application.reservation",
    "com.hanghae.cinema.domain.reservation",
    "com.hanghae.cinema.domain.message",
    "com.hanghae.cinema.infrastructure"
])
@Import(ApplicationConfig::class)
@EntityScan(basePackages = ["com.hanghae.cinema.domain", "com.hanghae.cinema.infrastructure"])
class TestConfig {
    @Bean
    fun messageService(): MessageService {
        return TestMessageService()
    }
}
