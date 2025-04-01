-- 영화 데이터
INSERT INTO movies (id, title, duration) VALUES
(1, '테스트 영화 1', 120),
(2, '테스트 영화 2', 150);

-- 극장 데이터
INSERT INTO theaters (id, name) VALUES
(1, '테스트 극장 1'),
(2, '테스트 극장 2');

-- 상영 일정 데이터
INSERT INTO schedules (id, movie_id, theater_id, start_time, end_time) VALUES
(1, 1, 1, CURRENT_TIMESTAMP + INTERVAL 1 DAY, CURRENT_TIMESTAMP + INTERVAL 1 DAY + INTERVAL 2 HOUR),
(2, 2, 2, CURRENT_TIMESTAMP + INTERVAL 2 DAY, CURRENT_TIMESTAMP + INTERVAL 2 DAY + INTERVAL 2 HOUR);

-- 좌석 데이터
INSERT INTO seats (id, theater_id, row_name, number) VALUES
(1, 1, 'A', 1),
(2, 1, 'A', 2),
(3, 1, 'A', 3),
(4, 1, 'A', 4),
(5, 1, 'A', 5),
(6, 2, 'A', 1),
(7, 2, 'A', 2),
(8, 2, 'A', 3); 