package com.hanghae.cinema

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class CinemaApplicationTests {

    @Test
    fun contextLoads() {
        // This test will verify that the application context loads successfully with TestContainers
    }
} 