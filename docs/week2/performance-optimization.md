# 영화 검색 및 성능 최적화 가이드

이 문서는 항해 시네마 프로젝트 2주차 성능 최적화 요구사항 구현 방법에 대해 설명합니다. 검색 기능, 인덱싱, 캐싱(로컬 및 분산)을 통한 성능 개선 방법과 성능 테스트 실행 방법을 다룹니다.

## 데이터베이스 환경

이 프로젝트는 MySQL 데이터베이스를 사용합니다. Docker Compose를 통해 MySQL과 Redis 서비스를 실행할 수 있습니다.

```bash
# MySQL + Redis 실행
docker-compose up -d
```

## 목차

- [요구사항 개요](#요구사항-개요)
- [검색 기능 구현](#검색-기능-구현)
- [인덱스 추가](#인덱스-추가)
- [로컬 캐시(Caffeine) 적용](#로컬-캐시-caffeine-적용)
- [분산 캐시(Redis) 적용](#분산-캐시-redis-적용)
- [성능 테스트 실행](#성능-테스트-실행)
- [성능 테스트 보고서 작성](#성능-테스트-보고서-작성)

## 요구사항 개요

2주차의 핵심 요구사항은 다음과 같습니다:

1. **API Refactoring**: 영화 검색 기능 추가
2. **Indexing**: 적절한 인덱스 생성 및 효과 측정
3. **Caching**: Redis 캐싱을 활용한 메인 페이지 성능 향상

## 검색 기능 구현

영화 제목과 장르를 기준으로 검색할 수 있는 기능을 구현했습니다.

### 1. Controller 수정

```kotlin
@RestController
@RequestMapping("/api/movies")
class MovieController(private val movieFacade: MovieFacade) {

    @GetMapping
    @Operation(summary = "현재 상영 중인 영화 목록 조회", description = "현재 상영 중인 영화 목록을 반환합니다.")
    fun getNowPlayingMovies(
        @RequestParam(required = false) title: String?,
        @RequestParam(required = false) genre: String?
    ): ResponseEntity<List<MovieResponseDto>> {
        // 요청 파라미터 검증 -> 추후에 다른 계층으로 이동시켜보려 합니다
        validateSearchParams(title, genre)
        return ResponseEntity.ok(movieFacade.getNowPlayingMovies(title, genre))
    }

    private fun validateSearchParams(title: String?, genre: String?) {
        if (title != null && title.length > 255) {
            throw InvalidRequestException("영화 제목은 255자를 초과할 수 없습니다.")
        }
        if (genre != null && genre.length > 100) {
            throw InvalidRequestException("장르 이름은 100자를 초과할 수 없습니다.")
        }
    }
}
```

### 2. MovieFacade 수정

```kotlin
@Service
class MovieFacade(
    private val movieService: MovieService,
    private val scheduleService: ScheduleService
) {
    
    fun getNowPlayingMovies(title: String? = null, genre: String? = null): List<MovieResponseDto> {
        val nowPlayingMovies = movieService.findNowPlayingMovies(title, genre)
        return nowPlayingMovies.mapNotNull { movie ->
            // 기존 매핑 로직 유지
        }
    }
}
```

### 3. MovieService 수정

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

### 4. QueryDSL을 사용한 동적 쿼리 구현

```kotlin
@Repository
class MovieRepositoryCustomImpl(
    private val queryFactory: JPAQueryFactory
) {
    fun findNowPlayingMoviesWithFilters(title: String?, genre: String?): List<Movie> {
        val movie = QMovie.movie
        
        val query = queryFactory
            .selectFrom(movie)
            .where(movie.releaseDate.loe(LocalDate.now()))
        
        // 동적 조건 추가
        if (!title.isNullOrBlank()) {
            query.where(movie.title.eq(title)) // 동등 연산자 사용
        }
        
        if (!genre.isNullOrBlank()) {
            query.where(movie.genre.name.eq(genre))
        }
        
        return query.orderBy(movie.releaseDate.desc()).fetch()
    }
}
```

## 인덱스 추가

검색 성능을 향상시키기 위해 필요한 인덱스를 추가했습니다.

### 1. 인덱스 정의 (SQL)

```sql
-- cinema-infrastructure/src/main/resources/db/migration/V2__add_indexes.sql
-- 영화 테이블에 인덱스 추가
CREATE INDEX idx_movie_title ON movie (title);
CREATE INDEX idx_movie_genre_name ON movie_genre (name);
CREATE INDEX idx_movie_release_date ON movie (release_date);
```

### 2. 인덱스 적용 방법

데이터베이스 콘솔에서 직접 실행:

```bash
mysql -u cinema -p cinema < cinema-infrastructure/src/main/resources/db/migration/V2__add_indexes.sql
```

### 3. 실행 계획 확인

인덱스가 적용되었는지 확인하기 위해 실행 계획을 확인합니다:

```sql
-- 인덱스 적용 전 쿼리 실행 계획
EXPLAIN SELECT * FROM movie 
WHERE release_date <= CURRENT_DATE 
ORDER BY release_date DESC;

-- 인덱스 적용 후 title 검색 쿼리 (동등 연산) 실행 계획
EXPLAIN SELECT * FROM movie 
WHERE title = '아바타' AND release_date <= CURRENT_DATE 
ORDER BY release_date DESC;

-- 인덱스 적용 후 genre 검색 쿼리 실행 계획
EXPLAIN SELECT m.* FROM movie m
JOIN movie_genre g ON m.genre_id = g.id
WHERE g.name = '액션' AND m.release_date <= CURRENT_DATE 
ORDER BY m.release_date DESC;
```

## 로컬 캐시(Caffeine) 적용


### 1. 의존성 추가

이미 추가되어 있습니다:
```gradle
// cinema-api/build.gradle.kts
dependencies {
    // Caffeine Cache
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
}
```

### 2. 캐시 설정

```kotlin
// cinema-api/src/main/kotlin/com/hanghae/cinema/api/config/CaffeineCacheConfig.kt
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

### 3. 캐시 적용

```kotlin
// MovieFacade에 캐시 적용
@Cacheable(
    value = ["nowPlayingMovies"], 
    key = "#title?.toString() ?: 'all' + '_' + #genre?.toString() ?: 'all'"
)
fun getNowPlayingMovies(title: String? = null, genre: String? = null): List<MovieResponseDto> {
    // 기존 코드 유지
}
```

### 4. 로컬 캐시 프로필로 애플리케이션 실행

```bash
./gradlew cinema-api:bootRun --args='--spring.profiles.active=local-cache'
```

## 분산 캐시(Redis) 적용

다중 인스턴스 환경에서 일관된 캐싱을 위해 Redis를 적용했습니다.

### 1. 의존성 추가

이미 추가되어 있습니다:
```gradle
// cinema-api/build.gradle.kts
dependencies {
    // Redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.redisson:redisson:${libs.versions.redisson.get()}")
}
```

### 2. Redis 설정

```kotlin
// cinema-api/src/main/kotlin/com/hanghae/cinema/api/config/RedisConfig.kt
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

### 3. Redis Docker 설정

```yaml
# docker-compose.yml에 Redis 추가
version: '3.8'

services:
  mysql:
    # 기존 설정 유지
      
  redis:
    image: redis:7-alpine
    container_name: cinema-redis
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data

volumes:
  mysql-data:
  redis-data:
```

### 4. Redis 실행

```bash
docker-compose up -d redis
```

### 5. Redis 캐시 프로필로 애플리케이션 실행

```bash
./gradlew cinema-api:bootRun --args='--spring.profiles.active=redis-cache'
```

## 성능 테스트 실행

K6를 사용하여 성능 테스트를 실행하는 방법을 설명합니다.

### 1. K6 설치

macOS:
```bash
brew install k6
```

Linux:
```bash
sudo apt-get install k6
```

Docker:
```bash
docker pull grafana/k6
```

### 2. 테스트 스크립트 작성

```javascript
// k6-scripts/movie-api-test.js
import http from 'k6/http';
import { sleep, check } from 'k6';

export const options = {
  stages: [
    { duration: '1m', target: 100 }, // 1분 동안 100명까지 증가
    { duration: '3m', target: 100 }, // 3분 동안 100명 유지
    { duration: '1m', target: 0 },   // 1분 동안 0명까지 감소
  ],
  thresholds: {
    http_req_duration: ['p(95)<200'], // 95%의 요청이 200ms 이하
    http_req_failed: ['rate<0.01'],   // 실패율 1% 미만
  },
};

export default function () {
  const BASE_URL = 'http://localhost:8080';
  
  // 랜덤 검색어 생성 (10%의 확률로 검색어 사용)
  const useSearchParam = Math.random() < 0.1;
  let url = `${BASE_URL}/api/movies`;
  
  if (useSearchParam) {
    // 미리 정의된 영화 제목 샘플 중 하나 선택
    const movieTitles = ['아바타', '인터스텔라', '매트릭스', '터미네이터'];
    const randomTitle = movieTitles[Math.floor(Math.random() * movieTitles.length)];
    url += `?title=${randomTitle}`;
  }
  
  const res = http.get(url);
  
  check(res, {
    'status is 200': (r) => r.status === 200,
    'response time < 200ms': (r) => r.timings.duration < 200,
  });
  
  sleep(Math.random() * 3); // 1~3초 대기 (사용자 행동 시뮬레이션)
}
```

### 3. 테스트 디렉토리 생성

```bash
mkdir -p k6-scripts
```

### 4. 테스트 스크립트 저장

```bash
# 위의 자바스크립트 코드를 k6-scripts/movie-api-test.js 파일에 저장
```

### 5. 테스트 실행

각 단계별로 다음과 같이 테스트를 실행합니다:

**원본 API 테스트**:
```bash
k6 run k6-scripts/movie-api-test.js
```

**검색 기능 추가 후 테스트**:
```bash
k6 run k6-scripts/movie-api-test.js
```

**인덱스 추가 후 테스트**:
```bash
mysql -u cinema -p cinema < cinema-infrastructure/src/main/resources/db/migration/V2__add_indexes.sql
k6 run k6-scripts/movie-api-test.js
```

**로컬 캐시 적용 후 테스트**:
```bash
./gradlew cinema-api:bootRun --args='--spring.profiles.active=local-cache'
k6 run k6-scripts/movie-api-test.js
```

**분산 캐시 적용 후 테스트**:
```bash
docker-compose up -d redis
./gradlew cinema-api:bootRun --args='--spring.profiles.active=redis-cache'
k6 run k6-scripts/movie-api-test.js
```

## 성능 테스트 보고서 작성

각 단계별 성능 테스트 결과를 바탕으로 다음과 같은 형식으로 보고서를 작성합니다:

### 1. 테스트 환경 설명

- 하드웨어: MacBook Pro M1 (16GB RAM) 등
- 소프트웨어 버전: Spring Boot 3.2.0, Kotlin 1.9.21, PostgreSQL 15 등

### 2. 테스트 시나리오

- 가상 사용자 수: 100명
- 테스트 시간: 5분
- 요청 패턴: 90%는 전체 목록 조회, 10%는 특정 영화 검색

### 3. 단계별 측정 결과

각 단계마다 다음 지표를 측정하고 비교합니다:

- **응답 시간**: 평균, p95, p99
- **처리량(RPS)**: 초당 처리 요청 수
- **오류율**: 요청 실패 비율

### 4. 개선 효과 분석

각 최적화 단계의 효과와 개선율을 분석합니다:

- 인덱스 적용으로 인한 성능 향상
- 로컬 캐시(Caffeine) 적용 효과
- 분산 캐시(Redis) 적용 효과

### 5. 결론 및 제언

테스트 결과를 종합하여 결론을 도출하고, 추가 개선 가능성을 제안합니다. 