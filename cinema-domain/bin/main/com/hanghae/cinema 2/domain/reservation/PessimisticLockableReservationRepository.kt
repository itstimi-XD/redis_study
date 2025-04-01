package com.hanghae.cinema.domain.reservation

interface PessimisticLockableReservationRepository {
    fun findAllByScheduleIdAndSeatIdInWithPessimisticLock(scheduleId: Long, seatIds: List<Long>): List<Reservation>
} 