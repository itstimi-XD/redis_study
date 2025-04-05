-- 영화관 데이터
INSERT INTO cinema (id, name, location, created_at, created_by, updated_at, updated_by)
VALUES (1, 'Test Cinema', 'Test Location', NOW(), 'system', NOW(), 'system');

-- 장르 데이터
INSERT INTO genres (id, name, created_at, created_by, updated_at, updated_by)
VALUES (1, '액션', NOW(), 'system', NOW(), 'system');

-- 상영관 데이터
INSERT INTO theaters (id, name, cinema_id, total_seats, created_at, created_by, updated_at, updated_by)
VALUES (1, 'Test Theater', 1, 25, NOW(), 'system', NOW(), 'system');

-- 좌석 데이터
INSERT INTO seats (id, theater_id, seat_number, seat_row, column_num, created_at, created_by, updated_at, updated_by)
VALUES 
(1, 1, 'A1', 'A', 1, NOW(), 'system', NOW(), 'system'),
(2, 1, 'A2', 'A', 2, NOW(), 'system', NOW(), 'system'),
(3, 1, 'A3', 'A', 3, NOW(), 'system', NOW(), 'system'),
(4, 1, 'A4', 'A', 4, NOW(), 'system', NOW(), 'system'),
(5, 1, 'A5', 'A', 5, NOW(), 'system', NOW(), 'system'),
(6, 1, 'A6', 'A', 6, NOW(), 'system', NOW(), 'system');

-- 영화 데이터
INSERT INTO movies (id, title, rating, release_date, thumbnail_url, running_time, genre_id, created_at, created_by, updated_at, updated_by)
VALUES (1, 'Test Movie', 'ALL', '2024-03-20', 'http://example.com/thumbnail.jpg', 120, 1, NOW(), 'system', NOW(), 'system');

-- 상영 스케줄 데이터
INSERT INTO schedules (id, movie_id, theater_id, start_time, end_time, created_at, created_by, updated_at, updated_by)
VALUES (1, 1, 1, '2024-03-20 10:00:00', '2024-03-20 12:00:00', NOW(), 'system', NOW(), 'system'); 