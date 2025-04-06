package com.hanghae.cinema.api.config

import org.redisson.api.RedissonClient
import org.springframework.context.annotation.Configuration

@Configuration
class RateLimitConfig(
    private val redissonClient: RedissonClient
) {
    companion object {
        const val MAX_REQUESTS_PER_MINUTE = 50L
        const val BLOCK_DURATION_HOURS = 1L
        const val BOOKING_COOLDOWN_MINUTES = 5L
    }
} 