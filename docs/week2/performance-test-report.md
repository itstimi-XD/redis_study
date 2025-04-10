# 성능 테스트 보고서

## 테스트 환경

- **하드웨어**: MacBook Pro
- **소프트웨어 버전**: 
  - Spring Boot 3.2.0
  - Kotlin 1.9.21
  - MySQL 8.0
  - Redis 7-alpine
  - K6 (부하 테스트 도구)

## 전제 조건

- **DAU**: 1,000명
- **1명당 1일 평균 접속 수**: 2번
- **피크 시간대의 집중률**: 평소 트래픽의 10배
- **Throughput 계산**:
    - **1일 총 접속 수** = DAU × 1명당 1일 평균 접속 수 = 1,000 × 2 = **2,000** (1일 총 접속 수)
    - **1일 평균 RPS** = 1일 총 접속 수 ÷ 86,400 (초/일)= 2,000 ÷ 86,400 ≈ **0.023 RPS**
    - **1일 최대 RPS** = 1일 평균 RPS × (최대 트래픽 / 평소 트래픽)= 0.023 × 10 = **0.23 RPS**
- **VU**: 100명 (부하 테스트 시 사용하는 가상 사용자 수)
- **Thresholds**:
    - p(95)의 응답 소요 시간 200ms 이하
    - 실패율 1% 이하


## 0. 1주차 API 테스트

### 개요

1주차에서 구현한 검색 기능이 없는 기본 API에 대한 성능 테스트입니다. 이 테스트는 해당하는 커밋에 체크아웃하여 수행되었습니다.

