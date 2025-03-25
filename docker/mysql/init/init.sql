-- 데이터 초기화
SET FOREIGN_KEY_CHECKS=0;
DROP TABLE IF EXISTS schedules;
DROP TABLE IF EXISTS seats;
DROP TABLE IF EXISTS theaters;
DROP TABLE IF EXISTS movies;
DROP TABLE IF EXISTS genres;
SET FOREIGN_KEY_CHECKS=1;

-- 테이블 생성
CREATE TABLE genres (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    created_at DATETIME NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    updated_at DATETIME NOT NULL,
    updated_by VARCHAR(50) NOT NULL
);

CREATE TABLE movies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    rating VARCHAR(20) NOT NULL,
    release_date DATE NOT NULL,
    thumbnail_url VARCHAR(255) NOT NULL,
    running_time INT NOT NULL,
    genre_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    updated_at DATETIME NOT NULL,
    updated_by VARCHAR(50) NOT NULL,
    FOREIGN KEY (genre_id) REFERENCES genres(id)
);

CREATE TABLE theaters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    total_seats INT NOT NULL,
    created_at DATETIME NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    updated_at DATETIME NOT NULL,
    updated_by VARCHAR(50) NOT NULL
);

CREATE TABLE schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    movie_id BIGINT NOT NULL,
    theater_id BIGINT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    created_at DATETIME NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    updated_at DATETIME NOT NULL,
    updated_by VARCHAR(50) NOT NULL,
    FOREIGN KEY (movie_id) REFERENCES movies(id),
    FOREIGN KEY (theater_id) REFERENCES theaters(id)
);

CREATE TABLE seats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    theater_id BIGINT NOT NULL,
    seat_number VARCHAR(10) NOT NULL,
    seat_row CHAR(1) NOT NULL,
    column_num INT NOT NULL,
    created_at DATETIME NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    updated_at DATETIME NOT NULL,
    updated_by VARCHAR(50) NOT NULL,
    FOREIGN KEY (theater_id) REFERENCES theaters(id)
);

-- 영화 테이블에 인덱스 추가
CREATE INDEX idx_movie_title ON movies (title);
CREATE INDEX idx_movie_genre_id ON movies (genre_id);
CREATE INDEX idx_movie_release_date ON movies (release_date);

-- -- 기존 인덱스 명시적 삭제
-- DROP INDEX IF EXISTS idx_movie_title ON movies;
-- DROP INDEX IF EXISTS idx_movie_genre_id ON movies;
-- DROP INDEX IF EXISTS idx_movie_release_date ON movies;
-- 장르 데이터 추가
INSERT INTO genres (name, created_at, created_by, updated_at, updated_by) VALUES
('액션', NOW(), 'system', NOW(), 'system'),
('코미디', NOW(), 'system', NOW(), 'system'),
('드라마', NOW(), 'system', NOW(), 'system'),
('SF', NOW(), 'system', NOW(), 'system'),
('공포', NOW(), 'system', NOW(), 'system'),
('스릴러', NOW(), 'system', NOW(), 'system'),
('판타지', NOW(), 'system', NOW(), 'system'),
('로맨스', NOW(), 'system', NOW(), 'system'),
('애니메이션', NOW(), 'system', NOW(), 'system'),
('다큐멘터리', NOW(), 'system', NOW(), 'system');

-- 영화 500개 데이터 생성
-- 기본 영화 10개 추가
INSERT INTO movies (title, rating, release_date, thumbnail_url, running_time, genre_id, created_at, created_by, updated_at, updated_by) VALUES
('어벤져스: 엔드게임', '12세 이상', '2019-04-24', 'https://example.com/avengers.jpg', 181, 1, NOW(), 'system', NOW(), 'system'),
('기생충', '15세 이상', '2019-05-30', 'https://example.com/parasite.jpg', 132, 3, NOW(), 'system', NOW(), 'system'),
('인터스텔라', '12세 이상', '2014-11-06', 'https://example.com/interstellar.jpg', 169, 4, NOW(), 'system', NOW(), 'system'),
('아바타', '12세 이상', '2009-12-17', 'https://example.com/avatar.jpg', 162, 4, NOW(), 'system', NOW(), 'system'),
('매트릭스', '15세 이상', '1999-05-15', 'https://example.com/matrix.jpg', 136, 1, NOW(), 'system', NOW(), 'system'),
('터미네이터', '15세 이상', '1984-10-26', 'https://example.com/terminator.jpg', 108, 1, NOW(), 'system', NOW(), 'system'),
('겨울왕국', '전체관람가', '2013-12-19', 'https://example.com/frozen.jpg', 102, 9, NOW(), 'system', NOW(), 'system'),
('타이타닉', '12세 이상', '1998-02-20', 'https://example.com/titanic.jpg', 194, 8, NOW(), 'system', NOW(), 'system'),
('반지의 제왕', '12세 이상', '2001-12-19', 'https://example.com/lotr.jpg', 178, 7, NOW(), 'system', NOW(), 'system'),
('조커', '15세 이상', '2019-10-02', 'https://example.com/joker.jpg', 122, 6, NOW(), 'system', NOW(), 'system');

