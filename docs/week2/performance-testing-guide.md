# 항해 시네마 성능 테스트 수행 가이드

이 문서는 항해 시네마 프로젝트의 성능 테스트를 단계별로 수행하는 방법을 설명합니다.

## 목차
- [사전 준비](#사전-준비)
- [0. 1주차 API 테스트](#0-1주차-api-테스트)
- [1. 인덱스 적용 전 테스트](#1-인덱스-적용-전-테스트)
- [2. 인덱스 적용 후 테스트](#2-인덱스-적용-후-테스트)
- [3. 로컬 캐시 적용 후 테스트](#3-로컬-캐시-적용-후-테스트)
- [4. 분산 캐시 적용 후 테스트](#4-분산-캐시-적용-후-테스트)
- [문제 해결](#문제-해결)

## 사전 준비

### 1. K6 설치
```bash
# macOS
brew install k6

# Linux
sudo apt-get install k6

# Windows (with chocolatey)
choco install k6
```

### 2. 테스트 데이터 준비
```bash
# MySQL 실행
docker-compose up -d mysql

# 데이터베이스 접속 및 데이터 확인
mysql -u cinema -p cinema
> SELECT COUNT(*) FROM movies;  # 500개 이상 있어야 함
> SELECT COUNT(*) FROM schedules;  # 충분한 수의 스케줄 확인
```

## 0. 1주차 API 테스트

### 0-1. 1주차 코드 체크아웃
```bash
# 1주차 브랜치로 체크아웃 혹은 해당하는 커밋으로 체크아웃
git checkout week1
```

### 0-2. 애플리케이션 실행 (dev 프로필)
```bash
./gradlew cinema-api:bootRun --args='--spring.profiles.active=dev'
```

### 0-3. K6 부하 테스트 실행
```bash
# 새 터미널에서 K6 테스트 실행
k6 run k6-scripts/movie-api-test.js
```

### 0-4. 테스트 결과 수집
K6 테스트 결과에서 다음 정보를 복사하여 보고서에 붙여넣습니다:
- http_req_duration (평균)
- http_req_duration (p95)
- 처리량(RPS)
- 실패율

### 0-5. 2주차 코드로 돌아오기
```bash
# 2주차 브랜치로 체크아웃 혹은 해당하는 커밋으로 체크아웃
git checkout week2
```

## 1. 인덱스 적용 전 테스트

### 1-1. 쿼리 실행 계획 확인
```bash
mysql -u cinema -p cinema
```

```sql
EXPLAIN SELECT m.* FROM movies m
WHERE m.release_date <= CURRENT_DATE
ORDER BY m.release_date DESC;
```

실행 계획 결과를 복사하여 보고서에 붙여넣기 합니다.

### 1-2. 애플리케이션 실행 (dev 프로필)
```bash
./gradlew cinema-api:bootRun --args='--spring.profiles.active=dev'
```

### 1-3. K6 부하 테스트 실행
```bash
# 새 터미널에서 K6 테스트 실행
k6 run k6-scripts/movie-api-test.js
```

### 1-4. 테스트 결과 수집
K6 테스트 결과에서 다음 정보를 복사하여 보고서에 붙여넣습니다:
- http_req_duration (평균)
- http_req_duration (p95)
- 처리량(RPS)
- 실패율

스크린샷도 함께 저장합니다:
- Mac: `Cmd + Shift + 4`
- Windows: `Win + Shift + S`

## 2. 인덱스 적용 후 테스트

### 2-1. 인덱스 적용
```
CREATE INDEX idx_movie_title ON movies (title);
CREATE INDEX idx_movie_release_date ON movies (release_date);
```

### 2-2. 인덱스 생성 확인
```bash
mysql -u cinema -p cinema
```

```sql
SHOW INDEX FROM movies;
SHOW INDEX FROM genres;
```

### 2-3. 실행 계획 다시 확인
```sql
-- 일반 조회 쿼리
EXPLAIN SELECT m.* FROM movies m
WHERE m.release_date <= CURRENT_DATE
ORDER BY m.release_date DESC;

-- 제목 검색
EXPLAIN SELECT m.* FROM movies m
WHERE m.title = '아바타' AND m.release_date <= CURRENT_DATE
ORDER BY m.release_date DESC;

-- 장르 검색
EXPLAIN SELECT m.* FROM movies m
JOIN genres g ON m.genre_id = g.id
WHERE g.name = '액션' AND m.release_date <= CURRENT_DATE
ORDER BY m.release_date DESC;
```

### 2-4. 애플리케이션 재시작
```bash
./gradlew cinema-api:bootRun --args='--spring.profiles.active=dev'
```

### 2-5. K6 부하 테스트 재실행
```bash
k6 run k6-scripts/movie-api-test.js
```
### 2-6. 테스트 결과 수집
K6 테스트 결과에서 다음 정보를 복사하여 보고서에 붙여넣습니다:
- http_req_duration (평균)
- http_req_duration (p95)
- 처리량(RPS)
- 실패율

스크린샷도 함께 저장합니다:
- Mac: `Cmd + Shift + 4`
- Windows: `Win + Shift + S`

## 3. 로컬 캐시 적용 후 테스트

### 3-1. 로컬 캐시 프로필로 애플리케이션 실행
```bash
./gradlew cinema-api:bootRun --args='--spring.profiles.active=dev,local-cache'
```

### 3-2. K6 부하 테스트 실행
```bash
k6 run k6-scripts/movie-api-test.js
```

### 3-3. 테스트 결과 수집
K6 테스트 결과에서 다음 정보를 복사하여 보고서에 붙여넣습니다:
- http_req_duration (평균)
- http_req_duration (p95)
- 처리량(RPS)
- 실패율

스크린샷도 함께 저장합니다:
- Mac: `Cmd + Shift + 4`
- Windows: `Win + Shift + S`

## 4. 분산 캐시 적용 후 테스트

### 4-1. Redis 실행
```bash
docker-compose up -d redis
```

### 4-2. Redis 상태 확인
```bash
redis-cli ping
```
PONG이 반환되면 Redis가 정상적으로 실행 중입니다.

### 4-3. Redis 캐시 프로필로 애플리케이션 실행
```bash
./gradlew cinema-api:bootRun --args='--spring.profiles.active=dev,redis-cache'
```

### 4-4. K6 부하 테스트 실행
```bash
k6 run k6-scripts/movie-api-test.js
```
### 4-5. 테스트 결과 수집
K6 테스트 결과에서 다음 정보를 복사하여 보고서에 붙여넣습니다:
- http_req_duration (평균)
- http_req_duration (p95)
- 처리량(RPS)
- 실패율

스크린샷도 함께 저장합니다:
- Mac: `Cmd + Shift + 4`
- Windows: `Win + Shift + S`

## 문제 해결

### 문제: Redis 연결 실패
**확인 사항**:
```bash
# Redis 컨테이너 상태 확인
docker ps | grep redis

# Redis 연결 테스트
redis-cli ping
```

**해결방법**: Redis 컨테이너가 실행 중이 아니라면 다시 시작하세요:
```bash
docker-compose up -d redis
```

### 문제: K6 테스트 실패
**확인 사항**:
```bash
# 애플리케이션 로그 확인
./gradlew cinema-api:bootRun --args='--spring.profiles.active=dev' --debug
```

**해결방법**: 애플리케이션이 올바른 포트(8080)에서 실행 중인지 확인하고, K6 스크립트의 BASE_URL이 올바르게 설정되어 있는지 확인하세요. 