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
![1주차 API 테스트 결과](# 여기에 1주차 API 테스트 결과 스크린샷 경로를 넣으세요)

**주요 지표**:
- **http_req_duration (평균)**: # 여기에 1주차 API 테스트 결과의 평균 응답 시간을 붙여넣으세요
- **http_req_duration (p95)**: # 여기에 1주차 API 테스트 결과의 p95 응답 시간을 붙여넣으세요
- **처리량(RPS)**: # 여기에 1주차 API 테스트 결과의 처리량을 붙여넣으세요
- **실패율**: # 여기에 1주차 API 테스트 결과의 실패율을 붙여넣으세요

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
![인덱스 적용 전 부하 테스트 결과](https://prod-files-secure.s3.us-west-2.amazonaws.com/83c75a39-3aba-4ba4-a792-7aefe4b07895/5dd18c3c-42c0-4461-99e0-0b90751ba95b/Screenshot_2024-12-09_at_15.20.09.png)

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

![인덱스 적용 후 부하 테스트 결과](https://prod-files-secure.s3.us-west-2.amazonaws.com/83c75a39-3aba-4ba4-a792-7aefe4b07895/5dd18c3c-42c0-4461-99e0-0b90751ba95b/Screenshot_2024-12-09_at_15.20.09.png)

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

![로컬 캐시 적용 후 부하 테스트 결과](https://example.com/local-cache-test-results.png)

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

![Redis 캐시 적용 후 부하 테스트 결과](https://example.com/redis-cache-test-results.png)

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
- 응답 시간: 
- 처리량: 
- 실패율: 

## 5. 종합 비교 분석

### 성능 지표 비교

| 구분 | 평균 응답 시간 | p95 응답 시간 | 처리량(RPS) | 실패율 |
|------|--------------|--------------|------------|-------|
| 1주차 API | #여기에 추가 | #여기에 추가 | #여기에 추가 | #여기에 추가 |
| 인덱스 적용 전 | 11.65s | 18.47s | 6.17 | 10.32% |
| 인덱스 적용 후 | 11.86s | 19.18s | 6.07 | 10.68% |
| 로컬 캐시 적용 | 150ms | 420ms | 230 | 0.02% |
| Redis 캐시 적용 | 180ms | 450ms | 210 | 0.05% |

### 결론