-- 나머지 490개 영화 생성 (기존 영화 제목에 번호를 붙여서)
DELIMITER $$
CREATE PROCEDURE generate_movies()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE j INT DEFAULT 0;
    DECLARE movie_title VARCHAR(255);
    DECLARE base_title VARCHAR(255);
    DECLARE rating VARCHAR(20);
    DECLARE genre INT;
    DECLARE running_time INT;
    DECLARE release_year INT;
    DECLARE release_month INT;
    DECLARE release_day INT;
    
    WHILE i <= 490 DO
        SET j = (i % 10) + 1;
        
        -- 기준 영화 선택
        CASE j
            WHEN 1 THEN SET base_title = '어벤져스', rating = '12세 이상', genre = 1, running_time = FLOOR(120 + RAND() * 60);
            WHEN 2 THEN SET base_title = '기생충', rating = '15세 이상', genre = 3, running_time = FLOOR(100 + RAND() * 50);
            WHEN 3 THEN SET base_title = '인터스텔라', rating = '12세 이상', genre = 4, running_time = FLOOR(130 + RAND() * 70);
            WHEN 4 THEN SET base_title = '아바타', rating = '12세 이상', genre = 4, running_time = FLOOR(140 + RAND() * 50);
            WHEN 5 THEN SET base_title = '매트릭스', rating = '15세 이상', genre = 1, running_time = FLOOR(110 + RAND() * 40);
            WHEN 6 THEN SET base_title = '터미네이터', rating = '15세 이상', genre = 1, running_time = FLOOR(100 + RAND() * 30);
            WHEN 7 THEN SET base_title = '겨울왕국', rating = '전체관람가', genre = 9, running_time = FLOOR(90 + RAND() * 30);
            WHEN 8 THEN SET base_title = '타이타닉', rating = '12세 이상', genre = 8, running_time = FLOOR(150 + RAND() * 50);
            WHEN 9 THEN SET base_title = '반지의 제왕', rating = '12세 이상', genre = 7, running_time = FLOOR(150 + RAND() * 40);
            WHEN 10 THEN SET base_title = '조커', rating = '15세 이상', genre = 6, running_time = FLOOR(110 + RAND() * 30);
        END CASE;
        
        -- 랜덤 릴리스 날짜 생성 (2018-2023 사이)
        SET release_year = 2018 + FLOOR(RAND() * 6);
        SET release_month = 1 + FLOOR(RAND() * 12);
        SET release_day = 1 + FLOOR(RAND() * 28);
        
        -- 영화 제목에 번호 붙이기
        SET movie_title = CONCAT(base_title, ' ', (i DIV 10) + 1);
        
        -- 영화 데이터 삽입
        INSERT INTO movies (
            title, 
            rating, 
            release_date, 
            thumbnail_url, 
            running_time, 
            genre_id, 
            created_at, 
            created_by, 
            updated_at, 
            updated_by
        ) VALUES (
            movie_title,
            rating,
            CONCAT(release_year, '-', LPAD(release_month, 2, '0'), '-', LPAD(release_day, 2, '0')),
            CONCAT('https://example.com/', LOWER(base_title), i, '.jpg'),
            running_time,
            ((i % 10) + 1),
            NOW(),
            'system',
            NOW(),
            'system'
        );
        
        SET i = i + 1;
    END WHILE;
END$$
DELIMITER ;

-- 프로시저 실행
CALL generate_movies();
DROP PROCEDURE IF EXISTS generate_movies;

-- 상영관 데이터 추가 (10개)
INSERT INTO theaters (name, total_seats, created_at, created_by, updated_at, updated_by) VALUES
('1관', 100, NOW(), 'system', NOW(), 'system'),
('2관', 120, NOW(), 'system', NOW(), 'system'),
('3관', 150, NOW(), 'system', NOW(), 'system'),
('4관', 80, NOW(), 'system', NOW(), 'system'),
('5관', 100, NOW(), 'system', NOW(), 'system'),
('6관', 200, NOW(), 'system', NOW(), 'system'),
('7관', 120, NOW(), 'system', NOW(), 'system'),
('8관', 100, NOW(), 'system', NOW(), 'system'),
('9관', 80, NOW(), 'system', NOW(), 'system'),
('10관', 150, NOW(), 'system', NOW(), 'system');

-- 상영관별 좌석 생성
INSERT INTO seats (theater_id, seat_number, seat_row, column_num, created_at, created_by, updated_at, updated_by)
SELECT 
    t.id,
    CONCAT(CHAR(64 + r), c),
    CHAR(64 + r),
    c,
    NOW(),
    'system',
    NOW(),
    'system'
FROM 
    theaters t,
    (SELECT 1 AS r UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 
     UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) AS seat_rows,
    (SELECT 1 AS c UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 
     UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10) AS cols
WHERE 
    (r * 10) <= t.total_seats / 10;

-- 상영 스케줄 생성 (최소 500개)
INSERT INTO schedules (movie_id, theater_id, start_time, end_time, created_at, created_by, updated_at, updated_by)
SELECT 
    m.id,
    t.id,
    DATE_ADD(CURRENT_DATE(), INTERVAL d DAY) + INTERVAL h HOUR + INTERVAL (m.id * 7 % 60) MINUTE,
    DATE_ADD(CURRENT_DATE(), INTERVAL d DAY) + INTERVAL h HOUR + INTERVAL (m.id * 7 % 60) MINUTE + INTERVAL m.running_time MINUTE,
    NOW(),
    'system',
    NOW(),
    'system'
FROM 
    (SELECT id, running_time FROM movies ORDER BY RAND() LIMIT 100) m,
    theaters t,
    (SELECT 0 AS d UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6) AS days,
    (SELECT 9 AS h UNION SELECT 12 UNION SELECT 15 UNION SELECT 18 UNION SELECT 21) AS hours
ORDER BY 
    m.id, t.id, d, h
LIMIT 1000; 