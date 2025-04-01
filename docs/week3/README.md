# 3주차: 동시성 이슈 해결과 성능 최적화

## 구현 과정

### 1. 예약 API 기본 구조 구현 ✅
- Reservation 엔티티 및 관련 레포지토리 구현
- 예약 API 컨트롤러 및 Facade 구현
- 메시지 서비스 구현 (FCM 시뮬레이션)
- Infrastructure 모듈 QueryDSL 설정 추가
- init.sql에 reservations 테이블 추가

### 2. Pessimistic Lock 구현 ✅
- JPA의 `@Lock(LockModeType.PESSIMISTIC_WRITE)` 활용
- 예약 시도 시점에 해당 좌석들에 대해 비관적 락 획득
- 트랜잭션 종료 시까지 다른 트랜잭션의 접근 차단

### 3. Optimistic Lock 구현 🏗️
- 구현 예정

### 4. AOP 기반 Distributed Lock 구현 🏗️
- 구현 예정

### 5. 함수형 기반 Distributed Lock 구현 🏗️
- 구현 예정

## 상세 구현 내용

### 1. 예약 API 기본 구조

#### 1.1 도메인 설계
- **Reservation 엔티티**
  ```kotlin
  @Entity
  @Table(name = "reservations")
  class Reservation(
      @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
      val id: Long? = null,
      
      @ManyToOne(fetch = FetchType.LAZY)
      @JoinColumn(name = "schedule_id")
      val schedule: Schedule,
      
      @ManyToOne(fetch = FetchType.LAZY)
      @JoinColumn(name = "seat_id")
      val seat: Seat,
      
      val userId: String,
      
      @Version
      var version: Long = 0,
      
      val reservedAt: LocalDateTime = LocalDateTime.now()
  )
  ```
  - `@Version` 필드를 미리 추가하여 향후 Optimistic Lock 구현을 준비
  - Schedule, Seat와 다대일 관계 설정
  - 사용자 식별을 위한 userId 필드 추가

#### 1.2 비즈니스 규칙
1. 한 번에 최대 5개 좌석까지만 예약 가능
2. 연속된 좌석만 예약 가능
3. 이미 예약된 좌석은 예약 불가
4. 예약 완료시 FCM 푸시 알림 발송 (시뮬레이션)

#### 1.3 데이터베이스 설계
```sql
CREATE TABLE reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    schedule_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    version BIGINT DEFAULT 0,
    reserved_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    updated_at DATETIME NOT NULL,
    updated_by VARCHAR(50) NOT NULL,
    FOREIGN KEY (schedule_id) REFERENCES schedules(id),
    FOREIGN KEY (seat_id) REFERENCES seats(id)
);

-- 성능을 위한 인덱스 추가
CREATE INDEX idx_reservation_schedule_seat ON reservations (schedule_id, seat_id);
CREATE INDEX idx_reservation_user ON reservations (user_id);
```

#### 1.4 API 명세
```http
POST /api/reservations
Header: X-USER-ID: {userId}
Body: {
    "scheduleId": 1,
    "seatIds": [1, 2, 3]
}
Response: [
    {
        "id": 1,
        "scheduleId": 1,
        "movieTitle": "어벤져스",
        "theaterName": "1관",
        "seatNumber": "A1",
        "startTime": "2024-01-01T10:00:00",
        "endTime": "2024-01-01T12:00:00",
        "reservedAt": "2024-01-01T09:00:00"
    },
    ...
]
``` 

### 2. Pessimistic Lock 구현

#### 2.1 개요
비관적 락(Pessimistic Lock)은 데이터베이스 수준에서 동시성을 제어하는 방식입니다. 트랜잭션이 시작될 때 해당 데이터에 대한 락을 획득하고, 트랜잭션이 종료될 때까지 다른 트랜잭션이 해당 데이터에 접근하지 못하도록 합니다.

#### 2.2 구현 방식
1. **Repository 인터페이스 분리**
   ```kotlin
   interface PessimisticLockableReservationRepository {
       fun findAllByScheduleIdAndSeatIdInWithPessimisticLock(
           scheduleId: Long, 
           seatIds: List<Long>
       ): List<Reservation>
   }
   ```

2. **JPA Repository에 락 적용**
   ```kotlin
   interface ReservationJpaRepository : JpaRepository<Reservation, Long> {
       @Lock(LockModeType.PESSIMISTIC_WRITE)
       @Query("SELECT r FROM Reservation r WHERE r.schedule.id = :scheduleId AND r.seat.id IN :seatIds")
       fun findAllByScheduleIdAndSeatIdInWithPessimisticLock(
           scheduleId: Long, 
           seatIds: List<Long>
       ): List<Reservation>
   }
   ```

3. **Service 레이어 적용**
   ```kotlin
   @Transactional
   fun reserve(scheduleId: Long, seatIds: List<Long>, userId: String): List<Reservation> {
       // ... 기존 검증 로직 ...

       // 비관적 락을 사용하여 이미 예약된 좌석 확인
       val existingReservations = (reservationRepository as PessimisticLockableReservationRepository)
           .findAllByScheduleIdAndSeatIdInWithPessimisticLock(scheduleId, seatIds)
       require(existingReservations.isEmpty()) { "이미 예약된 좌석이 포함되어 있습니다." }

       // ... 예약 처리 로직 ...
   }
   ```

#### 2.3 장단점
**장점**
- 충돌이 자주 발생하는 환경에서 데이터 일관성을 보장
- 롤백 발생 가능성이 낮음
- 구현이 상대적으로 단순

**단점**
- 동시성이 떨어짐 (한 번에 하나의 트랜잭션만 접근 가능)
- 데드락 발생 가능성
- 성능 오버헤드 (락 획득/해제에 따른) 

#### 2.4 테스트 구현
동시성 문제 해결을 검증하기 위한 테스트를 구현했습니다.

1. **동시 예약 테스트**
   ```kotlin
   @Test
   fun `동시에 같은 좌석을 예약하면 하나만 성공해야 한다`() {
       val threadCount = 10
       val executorService = Executors.newFixedThreadPool(32)
       val latch = CountDownLatch(threadCount)
       val successCount = AtomicInteger()
       val failCount = AtomicInteger()

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

       latch.await(10, TimeUnit.SECONDS)
       assertEquals(1, successCount.get())
       assertEquals(threadCount - 1, failCount.get())
   }
   ```

2. **비즈니스 규칙 테스트**
   - 연속되지 않은 좌석 예약 시도
   - 한 사용자의 최대 예약 가능 좌석 수 초과 시도

#### 2.5 테스트 결과 분석
1. **동시성 제어 성공**
   - 10개의 동시 요청 중 정확히 1개만 성공
   - 나머지 9개는 예상대로 실패 처리됨
   - 데이터베이스에는 중복 예약이 발생하지 않음

2. **성능 고려사항**
   - 비관적 락으로 인한 대기 시간 발생
   - 실제 운영 환경에서는 타임아웃 설정 필요
   - 데드락 방지를 위한 추가 전략 검토 필요 