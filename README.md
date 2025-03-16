# 항해 시네마 프로젝트

## 프로젝트 소개
'항해 시네마'는 '모형물 배 = 항해호'에 탑승하여, 최신 및 재개봉 영화를 관람할 수 있는 서비스입니다.

## 모듈 구성 및 책임
이 프로젝트는 Clean + Layered Architecture를 기반으로 설계되었으며, 다음과 같은 모듈 구조를 가집니다:

1. **cinema-api**: 외부 요청을 처리하는 Presentation Layer
   - API 요청 처리 및 응답 반환
   - 컨트롤러 및 예외 처리

2. **cinema-application**: 비즈니스 유스케이스를 조합하는 Application Layer
   - 여러 도메인 서비스를 조합하여 복잡한 비즈니스 로직 처리
   - Facade 패턴을 사용하여 도메인 서비스 조합
   - DTO 변환 및 트랜잭션 관리

3. **cinema-domain**: 핵심 비즈니스 로직과 엔티티를 포함하는 Domain Layer
   - 비즈니스 규칙 및 도메인 로직 구현
   - 도메인 모델 및 레포지토리 인터페이스 정의

4. **cinema-infrastructure**: 데이터베이스 및 외부 서비스 연동을 담당하는 Infrastructure Layer
   - 데이터베이스 접근 구현체
   - 레포지토리 인터페이스 구현

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
   - 하나의 영화는 하나의 장르만 가짐

2. **Genre**: 영화 장르 정보를 저장하는 테이블
   - 장르 이름 정보 포함

3. **Theater**: 상영관 정보를 저장하는 테이블
   - 상영관 이름, 총 좌석 수 정보 포함
   - 모든 상영관은 5x5 형태의 좌석 구조를 가짐

4. **Schedule**: 상영 시간표 정보를 저장하는 테이블
   - 영화와 상영관 간의 다대다 관계를 해소
   - 상영 시작 시간과 종료 시간 정보 포함
   - 상영 시간표는 최소 1~2일 전에 등록됨
   - 상영 시간표에 등록된 영화의 개봉일은 상영 시간 날짜보다 이전이어야 함

5. **Seat**: 좌석 정보를 저장하는 테이블
   - 상영관에 속한 좌석 정보 포함
   - 좌석 번호(A1, B2 등), 행(A-E), 열(1-5) 정보 포함

## 아키텍처 설계 상세

### Clean + Layered Architecture
이 프로젝트는 Clean Architecture와 Layered Architecture를 결합한 형태로 설계되었습니다. 이 아키텍처는 다음과 같은 특징을 가집니다:

- 애플리케이션의 핵심은 비즈니스 로직
- 데이터 계층 및 API 계층이 비즈니스 로직을 의존 (비즈니스의 Interface 활용)
- 도메인 중심적인 계층 아키텍처
- Presentation은 도메인을 API로 서빙, DataSource는 도메인이 필요로 하는 기능을 서빙
- DIP(의존성 역전 원칙)와 OCP(개방-폐쇄 원칙) 준수

### 레이어별 책임
1. **Presentation Layer (cinema-api)**
   - 외부 요청을 처리하고 응답을 반환하는 역할
   - Controller, Exception Handler 등이 포함됨
   - 비즈니스 로직에 직접 접근하지 않고 Application Layer를 통해 접근

2. **Application Layer (cinema-application)**
   - 비즈니스 유스케이스를 조합하는 역할
   - Facade 패턴을 사용하여 여러 도메인 서비스를 조합
   - 트랜잭션 관리 및 도메인 간 조율을 담당

3. **Domain Layer (cinema-domain)**
   - 핵심 비즈니스 로직과 엔티티를 포함
   - 도메인 모델, 서비스, 레포지토리 인터페이스 등이 포함됨
   - 외부 의존성 없이 순수한 비즈니스 로직만 포함

4. **Infrastructure Layer (cinema-infrastructure)**
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
  }
]
```

### 응답 설명
- 영화 정보: 제목, 등급, 개봉일, 썸네일 URL, 러닝 타임, 장르
- 상영 시간표: 상영관 이름, 상영 시작/종료 시간
- 정렬 기준: 개봉일 기준 내림차순 (최신순), 시간표는 시작 시간 기준 오름차순

## 기술적 요구사항

1. **N+1 문제 해결**
   - 영화와 상영 시간표 조회 시 N+1 문제가 발생하지 않도록 설계
   - 적절한 Fetch Join 사용

2. **데이터베이스 설정**
   - Docker Compose를 통한 MySQL 데이터베이스 구성
   - DDL 자동 생성을 위한 ddl.sql 파일 관리

3. **테스트**
   - IntelliJ HTTP Client를 이용한 API 테스트
   - 단위 테스트 및 통합 테스트 작성

4. **데이터 초기화**
   - 상영 테이블에 500개 이상의 데이터 생성
   - 테스트를 위한 충분한 샘플 데이터 제공

## 프로젝트 실행 방법

1. 애플리케이션 실행:
```bash
./gradlew :cinema-api:bootRun --args='--spring.profiles.active=test'
```

2. API 테스트:
   - Swagger UI를 사용하여 API 테스트: `http://localhost:8080/swagger-ui.html`
   - 또는 브라우저에서 `http://localhost:8080/api/movies` 접속

## 기술 스택
- Kotlin
- Spring Boot 3.x
- Spring Data JPA
- PostgreSQL (with TestContainers)
- Docker
- Swagger/OpenAPI

## 데이터베이스 설정

이 프로젝트는 TestContainers를 사용하여 PostgreSQL 데이터베이스를 자동으로 실행합니다. 
별도의 데이터베이스 설치나 Docker Compose 설정이 필요하지 않습니다.

테스트 프로필(`test`)로 애플리케이션을 실행하면 다음과 같은 작업이 자동으로 수행됩니다:
1. TestContainers가 PostgreSQL Docker 컨테이너를 시작
2. 스키마가 자동으로 생성됨 (JPA의 `hibernate.ddl-auto=create-drop` 설정 사용)
3. `DataInitializer` 클래스에 의해 초기 데이터가 자동으로 삽입됨

## 초기 데이터

애플리케이션 시작 시 다음과 같은 초기 데이터가 자동으로 생성됩니다:
- 장르: 액션, 코미디, 드라마, SF
- 영화: 어벤져스: 엔드게임, 기생충, 인터스텔라
- 상영관: 1관, 2관, 3관

이 데이터는 `DataInitializer` 클래스에서 관리되며, 필요에 따라 수정할 수 있습니다.