package com.hanghae.cinema.application.message

import com.hanghae.cinema.domain.message.MessageService
import com.hanghae.cinema.domain.reservation.Reservation
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service

@Service
@Primary
class TestMessageService : MessageService {
    private val sentMessages = mutableMapOf<String, String>()

    override fun send(userId: String, message: String) {
        sentMessages[userId] = message
    }

    override fun sendReservationComplete(reservations: List<Reservation>, userId: String) {
        val firstReservation = reservations.first()
        val message = "[예약 완료] ${firstReservation.schedule.movie.title} " +
                "(${firstReservation.schedule.theater.name}) " +
                "${firstReservation.schedule.startTime}에 " +
                "${reservations.size}개의 좌석이 예약되었습니다."
        send(userId, message)
    }

    fun getLastMessageFor(userId: String): String? = sentMessages[userId]

    fun clearMessages() {
        sentMessages.clear()
    }
}