package com.hanghae.cinema.application.reservation.dto

import com.hanghae.cinema.domain.reservation.Reservation
import java.time.LocalDateTime

data class ReservationResponse(
    val id: Long,
    val scheduleId: Long,
    val movieTitle: String,
    val theaterName: String,
    val seatNumber: String,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val reservedAt: LocalDateTime
) {
    companion object {
        fun from(reservation: Reservation): ReservationResponse {
            return ReservationResponse(
                id = reservation.id!!,
                scheduleId = reservation.schedule.id!!,
                movieTitle = reservation.schedule.movie.title,
                theaterName = reservation.schedule.theater.name,
                seatNumber = reservation.seat.seatNumber,
                startTime = reservation.schedule.startTime,
                endTime = reservation.schedule.endTime,
                reservedAt = reservation.reservedAt
            )
        }
    }
} 