package com.hanghae.cinema.api.ratelimit

import com.hanghae.cinema.api.exception.RateLimitExceededException
import org.redisson.api.RateIntervalUnit
import org.redisson.api.RateType
import org.redisson.api.RedissonClient
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Component
@Profile("distributed")
class RedisRateLimiter(
    private val redissonClient: RedissonClient
) : RateLimiter {
    companion object {
        private const val MAX_REQUESTS_PER_MINUTE = 50L
        private const val BLOCK_DURATION_HOURS = 1L
        private const val BOOKING_COOLDOWN_MINUTES = 5L
    }

    override fun checkApiCallLimit(clientIp: String) {
        val blockedKey = "rate_limit:blocked:$clientIp"
        
        // Check if IP is blocked
        if (redissonClient.getBucket<Boolean>(blockedKey).isExists) {
            throw RateLimitExceededException("과도한 요청으로 인해 1시간 동안 차단되었습니다.")
        }
        
        val counterKey = "rate_limit:counter:$clientIp"
        val counter = redissonClient.getAtomicLong(counterKey)
        
        // Initialize counter with 1 minute expiry if it doesn't exist
        if (!counter.isExists) {
            counter.set(0)
            counter.expire(1, TimeUnit.MINUTES)
        }
        
        val count = counter.incrementAndGet()
        if (count > MAX_REQUESTS_PER_MINUTE) {
            // Block IP for 1 hour
            redissonClient.getBucket<Boolean>(blockedKey).set(true, BLOCK_DURATION_HOURS, TimeUnit.HOURS)
            throw RateLimitExceededException("1분 내 50회 이상 요청하여 1시간 동안 차단되었습니다.")
        }
    }

    override fun checkBookingLimit(clientIp: String, movieTimeId: String) {
        val key = "rate_limit:booking:$clientIp:$movieTimeId"
        
        val bucket = redissonClient.getBucket<Boolean>(key)
        if (bucket.isExists) {
            throw RateLimitExceededException("5분 이내에 같은 시간대의 영화를 예약할 수 없습니다.")
        }
        
        bucket.set(true, BOOKING_COOLDOWN_MINUTES, TimeUnit.MINUTES)
    }
} 