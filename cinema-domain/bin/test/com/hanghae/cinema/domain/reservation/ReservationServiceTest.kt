package com.hanghae.cinema.domain.reservation

import com.hanghae.cinema.domain.schedule.Schedule
import com.hanghae.cinema.domain.schedule.ScheduleRepository
import com.hanghae.cinema.domain.seat.Seat
import com.hanghae.cinema.domain.seat.SeatRepository
import com.hanghae.cinema.domain.config.CinemaDomainConfig
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.test.assertFailsWith
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase

@SpringBootTest(
    classes = [CinemaDomainConfig::class],
    properties = [
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect",
        "spring.jpa.show-sql=true",
        "spring.jpa.properties.hibernate.format_sql=true",
        "spring.sql.init.mode=always",
        "spring.sql.init.schema-locations=classpath:schema.sql",
        "spring.jpa.defer-datasource-initialization=true"
    ]
)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EntityScan(basePackages = ["com.hanghae.cinema"])
@ActiveProfiles("test")
@Testcontainers
class ReservationServiceTest @Autowired constructor(
    private val reservationService: ReservationService,
    private val scheduleRepository: ScheduleRepository,
    private val seatRepository: SeatRepository,
    private val reservationRepository: ReservationRepository
) {
    companion object {
        @Container
        private val mysqlContainer = MySQLContainer<Nothing>("mysql:8.0.32").apply {
            withDatabaseName("test")
            withUsername("test")
            withPassword("test")
            withReuse(true)
            withInitScript("schema.sql")
            withUrlParam("useSSL", "false")
            withUrlParam("allowPublicKeyRetrieval", "true")
            start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { mysqlContainer.getJdbcUrl() }
            registry.add("spring.datasource.username") { mysqlContainer.getUsername() }
            registry.add("spring.datasource.password") { mysqlContainer.getPassword() }
            registry.add("spring.datasource.driver-class-name") { mysqlContainer.getDriverClassName() }
        }
    }

    private lateinit var schedule: Schedule
    private lateinit var seats: List<Seat>

    @BeforeEach
    fun setUp() {
        // schema.sql에서 생성된 테스트 데이터 활용
        schedule = scheduleRepository.findById(1L)
            ?: throw IllegalStateException("Schedule not found")
        seats = seatRepository.findAllById(listOf(1L, 2L, 3L))
    }

    @Test
    fun `동시에 같은 좌석을 예약하면 하나만 성공해야 한다`() {
        // given
        val threadCount = 10
        val executorService = Executors.newFixedThreadPool(32)
        val latch = CountDownLatch(threadCount)
        val successCount = java.util.concurrent.atomic.AtomicInteger()
        val failCount = java.util.concurrent.atomic.AtomicInteger()

        // when
        repeat(threadCount) { index ->
            executorService.submit {
                try {
                    reservationService.reserve(
                        scheduleId = schedule.id!!,
                        seatIds = seats.map { it.id!! },
                        userId = "user-$index"
                    )
                    successCount.incrementAndGet()
                } catch (e: Exception) {
                    failCount.incrementAndGet()
                } finally {
                    latch.countDown()
                }
            }
        }

        // then
        latch.await(10, TimeUnit.SECONDS)
        assertEquals(1, successCount.get(), "하나의 예약만 성공해야 합니다")
        assertEquals(threadCount - 1, failCount.get(), "나머지는 실패해야 합니다")

        // verify
        val reservations = reservationRepository.findByScheduleId(schedule.id!!)
        assertEquals(seats.size, reservations.size, "예약된 좌석 수가 일치해야 합니다")
    }

    @Test
    fun `연속되지 않은 좌석은 예약할 수 없다`() {
        // given
        val nonConsecutiveSeats = listOf(1L, 3L, 5L) // A1, A3, A5

        // when & then
        assertFailsWith<IllegalArgumentException>("연속된 좌석만 예약할 수 있습니다.") {
            reservationService.reserve(
                scheduleId = schedule.id!!,
                seatIds = nonConsecutiveSeats,
                userId = "user-1"
            )
        }
    }

    @Test
    fun `한 사용자는 한 스케줄에 최대 5개까지만 좌석을 예약할 수 있다`() {
        // given
        val userId = "user-1"
        val firstReservationSeats = listOf(1L, 2L, 3L) // 첫 번째 예약: 3자리
        val secondReservationSeats = listOf(4L, 5L, 6L) // 두 번째 예약: 3자리 (초과)

        // when
        reservationService.reserve(
            scheduleId = schedule.id!!,
            seatIds = firstReservationSeats,
            userId = userId
        )

        // then
        assertFailsWith<IllegalArgumentException>("한 상영 스케줄당 최대 5개의 좌석만 예약할 수 있습니다.") {
            reservationService.reserve(
                scheduleId = schedule.id!!,
                seatIds = secondReservationSeats,
                userId = userId
            )
        }
    }
} 