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