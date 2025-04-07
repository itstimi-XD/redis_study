package com.hanghae.cinema.api.ratelimit

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.hanghae.cinema.api.exception.RateLimitExceededException
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Function

@Component
@Profile("single")
class GuavaRateLimiter : RateLimiter {
    companion object {
        private const val MAX_REQUESTS_PER_MINUTE = 50L
        private const val BLOCK_DURATION_HOURS = 1L
        private const val BOOKING_COOLDOWN_MINUTES = 5L
    }

    private val apiCallCounter: LoadingCache<String, AtomicInteger> = CacheBuilder.newBuilder()
        .expireAfterWrite(1, TimeUnit.MINUTES)
        .build(object : CacheLoader<String, AtomicInteger>() {
            override fun load(key: String): AtomicInteger = AtomicInteger(0)
        })

    private val apiBlockList: LoadingCache<String, Boolean> = CacheBuilder.newBuilder()
        .expireAfterWrite(1, TimeUnit.HOURS)
        .build(object : CacheLoader<String, Boolean>() {
            override fun load(key: String): Boolean = false
        })

    private val bookingLimiter: LoadingCache<String, Boolean> = CacheBuilder.newBuilder()
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .build(object : CacheLoader<String, Boolean>() {
            override fun load(key: String): Boolean = false
        })

    override fun checkApiRateLimit(clientIp: String): Boolean {
        if (apiBlockList.get(clientIp)) {
            return false
        }

        val counter = apiCallCounter.get(clientIp)
        val currentCount = counter.incrementAndGet()

        if (currentCount > MAX_REQUESTS_PER_MINUTE) {
            apiBlockList.put(clientIp, true)
            return false
        }

        return true
    }

    override fun checkBookingRateLimit(clientIp: String, scheduleId: Long): Boolean {
        val key = "$clientIp:$scheduleId"
        return !bookingLimiter.get(key)
    }

    override fun isBlocked(ip: String): Boolean {
        return apiBlockList.get(ip)
    }
} 