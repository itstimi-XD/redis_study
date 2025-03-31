package com.hanghae.cinema.application.reservation

import com.hanghae.cinema.application.reservation.dto.ReservationRequest
import com.hanghae.cinema.application.reservation.dto.ReservationResponse
import com.hanghae.cinema.domain.message.MessageService
import com.hanghae.cinema.domain.reservation.ReservationService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ReservationFacade(
    private val reservationService: ReservationService,
    private val messageService: MessageService
) {
    @Transactional
    fun reserve(request: ReservationRequest, userId: String): List<ReservationResponse> {
        // 1. 예약 처리
        val reservations = reservationService.reserve(
            scheduleId = request.scheduleId,
            seatIds = request.seatIds,
            userId = userId
        )

        // 2. 예약 완료 메시지 발송
        val firstReservation = reservations.first()
        val message = buildReservationMessage(
            movieTitle = firstReservation.schedule.movie.title,
            theaterName = firstReservation.schedule.theater.name,
            startTime = firstReservation.schedule.startTime,
            seatCount = reservations.size
        )
        messageService.send(userId, message)

        // 3. 응답 변환 및 반환
        return reservations.map { ReservationResponse.from(it) }
    }

    private fun buildReservationMessage(
        movieTitle: String,
        theaterName: String,
        startTime: java.time.LocalDateTime,
        seatCount: Int
    ): String {
        return "[예약 완료] $movieTitle ($theaterName) ${startTime.toLocalTime()}에 $seatCount 개의 좌석이 예약되었습니다."
    }
}