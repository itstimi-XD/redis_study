package com.hanghae.cinema.domain.message

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class MessageService {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun send(userId: String, message: String) {
        logger.info("Sending message to user $userId: $message")
        Thread.sleep(500) // Simulating external service call
    }
} 