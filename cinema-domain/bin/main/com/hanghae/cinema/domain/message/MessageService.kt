package com.hanghae.cinema.domain.message

import com.hanghae.cinema.domain.reservation.Reservation

interface MessageService {
    fun send(userId: String, message: String)
    fun sendReservationComplete(reservations: List<Reservation>, userId: String)
} 