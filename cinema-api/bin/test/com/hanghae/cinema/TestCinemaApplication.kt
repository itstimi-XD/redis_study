package com.hanghae.cinema

import com.hanghae.cinema.config.TestContainersConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@SpringBootApplication
@Import(TestContainersConfig::class)
class TestCinemaApplication

fun main(args: Array<String>) {
    runApplication<TestCinemaApplication>(*args) {
        setAdditionalProfiles("test")
    }
} 