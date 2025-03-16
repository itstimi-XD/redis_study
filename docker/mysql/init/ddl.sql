CREATE TABLE IF NOT EXISTS genres (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    created_at DATETIME NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    updated_at DATETIME NOT NULL,
    updated_by VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS movies (
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

CREATE TABLE IF NOT EXISTS theaters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    total_seats INT NOT NULL,
    created_at DATETIME NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    updated_at DATETIME NOT NULL,
    updated_by VARCHAR(50) NOT NULL
);

CREATE TABLE IF NOT EXISTS schedules (
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

CREATE TABLE IF NOT EXISTS seats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    theater_id BIGINT NOT NULL,
    seat_number VARCHAR(10) NOT NULL,
    row CHAR(1) NOT NULL,
    column_num INT NOT NULL,
    created_at DATETIME NOT NULL,
    created_by VARCHAR(50) NOT NULL,
    updated_at DATETIME NOT NULL,
    updated_by VARCHAR(50) NOT NULL,
    FOREIGN KEY (theater_id) REFERENCES theaters(id)
);

-- 샘플 데이터 삽입
INSERT INTO genres (name, created_at, created_by, updated_at, updated_by) VALUES
('액션', NOW(), 'system', NOW(), 'system'),
('코미디', NOW(), 'system', NOW(), 'system'),
('드라마', NOW(), 'system', NOW(), 'system'),
('공포', NOW(), 'system', NOW(), 'system'),
('SF', NOW(), 'system', NOW(), 'system');

INSERT INTO movies (title, rating, release_date, thumbnail_url, running_time, genre_id, created_at, created_by, updated_at, updated_by) VALUES
('범죄도시4', '15세 이상', '2023-12-01', 'https://example.com/thumbnail1.jpg', 120, 1, NOW(), 'system', NOW(), 'system'),
('극한직업', '12세 이상', '2023-11-15', 'https://example.com/thumbnail2.jpg', 110, 2, NOW(), 'system', NOW(), 'system'),
('기생충', '15세 이상', '2023-10-20', 'https://example.com/thumbnail3.jpg', 132, 3, NOW(), 'system', NOW(), 'system'),
('신세계', '청소년 관람불가', '2023-09-05', 'https://example.com/thumbnail4.jpg', 135, 1, NOW(), 'system', NOW(), 'system'),
('인터스텔라', '12세 이상', '2023-08-10', 'https://example.com/thumbnail5.jpg', 169, 5, NOW(), 'system', NOW(), 'system');

INSERT INTO theaters (name, total_seats, created_at, created_by, updated_at, updated_by) VALUES
('1관', 25, NOW(), 'system', NOW(), 'system'),
('2관', 25, NOW(), 'system', NOW(), 'system'),
('3관', 25, NOW(), 'system', NOW(), 'system');

-- 각 상영관에 좌석 생성 (5x5 형태)
INSERT INTO seats (theater_id, seat_number, row, column_num, created_at, created_by, updated_at, updated_by)
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
    (SELECT 1 AS r UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5) AS rows,
    (SELECT 1 AS c UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5) AS cols,
    theaters t;

-- 상영 시간표 생성 (500개 이상의 데이터)
-- 현재 날짜부터 7일간의 상영 일정을 생성
INSERT INTO schedules (movie_id, theater_id, start_time, end_time, created_at, created_by, updated_at, updated_by)
SELECT 
    m.id,
    t.id,
    DATE_ADD(CURRENT_DATE(), INTERVAL d DAY) + INTERVAL h HOUR + INTERVAL (m.id * 10 % 60) MINUTE,
    DATE_ADD(CURRENT_DATE(), INTERVAL d DAY) + INTERVAL h HOUR + INTERVAL (m.id * 10 % 60) MINUTE + INTERVAL m.running_time MINUTE,
    NOW(),
    'system',
    NOW(),
    'system'
FROM 
    movies m,
    theaters t,
    (SELECT 0 AS d UNION SELECT 1 UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6) AS days,
    (SELECT 10 AS h UNION SELECT 13 UNION SELECT 16 UNION SELECT 19 UNION SELECT 22) AS hours
WHERE 
    m.release_date <= DATE_ADD(CURRENT_DATE(), INTERVAL d DAY)
ORDER BY 
    m.id, t.id, d, h
LIMIT 500; 