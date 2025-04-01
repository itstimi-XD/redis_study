package com.hanghae.cinema.application.reservation

import com.hanghae.cinema.application.config.TestConfig
import com.hanghae.cinema.application.message.TestMessageService
import com.hanghae.cinema.application.reservation.dto.ReservationRequest
import com.hanghae.cinema.domain.movie.Movie
import com.hanghae.cinema.domain.reservation.ReservationRepository
import com.hanghae.cinema.domain.reservation.ReservationService
import com.hanghae.cinema.domain.schedule.Schedule
import com.hanghae.cinema.domain.schedule.ScheduleRepository
import com.hanghae.cinema.domain.seat.Seat
import com.hanghae.cinema.domain.seat.SeatRepository
import com.hanghae.cinema.domain.theater.Theater
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDateTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import org.junit.jupiter.api.Assertions.*
import org.springframework.dao.PessimisticLockingFailureException
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import java.util.concurrent.atomic.AtomicInteger
import com.hanghae.cinema.application.config.ApplicationConfig

@SpringBootTest(
    classes = [ApplicationConfig::class, TestConfig::class]
)
@Testcontainers
@ActiveProfiles("test")
class ReservationFacadeTest @Autowired constructor(
    private val reservationFacade: ReservationFacade,
    private val reservationService: ReservationService,
    private val messageService: TestMessageService,
    private val scheduleRepository: ScheduleRepository,
    private val seatRepository: SeatRepository,
    private val reservationRepository: ReservationRepository
) {
    companion object {
        @Container
        val mysqlContainer = MySQLContainer<Nothing>("mysql:8.0.32").apply {
            withDatabaseName("testdb")
            withUsername("test")
            withPassword("test")
            withInitScript("schema.sql")
        }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { mysqlContainer.jdbcUrl }
            registry.add("spring.datasource.username") { mysqlContainer.username }
            registry.add("spring.datasource.password") { mysqlContainer.password }
            registry.add("spring.jpa.hibernate.ddl-auto") { "validate" }
            registry.add("spring.jpa.database-platform") { "org.hibernate.dialect.MySQLDialect" }
            registry.add("spring.jpa.show-sql") { "true" }
            registry.add("spring.jpa.properties.hibernate.format_sql") { "true" }
            registry.add("spring.sql.init.mode") { "always" }
            registry.add("spring.sql.init.schema-locations") { "classpath:schema.sql" }
            registry.add("spring.jpa.defer-datasource-initialization") { "true" }
        }
    }

    private lateinit var schedule: Schedule
    private lateinit var seats: List<Seat>

    @BeforeEach
    fun setUp() {
        // schema.sql에서 미리 생성된 데이터 사용
        schedule = scheduleRepository.findById(1L)
            ?: throw IllegalStateException("Test schedule not found")
        
        seats = seatRepository.findAllById(listOf(1L, 2L, 3L, 4L, 5L))
        
        messageService.clearMessages()
    }

    @Test
    fun `동시 예약 시도시 하나만 성공하고 나머지는 실패해야 한다`() {
        // given
        val threadCount = 5
        val executorService = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)
        val results = mutableMapOf<String, Throwable?>()

        // when
        repeat(threadCount) { index ->
            executorService.submit {
                try {
                    val userId = "user-$index"
                    val request = ReservationRequest(
                        scheduleId = schedule.id!!,
                        seatIds = listOf(seats.first().id!!)
                    )
                    reservationFacade.reserve(request, userId)
                    results[userId] = null // 성공
                } catch (e: Exception) {
                    results["user-$index"] = e
                } finally {
                    latch.countDown()
                }
            }
        }

        // then
        latch.await(10, TimeUnit.SECONDS)
        executorService.shutdown()

        val successCount = results.count { it.value == null }
        val lockFailureCount = results.count { it.value is PessimisticLockingFailureException }

        assertEquals(1, successCount, "하나의 예약만 성공해야 합니다")
        assertEquals(threadCount - 1, lockFailureCount, "나머지는 락 획득 실패로 실패해야 합니다")
    }

    @Test
    fun `예약 성공시 알림 메시지가 발송되어야 한다`() {
        // given
        val userId = "test-user"
        val request = ReservationRequest(
            scheduleId = schedule.id!!,
            seatIds = listOf(seats.first().id!!)
        )

        // when
        val response = reservationFacade.reserve(request, userId)

        // then
        assertNotNull(response)
        assertTrue(response.isNotEmpty())
        
        val message = messageService.getLastMessageFor(userId)
        assertNotNull(message, "메시지가 발송되어야 합니다")
    }

    @Test
    fun `예약 실패시 알림 메시지가 발송되지 않아야 한다`() {
        // given
        val userId = "test-user"
        val invalidSeatId = -1L
        val request = ReservationRequest(
            scheduleId = schedule.id!!,
            seatIds = listOf(invalidSeatId)
        )

        // when & then
        assertThrows(IllegalArgumentException::class.java) {
            reservationFacade.reserve(request, userId)
        }
        
        val message = messageService.getLastMessageFor(userId)
        assertNull(message, "메시지가 발송되지 않아야 합니다")
    }

    @Test
    fun `동시에 여러 사용자가 같은 좌석을 예약할 때 하나만 성공해야 한다`() {
        // given
        val threadCount = 5
        val executorService = Executors.newFixedThreadPool(32)
        val latch = CountDownLatch(threadCount)
        val successCount = AtomicInteger(0)
        val failCount = AtomicInteger(0)
        val targetSeat = seats.first()

        // when
        repeat(threadCount) { index ->
            executorService.submit {
                try {
                    val userId = "user-$index"
                    val request = ReservationRequest(
                        scheduleId = schedule.id!!,
                        seatIds = listOf(targetSeat.id!!)
                    )
                    reservationFacade.reserve(request, userId)
                    successCount.incrementAndGet()
                } catch (e: PessimisticLockingFailureException) {
                    failCount.incrementAndGet()
                } finally {
                    latch.countDown()
                }
            }
        }

        // then
        latch.await(10, TimeUnit.SECONDS)
        executorService.shutdown()
        
        assertEquals(1, successCount.get())
        assertEquals(4, failCount.get())
        val reservations = reservationRepository.findByScheduleId(schedule.id!!)
        assertEquals(1, reservations.size)
    }

    @Test
    fun `예약 성공시 메시지가 전송되어야 한다`() {
        // given
        val userId = "test-user"
        val request = ReservationRequest(
            scheduleId = schedule.id!!,
            seatIds = listOf(seats.first().id!!)
        )

        // when
        reservationFacade.reserve(request, userId)

        // then
        val lastMessage = messageService.getLastMessageFor(userId)
        assertNotNull(lastMessage)
        assertTrue(lastMessage!!.contains("예약이 완료되었습니다"))
    }
} 