package com.hanghae.cinema.api.config

import com.github.benmanes.caffeine.cache.Caffeine
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.caffeine.CaffeineCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.util.concurrent.TimeUnit

@Configuration
@EnableCaching
@Profile("local-cache")
class CaffeineCacheConfig {
    
    @Bean
    fun cacheManager(): CacheManager {
        val caffeineBuilder = Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .recordStats()
            
        return CaffeineCacheManager().apply {
            setCaffeine(caffeineBuilder)
            setCacheNames(listOf("nowPlayingMovies"))
        }
    }
}