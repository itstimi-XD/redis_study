package com.hanghae.cinema.application.config

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import com.hanghae.cinema.application.config.ApplicationConfig
import org.springframework.boot.autoconfigure.domain.EntityScan

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
class TestConfig 