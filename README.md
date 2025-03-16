# 항해 시네마 프로젝트

## 프로젝트 소개
'항해 시네마'는 '모형물 배 = 항해호'에 탑승하여, 최신 및 재개봉 영화를 관람할 수 있는 서비스입니다.

## 모듈 구성 및 책임
이 프로젝트는 Clean + Layered Architecture를 기반으로 설계되었으며, 다음과 같은 패키지 구조를 가집니다:

1. **interfaces**: 외부 요청을 처리하는 Presentation Layer
   - API 요청 처리 및 응답 반환
   - DTO 변환 및 유효성 검증

2. **application**: 비즈니스 유스케이스를 조합하는 Application Layer
   - 여러 도메인 서비스를 조합하여 복잡한 비즈니스 로직 처리
   - 트랜잭션 관리

3. **domain**: 핵심 비즈니스 로직과 엔티티를 포함하는 Domain Layer
   - 비즈니스 규칙 및 도메인 로직 구현
   - 도메인 모델 정의

4. **infrastructure**: 데이터베이스 및 외부 서비스 연동을 담당하는 Infrastructure Layer
   - 데이터베이스 접근 구현체
   - 외부 서비스 연동 구현체

## 테이블 설계 (ERD)

```
+----------------+       +----------------+       +----------------+
|     Movie      |       |     Genre      |       |    Theater     |
+----------------+       +----------------+       +----------------+
| id (PK)        |       | id (PK)        |       | id (PK)        |
| title          |       | name           |       | name           |
| rating         |       | created_at     |       | total_seats    |
| release_date   |       | created_by     |       | created_at     |
| thumbnail_url  |       | updated_at     |       | created_by     |
| running_time   |       | updated_by     |       | updated_at     |
| genre_id (FK)  |       +----------------+       | updated_by     |
| created_at     |                |               +----------------+
| created_by     |                |                      |
| updated_at     |                |                      |
| updated_by     |                |                      |
+----------------+                |                      |
        |                         |                      |
        |                         |                      |
        |                         |                      |
+----------------+                |                      |
|    Schedule    |                |                      |
+----------------+                |                      |
| id (PK)        |                |                      |
| movie_id (FK)  |----------------+                      |
| theater_id (FK)|----------------------------------------+
| start_time     |
| end_time       |
| created_at     |
| created_by     |
| updated_at     |
| updated_by     |
+----------------+
        |
        |
        |
+----------------+
|      Seat      |
+----------------+
| id (PK)        |
| theater_id (FK)|
| seat_number    |
| row            |
| column         |
| created_at     |
| created_by     |
| updated_at     |
| updated_by     |
+----------------+
```

### 테이블 설명
1. **Movie**: 영화 정보를 저장하는 테이블
   - 영화 제목, 등급, 개봉일, 썸네일 URL, 러닝 타임 등의 정보 포함
   - Genre와 다대일 관계

2. **Genre**: 영화 장르 정보를 저장하는 테이블
   - 장르 이름 정보 포함

3. **Theater**: 상영관 정보를 저장하는 테이블
   - 상영관 이름, 총 좌석 수 정보 포함

4. **Schedule**: 상영 시간표 정보를 저장하는 테이블
   - 영화와 상영관 간의 다대다 관계를 해소
   - 상영 시작 시간과 종료 시간 정보 포함

5. **Seat**: 좌석 정보를 저장하는 테이블
   - 상영관에 속한 좌석 정보 포함
   - 좌석 번호, 행, 열 정보 포함

## 아키텍처 설계 상세

### 1. Presentation Layer (interfaces)
- 외부 요청을 처리하고 응답을 반환하는 역할
- Controller, DTO, Exception Handler 등이 포함됨
- 비즈니스 로직에 직접 접근하지 않고 Application Layer를 통해 접근

### 2. Application Layer (application)
- 비즈니스 유스케이스를 조합하는 역할
- Facade 패턴을 사용하여 여러 도메인 서비스를 조합
- 트랜잭션 관리 및 도메인 간 조율을 담당

### 3. Domain Layer (domain)
- 핵심 비즈니스 로직과 엔티티를 포함
- 도메인 모델, 서비스, 레포지토리 인터페이스 등이 포함됨
- 외부 의존성 없이 순수한 비즈니스 로직만 포함

### 4. Infrastructure Layer (infrastructure)
- 데이터베이스 접근, 외부 서비스 연동 등을 담당
- 도메인 레이어에 정의된 레포지토리 인터페이스의 구현체 포함
- JPA, MyBatis 등의 기술적 구현을 캡슐화

## 상영 중인 영화 조회 API 설계

### 요청
```
GET /api/movies/now-playing
```

### 응답
```json
[
  {
    "id": 1,
    "title": "범죄도시4",
    "rating": "15세 이상",
    "releaseDate": "2023-12-01",
    "thumbnailUrl": "https://example.com/thumbnail1.jpg",
    "runningTime": 120,
    "genre": "액션",
    "schedules": [
      {
        "id": 1,
        "theaterName": "1관",
        "startTime": "2023-12-10T10:00:00",
        "endTime": "2023-12-10T12:00:00"
      },
      {
        "id": 2,
        "theaterName": "1관",
        "startTime": "2023-12-10T13:00:00",
        "endTime": "2023-12-10T15:00:00"
      }
    ]
  },
  {
    "id": 2,
    "title": "극한직업",
    "rating": "12세 이상",
    "releaseDate": "2023-11-15",
    "thumbnailUrl": "https://example.com/thumbnail2.jpg",
    "runningTime": 110,
    "genre": "코미디",
    "schedules": [
      {
        "id": 3,
        "theaterName": "2관",
        "startTime": "2023-12-10T11:00:00",
        "endTime": "2023-12-10T12:50:00"
      }
    ]
  }
]
```

## 프로젝트 실행 방법

1. Docker Compose를 사용하여 MySQL 데이터베이스 실행:
```bash
docker-compose up -d
```

2. 애플리케이션 실행:
```bash
./gradlew bootRun
```

3. API 테스트:
   - IntelliJ HTTP Client를 사용하여 `src/test/http/movie.http` 파일의 요청 실행
   - 또는 브라우저에서 `http://localhost:8080/api/movies/now-playing` 접속

## 기술 스택
- Kotlin
- Spring Boot 3.x
- Spring Data JPA
- MySQL
- Docker Compose