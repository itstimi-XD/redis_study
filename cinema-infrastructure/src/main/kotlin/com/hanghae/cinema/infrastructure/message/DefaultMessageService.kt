package com.hanghae.cinema.infrastructure.message

import com.hanghae.cinema.domain.message.MessageService
import com.hanghae.cinema.domain.reservation.Reservation
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class DefaultMessageService : MessageService {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun send(userId: String, message: String) {
        logger.info("Sending message to user $userId: $message")
        Thread.sleep(500) // Simulating external service call
    }

    override fun sendReservationComplete(reservations: List<Reservation>, userId: String) {
        val firstReservation = reservations.first()
        val message = "[예약 완료] ${firstReservation.schedule.movie.title} " +
                "(${firstReservation.schedule.theater.name}) " +
                "${firstReservation.schedule.startTime}에 " +
                "${reservations.size}개의 좌석이 예약되었습니다."
        send(userId, message)
    }
} 