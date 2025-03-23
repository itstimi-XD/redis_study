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

## 1. Index 적용 전

### 현재 생성된 인덱스 확인
```
mysql> SELECT * FROM information_schema.statistics WHERE table_schema = 'cinema' AND table_name = 'movies';
+---------------+--------------+------------+------------+--------------+--------------------+--------------+-------------+-----------+-------------+----------+--------+----------+------------+---------+---------------+------------+------------+
| TABLE_CATALOG | TABLE_SCHEMA | TABLE_NAME | NON_UNIQUE | INDEX_SCHEMA | INDEX_NAME         | SEQ_IN_INDEX | COLUMN_NAME | COLLATION | CARDINALITY | SUB_PART | PACKED | NULLABLE | INDEX_TYPE | COMMENT | INDEX_COMMENT | IS_VISIBLE | EXPRESSION |
+---------------+--------------+------------+------------+--------------+--------------------+--------------+-------------+-----------+-------------+----------+--------+----------+------------+---------+---------------+------------+------------+
| def           | cinema       | movies     |          1 | cinema       | idx_movie_genre_id |            1 | genre_id    | A         |          10 |     NULL |   NULL |          | BTREE      |         |               | YES        | NULL       |
| def           | cinema       | movies     |          0 | cinema       | PRIMARY            |            1 | id          | A         |         500 |     NULL |   NULL |          | BTREE      |         |               | YES        | NULL       |
+---------------+--------------+------------+------------+--------------+--------------------+--------------+-------------+-----------+-------------+----------+--------+----------+------------+---------+---------------+------------+------------+
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
+----+-------------+-------+------------+------+---------------+------+---------+------+------+----------+-----------------------------+
| id | select_type | table | partitions | type | possible_keys | key  | key_len | ref  | rows | filtered | Extra                       |
+----+-------------+-------+------------+------+---------------+------+---------+------+------+----------+-----------------------------+
|  1 | SIMPLE      | m     | NULL       | ALL  | NULL          | NULL | NULL    | NULL |  500 |    33.33 | Using where; Using filesort |
+----+-------------+-------+------------+------+---------------+------+---------+------+------+----------+-----------------------------+
1 row in set, 1 warning (0.01 sec)
```

### 부하 테스트 결과
![인덱스 적용 전 부하 테스트 결과](https://prod-files-secure.s3.us-west-2.amazonaws.com/83c75a39-3aba-4ba4-a792-7aefe4b07895/5dd18c3c-42c0-4461-99e0-0b90751ba95b/Screenshot_2024-12-09_at_15.20.09.png)

**주요 지표**:
- **http_req_duration (평균)**: avg=11.65s

- **http_req_duration (p95)**: p(95)=18.47s

- **처리량(RPS)**: http_reqs: 1869 요청 / 실행시간 302.6초 (5분 2.6초) -> 약 6.17 RPS

- **실패율**: 10.32% (목표 1% 미만이었으나 10% 넘음)


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
+----+-------------+-------+------------+------+------------------------+------+---------+------+------+----------+-----------------------------+
| id | select_type | table | partitions | type | possible_keys          | key  | key_len | ref  | rows | filtered | Extra                       |
+----+-------------+-------+------------+------+------------------------+------+---------+------+------+----------+-----------------------------+
|  1 | SIMPLE      | m     | NULL       | ALL  | idx_movie_release_date | NULL | NULL    | NULL |  500 |   100.00 | Using where; Using filesort |
+----+-------------+-------+------------+------+------------------------+------+---------+------+------+----------+-----------------------------+
1 row in set, 1 warning (0.02 sec)
```

**제목 검색 쿼리 실행 계획 (동등 연산)**:
```
+----+-------------+-------+------------+------+----------------------------------------+-----------------+---------+-------+------+----------+-----------------------------+
| id | select_type | table | partitions | type | possible_keys                          | key             | key_len | ref   | rows | filtered | Extra                       |
+----+-------------+-------+------------+------+----------------------------------------+-----------------+---------+-------+------+----------+-----------------------------+
|  1 | SIMPLE      | m     | NULL       | ref  | idx_movie_title,idx_movie_release_date | idx_movie_title | 1022    | const |    1 |   100.00 | Using where; Using filesort |
+----+-------------+-------+------------+------+----------------------------------------+-----------------+---------+-------+------+----------+-----------------------------+
1 row in set, 1 warning (0.00 sec)
```

**장르 검색 쿼리 실행 계획**:
```
+----+-------------+-------+------------+------+-------------------------------------------+--------------------+---------+-------------+------+----------+----------------------------------------------+
| id | select_type | table | partitions | type | possible_keys                             | key                | key_len | ref         | rows | filtered | Extra                                        |
+----+-------------+-------+------------+------+-------------------------------------------+--------------------+---------+-------------+------+----------+----------------------------------------------+
|  1 | SIMPLE      | g     | NULL       | ALL  | PRIMARY                                   | NULL               | NULL    | NULL        |   26 |    10.00 | Using where; Using temporary; Using filesort |
|  1 | SIMPLE      | m     | NULL       | ref  | idx_movie_genre_id,idx_movie_release_date | idx_movie_genre_id | 8       | cinema.g.id |   50 |   100.00 | Using where                                  |
+----+-------------+-------+------------+------+-------------------------------------------+--------------------+---------+-------------+------+----------+----------------------------------------------+
2 rows in set, 1 warning (0.25 sec)
```

### 부하 테스트 결과

![인덱스 적용 후 부하 테스트 결과](https://prod-files-secure.s3.us-west-2.amazonaws.com/83c75a39-3aba-4ba4-a792-7aefe4b07895/5dd18c3c-42c0-4461-99e0-0b90751ba95b/Screenshot_2024-12-09_at_15.20.09.png)

**주요 지표**:
- **http_req_duration (평균)**: 11.86s
- **http_req_duration (p95)**: 19.18s
- **처리량(RPS)**: 6.07/s
- **실패율**: 10.68%