### 부하 테스트 결과
![1주차 부하테스트 결과](https://github.com/user-attachments/assets/10912775-3aa6-457e-8477-b8d33d8815f3)

**주요 지표**:
- **http_req_duration (평균)**: 17.18ms
- **http_req_duration (p95)**: 21.62ms
- **처리량(RPS)**:
  - 총 요청 수: 15,894건
	- 실행 시간: 301초 (5분 1초)
	- 평균 처리량: 약 52.80 RPS
- **실패율**: 10.27%

## 1. Index 적용 전

### 개요

2주차에서 구현한 검색 기능이 있는 API에 대한 성능 테스트입니다. 이 테스트는 해당하는 커밋에 체크아웃하여 수행되었습니다.


### 현재 생성된 인덱스 확인
```
mysql> SHOW INDEX FROM movies;
+--------+------------+----------+--------------+-------------+-----------+-------------+----------+--------+------+------------+---------+---------------+---------+------------+
| Table  | Non_unique | Key_name | Seq_in_index | Column_name | Collation | Cardinality | Sub_part | Packed | Null | Index_type | Comment | Index_comment | Visible | Expression |
+--------+------------+----------+--------------+-------------+-----------+-------------+----------+--------+------+------------+---------+---------------+---------+------------+
| movies |          0 | PRIMARY  |            1 | id          | A         |           6 |     NULL |   NULL |      | BTREE      |         |               | YES     | NULL       |
| movies |          1 | genre_id |            1 | genre_id    | A         |           3 |     NULL |   NULL |      | BTREE      |         |               | YES     | NULL       |
+--------+------------+----------+--------------+-------------+-----------+-------------+----------+--------+------+------------+---------+---------------+---------+------------+
2 rows in set (0.01 sec)
```
### 쿼리 실행 계획 확인
```bash
mysql -u cinema -p cinema
```

```sql
EXPLAIN SELECT m.* FROM movies m
WHERE m.release_date <= CURRENT_DATE
ORDER BY m.release_date DESC;
```
### 실행 계획

```
mysql> EXPLAIN SELECT m.* FROM movies m
    -> WHERE m.release_date <= CURRENT_DATE
    -> ORDER BY m.release_date DESC;
+----+-------------+-------+------------+------+---------------+------+---------+------+------+----------+-----------------------------+
| id | select_type | table | partitions | type | possible_keys | key  | key_len | ref  | rows | filtered | Extra                       |
+----+-------------+-------+------------+------+---------------+------+---------+------+------+----------+-----------------------------+
|  1 | SIMPLE      | m     | NULL       | ALL  | NULL          | NULL | NULL    | NULL |    6 |    33.33 | Using where; Using filesort |
+----+-------------+-------+------------+------+---------------+------+---------+------+------+----------+-----------------------------+
1 row in set, 1 warning (0.02 sec)
```

### 부하 테스트 결과
![인덱스 적용 전 부하 테스트 결과](https://github.com/user-attachments/assets/8768a5ed-120a-4ce1-aff1-25f51b0aced5)

**주요 지표**:
- **http_req_duration (평균)**: 9.42초

- **http_req_duration (p95)**: 14.14초

- **처리량(RPS)**: 
  - 총 요청 수: 2,245건
	- 실행 시간: 302.6초 (5분 2.6초)
	- 평균 처리량: 약 7.42 RPS 

- **실패율**: 10.24%


## 2. Index 적용 후

### 현재 생성된 인덱스 확인

```
mysql> SHOW INDEX FROM movies;
+--------+------------+------------------------+--------------+--------------+-----------+-------------+----------+--------+------+------------+---------+---------------+---------+------------+
| Table  | Non_unique | Key_name               | Seq_in_index | Column_name  | Collation | Cardinality | Sub_part | Packed | Null | Index_type | Comment | Index_comment | Visible | Expression |
+--------+------------+------------------------+--------------+--------------+-----------+-------------+----------+--------+------+------------+---------+---------------+---------+------------+
| movies |          0 | PRIMARY                |            1 | id           | A         |         500 |     NULL |   NULL |      | BTREE      |         |               | YES     | NULL       |
| movies |          1 | idx_movie_genre_id     |            1 | genre_id     | A         |          10 |     NULL |   NULL |      | BTREE      |         |               | YES     | NULL       |
| movies |          1 | idx_movie_title        |            1 | title        | A         |         500 |     NULL |   NULL |      | BTREE      |         |               | YES     | NULL       |
| movies |          1 | idx_movie_release_date |            1 | release_date | A         |         411 |     NULL |   NULL |      | BTREE      |         |               | YES     | NULL       |
+--------+------------+------------------------+--------------+--------------+-----------+-------------+----------+--------+------+------------+---------+---------------+---------+------------+
4 rows in set (0.00 sec)
```

### 적용한 인덱스 DDL

```sql
-- 영화 테이블에 인덱스 추가
CREATE INDEX idx_movie_title ON movies (title);
CREATE INDEX idx_movie_genre_id ON movies (genre_id);
CREATE INDEX idx_movie_release_date ON movies (release_date);
```

### 쿼리 실행 계획 확인

**일반 조회 쿼리**:
```sql
EXPLAIN
SELECT m.* FROM movies m
WHERE m.release_date <= CURRENT_DATE
ORDER BY m.release_date DESC;
```

**제목 검색 쿼리 (동등 연산)**:
```sql
EXPLAIN
SELECT m.* FROM movies m
WHERE m.title = '아바타' AND m.release_date <= CURRENT_DATE
ORDER BY m.release_date DESC;
```

**장르 검색 쿼리**:
```sql
EXPLAIN
SELECT m.* FROM movies m
JOIN genres g ON m.genre_id = g.id
WHERE g.name = '액션' AND m.release_date <= CURRENT_DATE
ORDER BY m.release_date DESC;
```

### 실행 계획

**일반 조회 쿼리 실행 계획**:
```
mysql> EXPLAIN
    -> SELECT m.* FROM movies m
    -> WHERE m.release_date <= CURRENT_DATE
    -> ORDER BY m.release_date DESC;
+----+-------------+-------+------------+------+------------------------+------+---------+------+------+----------+-----------------------------+
| id | select_type | table | partitions | type | possible_keys          | key  | key_len | ref  | rows | filtered | Extra                       |
+----+-------------+-------+------------+------+------------------------+------+---------+------+------+----------+-----------------------------+
|  1 | SIMPLE      | m     | NULL       | ALL  | idx_movie_release_date | NULL | NULL    | NULL |    4 |   100.00 | Using where; Using filesort |
+----+-------------+-------+------------+------+------------------------+------+---------+------+------+----------+-----------------------------+
1 row in set, 1 warning (0.00 sec)

```

**제목 검색 쿼리 실행 계획 (동등 연산)**:
```
mysql> EXPLAIN
    -> SELECT m.* FROM movies m
    -> WHERE m.title = '' AND m.release_date <= CURRENT_DATE
    -> ORDER BY m.release_date DESC;
+----+-------------+-------+------------+------+----------------------------------------+-----------------+---------+-------+------+----------+-----------------------------+
| id | select_type | table | partitions | type | possible_keys                          | key             | key_len | ref   | rows | filtered | Extra                       |
+----+-------------+-------+------------+------+----------------------------------------+-----------------+---------+-------+------+----------+-----------------------------+
|  1 | SIMPLE      | m     | NULL       | ref  | idx_movie_title,idx_movie_release_date | idx_movie_title | 1022    | const |    1 |   100.00 | Using where; Using filesort |
+----+-------------+-------+------------+------+----------------------------------------+-----------------+---------+-------+------+----------+-----------------------------+
1 row in set, 1 warning (0.01 sec)
```

**장르 검색 쿼리 실행 계획**:
```
mysql> EXPLAIN
    -> SELECT m.* FROM movies m
    -> JOIN genres g ON m.genre_id = g.id
    -> WHERE g.name = '' AND m.release_date <= CURRENT_DATE
    -> ORDER BY m.release_date DESC;
+----+-------------+-------+------------+------+-------------------------------------------+--------------------+---------+-------------+------+----------+----------------------------------------------+
| id | select_type | table | partitions | type | possible_keys                             | key                | key_len | ref         | rows | filtered | Extra                                        |
+----+-------------+-------+------------+------+-------------------------------------------+--------------------+---------+-------------+------+----------+----------------------------------------------+
|  1 | SIMPLE      | g     | NULL       | ALL  | PRIMARY                                   | NULL               | NULL    | NULL        |   10 |    10.00 | Using where; Using temporary; Using filesort |
|  1 | SIMPLE      | m     | NULL       | ref  | idx_movie_genre_id,idx_movie_release_date | idx_movie_genre_id | 8       | cinema.g.id |    1 |   100.00 | Using where                                  |
+----+-------------+-------+------------+------+-------------------------------------------+--------------------+---------+-------------+------+----------+----------------------------------------------+
2 rows in set, 1 warning (0.00 sec)
```

### 부하 테스트 결과

![인덱스 적용 후 부하 테스트 결과](https://github.com/user-attachments/assets/6f23e355-f471-4ef1-93af-21fe3e3bf9bf)

**주요 지표**:
- **http_req_duration (평균)**: 9.59초
- **http_req_duration (p95)**: 15.08초
- **처리량(RPS)**: 
  - 총 요청 수: 2,206건
	- 실행 시간: 300.7초 (5분 0.7초)
	- 평균 처리량: 약 7.34 RPS
- **실패율**: 10.29%

## 3. 로컬 캐시 적용 후 테스트

### 구현 내용

로컬 캐시를 적용하기 위해 Spring의 `@Cacheable` 애노테이션을 사용하여 메모리 내 캐싱을 구현했습니다. 이 방식은 단일 인스턴스 환경에서 유용하지만, 여러 서버 인스턴스가 있는 분산 환경에서는 캐시 일관성 문제가 발생할 수 있습니다.

```kotlin
@Configuration
@EnableCaching
@Profile("local-cache")
class CaffeineCacheConfig {
    
    @Bean
    fun cacheManager(): CacheManager {
        val caffeineBuilder = Caffeine.newBuilder()
            .maximumSize(500)
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .recordStats()
            
        return CaffeineCacheManager().apply {
            setCaffeine(caffeineBuilder)
            setCacheNames(listOf("nowPlayingMovies"))
        }
    }
}
```

MovieFacade에서 캐시를 적용하여 서비스 계층의 호출 자체를 최소화했습니다:

```kotlin
@Service
class MovieFacade(
    private val movieService: MovieService,
    private val scheduleService: ScheduleService
) {
    
    @Cacheable(
        value = ["nowPlayingMovies"], 
        key = "#title?.toString() ?: 'all' + '_' + #genre?.toString() ?: 'all'"
    )
    fun getNowPlayingMovies(title: String? = null, genre: String? = null): List<MovieResponseDto> {
        val nowPlayingMovies = movieService.findNowPlayingMovies(title, genre)
        return nowPlayingMovies.mapNotNull { movie ->
            movie.id?.let { movieId ->
                val schedules = scheduleService.findByMovie(movieId)
                MovieResponseDto(
                    id = movieId,
                    title = movie.title,
                    rating = movie.rating,
                    releaseDate = movie.releaseDate,
                    thumbnailUrl = movie.thumbnailUrl,
                    runningTime = movie.runningTime,
                    genre = movie.genre.name,
                    schedules = schedules.mapNotNull { schedule ->
                        schedule.id?.let { scheduleId ->
                            ScheduleResponseDto(
                                id = scheduleId,
                                theaterName = schedule.theater.name,
                                startTime = schedule.startTime,
                                endTime = schedule.endTime
                            )
                        }
                    }.sortedBy { it.startTime }
                )
            }
        }.sortedByDescending { it.releaseDate }
    }
}
```

서비스 계층은 비즈니스 로직만 담당하고 캐싱은 퍼사드 계층에서 처리하여 관심사를 분리했습니다:

```kotlin
@Service
class MovieService(
    private val movieRepository: MovieRepository
) {
    @Transactional(readOnly = true)
    fun findNowPlayingMovies(title: String? = null, genre: String? = null): List<Movie> {
        // 검색 조건이 없을 경우 기존 메서드 사용
        if (title.isNullOrBlank() && genre.isNullOrBlank()) {
            return movieRepository.findByReleaseDateLessThanEqualOrderByReleaseDateDesc(LocalDate.now())
        }

        // 검색 조건이 있는 경우 커스텀 메서드 사용
        return movieRepository.findNowPlayingMoviesWithFilters(title, genre)
    }
}
```

### 부하 테스트 결과

![로컬 캐시 적용 후 부하 테스트 결과](https://github.com/user-attachments/assets/d7aa6460-5d92-4328-b48d-00e42f902ec5)

**주요 지표**:
- **http_req_duration (평균)**: 4.38ms
- **http_req_duration (p95)**: 6.78ms
- **처리량(RPS)**: 
  - 총 요청 수: 16,067건
	- 실행 시간: 301.3초 (5분 1.3초)
	- 평균 처리량: 약 53.33 RPS
- **실패율**: 9.75%

### 분석

로컬 캐시 적용 후 성능이 크게 개선되었습니다:
- 응답 시간: 
- 처리량: 
- 실패율: 

## 4. 분산 캐시(Redis) 적용 후 테스트

### 구현 내용

분산 환경에서의 캐시 일관성 문제를 해결하기 위해 Redis를 사용한 분산 캐시를 구현했습니다. Redis는 독립적인 캐시 서버로 동작하여 여러 애플리케이션 인스턴스에서 동일한 캐시를 공유할 수 있습니다.

```kotlin
@Configuration
@EnableCaching
@Profile("redis-cache")
class RedisConfig {
    
    @Bean
    fun redisConnectionFactory(
        @Value("\${spring.redis.host:localhost}") host: String,
        @Value("\${spring.redis.port:6379}") port: Int
    ): RedisConnectionFactory {
        val config = RedisStandaloneConfiguration(host, port)
        return LettuceConnectionFactory(config)
    }
    
    @Bean
    fun redisTemplate(connectionFactory: RedisConnectionFactory): RedisTemplate<String, Any> {
        return RedisTemplate<String, Any>().apply {
            setConnectionFactory(connectionFactory)
            keySerializer = StringRedisSerializer()
            valueSerializer = GenericJackson2JsonRedisSerializer()
        }
    }
    
    @Bean
    fun cacheManager(redisConnectionFactory: RedisConnectionFactory): CacheManager {
        val redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(10))
            .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer()))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(GenericJackson2JsonRedisSerializer())
            )
        
        return RedisCacheManager.builder(redisConnectionFactory)
            .cacheDefaults(redisCacheConfiguration)
            .build()
    }
}
```

### 부하 테스트 결과

![Redis 캐시 적용 후 부하 테스트 결과](https://github.com/user-attachments/assets/c3f09d48-2888-402d-a108-0ad9d9505cf8)

**주요 지표**:
- **http_req_duration (평균)**: 10.08초
- **http_req_duration (p95)**: 14.77초
- **처리량(RPS)**: 
  - 총 요청 수: 2,113건
	- 실행 시간: 301.2초 (5분 1.2초)
	- 평균 처리량: 약 7.02 RPS
- **실패율**: 100% (ㅠㅠㅠㅠㅠㅠㅠㅠㅠ)

### 분석

Redis 캐시 적용 결과:
- 응답 시간: Redis 연결 실패로 인해 평균 10.08초, p95 14.77초로 매우 높은 응답 시간을 기록
- 처리량: 평균 7.02 RPS로 매우 낮은 처리량을 보임
- 실패율: Redis 연결 문제로 인해 100% 실패율을 기록

## 5. 종합 비교 분석

### 성능 지표 비교

| 구분 | 평균 응답 시간 | p95 응답 시간 | 처리량(RPS) | 실패율 |
|------|--------------|--------------|------------|-------|
| 1주차 API | 17.18ms | 21.62ms | 52.80 | 10.27% |
| 인덱스 적용 전 | 9.42s | 14.14s | 7.42 | 10.24% |
| 인덱스 적용 후 | 9.59s | 15.08s | 7.34 | 10.29% |
| 로컬 캐시 적용 | 4.38ms | 6.78ms | 53.33 | 9.75% |
| Redis 캐시 적용 | 10.08s | 14.77s | 7.02 | 100% |

### 결론

1. **1주차 vs 2주차 기본 성능**
   - 1주차 API는 단순 조회 기능만 있어 상대적으로 좋은 성능을 보임
   - 2주차에서 검색 기능 추가로 인해 성능이 크게 저하됨 (응답 시간 약 550배 증가)

2. **인덱스 적용 효과**
   - 예상과 달리 인덱스 적용 전후의 성능 차이가 미미함
   - 이는 데이터셋이 작고(500개), 검색 조건이 복잡하지 않아 인덱스의 효과가 크게 나타나지 않은 것으로 분석됨
   - 대규모 데이터셋에서는 인덱스 효과가 더 명확하게 나타날 것으로 예상

3. **로컬 캐시의 효과**
   - 가장 큰 성능 개선을 보여줌
   - 평균 응답 시간이 9.42초에서 4.38ms로 약 2,150배 개선
   - 처리량도 7.42 RPS에서 53.33 RPS로 약 7배 증가
   - 실패율도 소폭 감소

4. **Redis 캐시 적용 시 문제점**
   - Redis 연결 설정 문제로 인해 오히려 성능 저하
   - 분산 환경에서의 캐시 전략은 신중한 설정과 모니터링이 필요함을 시사

5. **개선 방향**
   - Redis 연결 문제 해결이 최우선 과제
   - 대규모 데이터셋에서의 인덱스 효과 검증 필요
   - 캐시 전략의 세분화 (TTL, 캐시 키 설계 등)
   - 실패율 개선을 위한 추가적인 에러 핸들링 필요

6. **최종 권장사항**
   - 현재 환경에서는 로컬 캐시(Caffeine) 사용이 가장 효과적
   - 향후 분산 환경 전환 시 Redis 설정 문제 해결 후 재검토
   - 데이터 증가에 대비한 인덱스 전략 수립 필요


## 6. Redis Failover 전략

### 문제 상황
- Redis 연결 실패 시 전체 시스템 장애로 이어지는 문제 발생
- 캐시 계층이 단일 장애 지점(Single Point of Failure)이 되는 위험

### 개선 전략

#### 1. Circuit Breaker 패턴 적용
```kotlin
@Cacheable(
    value = ["nowPlayingMovies"], 
    key = "#title?.toString() ?: 'all' + '_' + #genre?.toString() ?: 'all'",
    unless = "#result == null"
)
@CircuitBreaker(name = "movieCache", fallbackMethod = "getNowPlayingMoviesFallback")
fun getNowPlayingMovies(title: String? = null, genre: String? = null): List<MovieResponseDto>
```

#### 2. Fallback 메커니즘
- Redis 접근 실패 시 즉시 데이터베이스 조회로 전환
- 로깅을 통한 장애 상황 모니터링
- 점진적인 복구를 위한 Circuit Breaker 설정

#### 3. Circuit Breaker 설정
```yaml
resilience4j:
  circuitbreaker:
    instances:
      movieCache:
        slidingWindowSize: 10        # 상태 결정을 위한 호출 수
        minimumNumberOfCalls: 5      # 최소 호출 횟수
        failureRateThreshold: 50     # 실패율 임계값
        waitDurationInOpenState: 10s # Circuit Open 상태 유지 시간
        permittedNumberOfCallsInHalfOpenState: 3  # Half-Open 상태에서 허용할 호출 수
```

### 기대 효과
1. **안정성 향상**
   - Redis 장애 시에도 서비스 가용성 유지
   - 점진적인 복구를 통한 시스템 안정성 확보

2. **모니터링 강화**
   - 장애 상황에 대한 로깅 및 추적
   - 캐시 계층의 건강도 모니터링

3. **성능 영향 최소화**
   - 장애 상황에서도 acceptable한 응답 시간 유지
   - 부분적 성능 저하는 있으나 전체 시스템 장애 방지

### 추가 고려사항
1. **캐시 복구 전략**
   - Circuit Breaker가 Half-Open 상태로 전환 시 점진적인 트래픽 처리
   - Redis 복구 후 캐시 워밍업 전략 수립 필요

2. **모니터링 및 알림**
   - Circuit Breaker 상태 변화 모니터링
   - 임계값 초과 시 운영팀 알림

3. **캐시 키 전략 개선**
   - 캐시 히트율을 높이기 위한 키 설계 재검토
   - 장르 기반 캐싱 우선 적용 검토


## 7. 개선사항 및 향후 과제

### 1. 테스트 데이터 개선
- **현재 한계점**
  - 테스트용 영화 데이터 500개로 제한적
  - 검색 테스트에 사용된 제목이 4개로 너무 적음
  - 캐시 히트율이 비정상적으로 높아질 가능성

- **개선 방안**
  - 실제 서비스 규모를 고려한 데이터셋 확장 (최소 10,000개 이상)
  - 다양한 검색 패턴을 반영한 테스트 시나리오 추가
  - 실제 사용자 패턴을 반영한 데이터 분포 (인기 영화, 장르별 분포 등)

### 2. 캐시 전략 개선
- **TTL 설정 기준 명확화**
  - 현재: 임의로 설정된 10분
  - 개선: 데이터 변경 주기 분석에 기반한 TTL 설정
    - 영화 정보 업데이트 주기: 일 1회
    - 상영 스케줄 변경 주기: 시간당
    - 장르별 차등 적용 검토

- **캐시 키 전략 최적화**
  - 현재: title + genre 조합으로 인한 낮은 히트율
  - 개선: 장르 기반 우선 캐싱 + 인기 검색어 기반 캐싱

### 3. 실패율 개선 전략
#### 현재 문제점
- Redis 연결 실패 시 100% 실패율
- 일반 요청에서도 10% 수준의 높은 실패율

#### 단계별 개선 방안
1. **즉시 적용 가능한 개선**
   - Circuit Breaker 패턴 적용으로 Redis 장애 대응
   - 로깅 강화로 실패 원인 추적
   - 타임아웃 설정 최적화

2. **성능 최적화**
   - 인덱스 재설계 (복합 인덱스 검토)
   - 캐시 워밍업 전략 도입
   - 쿼리 최적화 (Projection 활용)

3. **인프라 레벨 개선**
   - Redis Cluster 구성 검토
   - 부하 분산 전략 수립
   - 모니터링 체계 구축

### 4. Redis 연결 문제 해결
#### 현재 설정 문제점
- 기본 타임아웃 설정 부재
- 커넥션 풀 설정 미흡
- 장애 복구 전략 부재

#### 개선된 설정
```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      timeout: 3000
      connect-timeout: 3000
      client-type: lettuce
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 2
          max-wait: 1000
        shutdown-timeout: 100ms
```

#### 모니터링 강화
- Redis 연결 상태 모니터링
- 커넥션 풀 사용량 추적
- 응답 시간 및 타임아웃 발생 빈도 측정

### 5. 대규모 데이터셋 테스트 계획
1. **데이터 준비**
   - 실제 서비스 규모의 테스트 데이터 생성
   - 다양한 검색 패턴 시나리오 구성
   - 실제 사용자 패턴 분석 및 반영

2. **테스트 단계**
   - 인덱스 적용 전 기준 측정
   - 인덱스 적용 후 실행 계획 분석
   - 성능 지표 측정 (TPS, 응답시간, CPU/메모리 사용량)

3. **결과 분석**
   - 인덱스 효과 정량적 평가
   - 병목 구간 식별
   - 추가 최적화 포인트 도출



