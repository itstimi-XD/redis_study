//package com.hanghae.cinema.domain.reservation
//
//import com.hanghae.cinema.domain.schedule.Schedule
//import com.hanghae.cinema.domain.schedule.ScheduleRepository
//import com.hanghae.cinema.domain.seat.Seat
//import com.hanghae.cinema.domain.seat.SeatRepository
//import com.hanghae.cinema.domain.movie.Movie
//import com.hanghae.cinema.domain.theater.Theater
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Test
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
//import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
//import org.springframework.context.annotation.Import
//import org.springframework.test.context.ActiveProfiles
//import org.testcontainers.containers.MySQLContainer
//import org.testcontainers.junit.jupiter.Container
//import org.testcontainers.junit.jupiter.Testcontainers
//import java.time.LocalDateTime
//import org.junit.jupiter.api.Assertions.*
//
//@DataJpaTest
//@Testcontainers
//@ActiveProfiles("test")
//@Import(ReservationService::class)
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//class ReservationServiceTest @Autowired constructor(
//    private val reservationService: ReservationService,
//    private val scheduleRepository: ScheduleRepository,
//    private val seatRepository: SeatRepository,
//    private val reservationRepository: ReservationRepository
//) {
//    companion object {
//        @Container
//        val mysqlContainer = MySQLContainer<Nothing>("mysql:8.0.32").apply {
//            withDatabaseName("testdb")
//            withUsername("test")
//            withPassword("test")
//            withInitScript("schema.sql")
//        }
//    }
//
//    private lateinit var schedule: Schedule
//    private lateinit var seats: List<Seat>
//    private lateinit var movie: Movie
//    private lateinit var theater: Theater
//
//    @BeforeEach
//    fun setUp() {
//        // 테스트 데이터 초기화
//        movie = Movie(
//            title = "테스트 영화",
//            duration = 120
//        )
//
//        theater = Theater(
//            name = "테스트 극장",
//            totalSeats = 100
//        )
//
//        schedule = scheduleRepository.save(
//            Schedule(
//                movie = movie,
//                theater = theater,
//                startTime = LocalDateTime.now().plusDays(1),
//                endTime = LocalDateTime.now().plusDays(1).plusHours(2)
//            )
//        )
//
//        // A1 ~ A5 좌석 생성
//        seats = (1..5).map { number ->
//            seatRepository.save(
//                Seat(
//                    theater = theater,
//                    seatRow = "A",
//                    seatNumber = number
//                )
//            )
//        }
//    }
//
//    @Test
//    fun `연속된 좌석 예약 성공 테스트`() {
//        // given
//        val userId = "user-1"
//        val seatIds = seats.take(3).map { it.id!! }
//
//        // when
//        val reservations = reservationService.reserve(
//            scheduleId = schedule.id!!,
//            seatIds = seatIds,
//            userId = userId
//        )
//
//        // then
//        assertEquals(3, reservations.size)
//        assertEquals(userId, reservations.first().userId)
//        assertTrue(reservations.all { it.scheduleId == schedule.id })
//
//        // verify reservation in database
//        val savedReservations = reservationRepository.findByScheduleIdAndUserId(schedule.id!!, userId)
//        assertEquals(3, savedReservations.size)
//    }
//
//    @Test
//    fun `연속되지 않은 좌석 예약 실패 테스트`() {
//        // given
//        val userId = "user-1"
//        val nonConsecutiveSeats = listOf(seats[0].id!!, seats[2].id!!, seats[4].id!!)
//
//        // when & then
//        val exception = assertThrows(IllegalArgumentException::class.java) {
//            reservationService.reserve(
//                scheduleId = schedule.id!!,
//                seatIds = nonConsecutiveSeats,
//                userId = userId
//            )
//        }
//
//        assertEquals("연속된 좌석만 예약할 수 있습니다.", exception.message)
//    }
//
//    @Test
//    fun `한 사용자의 동일 스케줄 최대 좌석 수 초과 예약 실패 테스트`() {
//        // given
//        val userId = "user-1"
//        val firstReservationSeats = seats.take(3).map { it.id!! }
//        val secondReservationSeats = seats.takeLast(3).map { it.id!! }
//
//        // when
//        reservationService.reserve(
//            scheduleId = schedule.id!!,
//            seatIds = firstReservationSeats,
//            userId = userId
//        )
//
//        // then
//        val exception = assertThrows(IllegalArgumentException::class.java) {
//            reservationService.reserve(
//                scheduleId = schedule.id!!,
//                seatIds = secondReservationSeats,
//                userId = userId
//            )
//        }
//
//        assertEquals("한 상영 스케줄당 최대 5개의 좌석만 예약할 수 있습니다.", exception.message)
//    }
//
//    @Test
//    fun `이미 예약된 좌석 예약 실패 테스트`() {
//        // given
//        val firstUserId = "user-1"
//        val secondUserId = "user-2"
//        val seatIds = seats.take(2).map { it.id!! }
//
//        // when
//        reservationService.reserve(
//            scheduleId = schedule.id!!,
//            seatIds = seatIds,
//            userId = firstUserId
//        )
//
//        // then
//        val exception = assertThrows(IllegalArgumentException::class.java) {
//            reservationService.reserve(
//                scheduleId = schedule.id!!,
//                seatIds = seatIds,
//                userId = secondUserId
//            )
//        }
//
//        assertEquals("이미 예약된 좌석이 포함되어 있습니다.", exception.message)
//    }
//}