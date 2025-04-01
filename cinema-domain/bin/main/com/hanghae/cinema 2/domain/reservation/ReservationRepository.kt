package com.hanghae.cinema.domain.reservation

interface ReservationRepository {
    fun save(reservation: Reservation): Reservation
    fun findByScheduleIdAndSeatId(scheduleId: Long, seatId: Long): Reservation?
    fun findByScheduleIdAndUserId(scheduleId: Long, userId: String): List<Reservation>
    fun findByScheduleId(scheduleId: Long): List<Reservation>
    fun findAllByScheduleIdAndSeatIdIn(scheduleId: Long, seatIds: List<Long>): List<Reservation>
} 