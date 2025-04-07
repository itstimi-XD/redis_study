package com.hanghae.cinema.api.ratelimit

import com.hanghae.cinema.api.exception.RateLimitExceededException
import org.redisson.api.RAtomicLong
import org.redisson.api.RBucket
import org.redisson.api.RedissonClient
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.script.RedisScript
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

@Component
@Profile("distributed")
class RedisRateLimiter(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val redissonClient: RedissonClient
) : RateLimiter {

    private val apiRateLimitScript = """
        local key = KEYS[1]
        local maxRequests = tonumber(ARGV[1])
        local windowSeconds = tonumber(ARGV[2])
        local currentCount = redis.call('get', key)
        
        if currentCount then
            if tonumber(currentCount) >= maxRequests then
                return tonumber(currentCount)
            end
        end
        
        redis.call('incr', key)
        if not currentCount then
            redis.call('expire', key, windowSeconds)
        end
        
        return tonumber(redis.call('get', key))
    """

    private val bookingRateLimitScript = """
        local key = KEYS[1]
        local windowSeconds = tonumber(ARGV[1])
        local exists = redis.call('exists', key)
        
        if exists == 1 then
            return 0
        end
        
        redis.call('set', key, 1)
        redis.call('expire', key, windowSeconds)
        return 1
    """

    override fun checkApiRateLimit(ip: String): Boolean {
        val key = "rate:api:$ip"
        val result = redisTemplate.execute(
            RedisScript.of(apiRateLimitScript, Long::class.java),
            listOf(key),
            50, // 최대 요청 수
            60  // 윈도우 시간(초)
        ) ?: 0

        if (result > 50) {
            val blockKey = "block:$ip"
            redissonClient.getBucket<Boolean>(blockKey).set(true, 1, TimeUnit.HOURS)
            throw RateLimitExceededException("API 호출 한도를 초과했습니다. 1시간 동안 차단됩니다.")
        }

        return true
    }

    override fun checkBookingRateLimit(ip: String, scheduleId: Long): Boolean {
        val key = "rate:booking:$ip:$scheduleId"
        val result = redisTemplate.execute(
            RedisScript.of(bookingRateLimitScript, Long::class.java),
            listOf(key),
            300  // 5분(초)
        ) ?: 0

        if (result == 0L) {
            throw RateLimitExceededException("동일 시간대 영화는 5분 내 재예약이 불가능합니다.")
        }

        return true
    }

    override fun isBlocked(ip: String): Boolean {
        val blockKey = "block:$ip"
        return redissonClient.getBucket<Boolean>(blockKey).isExists
    }
} 