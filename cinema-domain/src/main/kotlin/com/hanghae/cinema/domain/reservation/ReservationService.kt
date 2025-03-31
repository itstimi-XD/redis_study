package com.hanghae.cinema.domain.reservation

import com.hanghae.cinema.domain.schedule.ScheduleRepository
import com.hanghae.cinema.domain.seat.Seat
import com.hanghae.cinema.domain.seat.SeatRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ReservationService(
    private val reservationRepository: ReservationRepository,
    private val scheduleRepository: ScheduleRepository,
    private val seatRepository: SeatRepository
) {
    @Transactional
    fun reserve(scheduleId: Long, seatIds: List<Long>, userId: String): List<Reservation> {
        // 1. 좌석 개수 검증
        require(seatIds.size <= 5) { "한 번에 최대 5개의 좌석만 예약할 수 있습니다." }

        // 2. 스케줄 조회
        val schedule = scheduleRepository.findById(scheduleId)
            ?: throw IllegalArgumentException("존재하지 않는 상영 스케줄입니다.")

        // 3. 좌석 조회 및 연속성 검증
        val seats = seatRepository.findAllById(seatIds)
        require(seats.size == seatIds.size) { "존재하지 않는 좌석이 포함되어 있습니다." }
        validateSeatsAreConsecutive(seats)

        // 4. 이미 예약된 좌석인지 확인
        val existingReservations = reservationRepository.findAllByScheduleIdAndSeatIdIn(scheduleId, seatIds)
        require(existingReservations.isEmpty()) { "이미 예약된 좌석이 포함되어 있습니다." }

        // 5. 사용자의 기존 예약 수 확인
        val userReservations = reservationRepository.findByScheduleIdAndUserId(scheduleId, userId)
        require(userReservations.size + seatIds.size <= 5) { "한 상영 스케줄당 최대 5개의 좌석만 예약할 수 있습니다." }

        // 6. 예약 생성
        return seats.map { seat ->
            val reservation = Reservation(
                schedule = schedule,
                seat = seat,
                userId = userId,
                reservedAt = LocalDateTime.now()
            )
            reservationRepository.save(reservation)
        }
    }

    private fun validateSeatsAreConsecutive(seats: List<Seat>) {
        // 같은 행에 있는지 확인
        val rows = seats.map { it.seatRow }.distinct()
        require(rows.size == 1) { "연속된 좌석만 예약할 수 있습니다." }

        // 열 번호가 연속적인지 확인
        val columns = seats.map { it.column }.sorted()
        for (i in 0 until columns.size - 1) {
            require(columns[i + 1] - columns[i] == 1) { "연속된 좌석만 예약할 수 있습니다." }
        }
    }
} 