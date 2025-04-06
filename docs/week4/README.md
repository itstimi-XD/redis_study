# Week 4: Rate Limit 구현

## 개요
API 서버의 안정성을 위해 Rate Limit 기능을 구현했습니다. 단일 서버 환경과 분산 환경 모두를 고려하여 두 가지 구현체를 제공합니다.

## 구현 내용

### 1. Rate Limit 인터페이스
```kotlin
interface RateLimiter {
    fun checkApiCallLimit(clientIp: String)
    fun checkBookingLimit(clientIp: String, movieTimeId: String)
}
```

### 2. 단일 서버 환경 구현 (Google Guava)
- Google Guava의 RateLimiter를 사용하여 구현
- In-memory 캐시를 사용하여 IP별 요청 제한
- 특징:
  - API 호출: 초당 50개 요청으로 제한
  - 예약 API: 같은 영화 시간에 대해 5분 내 재호출 불가

### 3. 분산 환경 구현 (Redis)
- Redisson 클라이언트를 사용하여 구현
- Redis를 사용하여 분산 환경에서 동기화
- 특징:
  - API 호출: 분당 50개 요청으로 제한
  - 과도한 요청 시 IP 1시간 차단
  - 예약 API: 같은 영화 시간에 대해 5분 내 재호출 불가

### 4. 프로필 기반 구현체 선택
- `single` 프로필: Guava RateLimiter 사용
- `distributed` 프로필: Redis RateLimiter 사용

## 사용 방법

### 1. 애플리케이션 실행
```bash
# 단일 서버 환경
./gradlew bootRun --args='--spring.profiles.active=single'

# 분산 환경
./gradlew bootRun --args='--spring.profiles.active=distributed'
```

### 2. API 사용 예시
```kotlin
@RateLimit(type = RateLimitType.API_CALL)
@GetMapping("/movies")
fun getMovies(): List<MovieResponse> {
    // ...
}

@RateLimit(type = RateLimitType.BOOKING)
@PostMapping("/reservations")
fun createReservation(@RequestBody request: ReservationRequest): ReservationResponse {
    // ...
}
```

## 테스트
- 각 구현체별 단위 테스트 구현
- 통합 테스트를 통한 실제 동작 검증
- 프로필별 테스트 환경 분리

## 기술 스택
- Spring Boot
- Google Guava
- Redis (Redisson)
- Kotlin
- JUnit 5 