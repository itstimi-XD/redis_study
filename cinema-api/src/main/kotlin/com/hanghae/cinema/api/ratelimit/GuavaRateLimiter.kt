package com.hanghae.cinema.api.ratelimit

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.hanghae.cinema.api.exception.RateLimitExceededException
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@Component
@Profile("single")
class GuavaRateLimiter : RateLimiter {
    // API 호출 카운트를 위한 Cache (IP별로 관리, 1분 만료)
    private val apiCallCounts: LoadingCache<String, AtomicInteger> = CacheBuilder.newBuilder()
        .expireAfterWrite(1, TimeUnit.MINUTES)
        .build(CacheLoader.from { _ -> AtomicInteger(0) })

    // IP 차단을 위한 Cache (1시간 만료)
    private val blockedIps: LoadingCache<String, Boolean> = CacheBuilder.newBuilder()
        .expireAfterWrite(1, TimeUnit.HOURS)
        .build(CacheLoader.from { _ -> false })

    // 예약 재시도 제한을 위한 Cache (5분 만료)
    private val bookingCache: LoadingCache<String, Boolean> = CacheBuilder.newBuilder()
        .expireAfterWrite(5, TimeUnit.MINUTES)
        .build(CacheLoader.from { _ -> false })

    override fun checkApiCallLimit(clientIp: String) {
        // 차단된 IP인지 확인
        if (blockedIps.get(clientIp)) {
            throw RateLimitExceededException("과도한 요청으로 인해 1시간 동안 차단되었습니다.")
        }

        // 1분 내 요청 횟수 증가
        val count = apiCallCounts.get(clientIp).incrementAndGet()
        
        // 50회 초과시 IP 차단
        if (count > 50) {
            blockedIps.put(clientIp, true)
            throw RateLimitExceededException("1분 내 50회 이상 요청하여 1시간 동안 차단되었습니다.")
        }
    }

    override fun checkBookingLimit(clientIp: String, movieTimeId: String) {
        val cacheKey = "$clientIp:$movieTimeId"
        if (bookingCache.get(cacheKey)) {
            throw RateLimitExceededException("5분 이내에 같은 시간대의 영화를 예약할 수 없습니다.")
        }
        bookingCache.put(cacheKey, true)
    }
} 