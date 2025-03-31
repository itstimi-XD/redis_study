package com.hanghae.cinema.application.reservation.dto

data class ReservationRequest(
    val scheduleId: Long,
    val seatIds: List<Long>
) 