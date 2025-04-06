package com.hanghae.cinema.api.ratelimit

interface RateLimiter {
    fun checkApiCallLimit(clientIp: String)
    fun checkBookingLimit(clientIp: String, movieTimeId: String)
} 