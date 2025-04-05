-- Drop tables if they exist
DROP TABLE IF EXISTS reservations;
DROP TABLE IF EXISTS seats;
DROP TABLE IF EXISTS schedules;
DROP TABLE IF EXISTS movies;
DROP TABLE IF EXISTS theaters;
DROP TABLE IF EXISTS genres;
DROP TABLE IF EXISTS cinema;

-- Create cinema table
CREATE TABLE cinema (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(255) NOT NULL,
                        location VARCHAR(255) NOT NULL
);

-- Create genres table
CREATE TABLE genres (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        name VARCHAR(255)
);

-- Create theaters table
CREATE TABLE theaters (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
                          total_seats INTEGER NOT NULL,
                          cinema_id BIGINT,
                          FOREIGN KEY (cinema_id) REFERENCES cinema(id)
);

-- Create movies table
CREATE TABLE movies (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        title VARCHAR(255) NOT NULL,
                        genre_id BIGINT,
                        running_time INT NOT NULL,
                        release_date DATE NOT NULL,
                        rating VARCHAR(10) NOT NULL,
                        thumbnail_url VARCHAR(255),
                        FOREIGN KEY (genre_id) REFERENCES genres(id)
);

-- Create schedules table
CREATE TABLE schedules (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           movie_id BIGINT NOT NULL,
                           theater_id BIGINT NOT NULL,
                           start_time TIMESTAMP NOT NULL,
                           end_time TIMESTAMP NOT NULL,
                           FOREIGN KEY (movie_id) REFERENCES movies(id),
                           FOREIGN KEY (theater_id) REFERENCES theaters(id)
);

-- Create seats table
CREATE TABLE seats (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       theater_id BIGINT NOT NULL,
                       seat_row CHAR(1) NOT NULL,
                       column_num INT NOT NULL,
                       seat_number VARCHAR(10) NOT NULL,
                       FOREIGN KEY (theater_id) REFERENCES theaters(id),
                       UNIQUE KEY uk_theater_seat (theater_id, seat_row, column_num)
);

-- Create reservations table
CREATE TABLE reservations (
                              id BIGINT AUTO_INCREMENT PRIMARY KEY,
                              user_id VARCHAR(255) NOT NULL,
                              schedule_id BIGINT NOT NULL,
                              seat_id BIGINT NOT NULL,
                              version BIGINT DEFAULT 0,
                              reserved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                              FOREIGN KEY (schedule_id) REFERENCES schedules(id),
                              FOREIGN KEY (seat_id) REFERENCES seats(id),
                              UNIQUE KEY uk_schedule_seat (schedule_id, seat_id)
);

-- Insert test data
INSERT INTO genres (id, name) VALUES (1, '액션');
INSERT INTO cinema (id, name, location) VALUES (1, '테스트 시네마', '서울시 강남구');

INSERT INTO theaters (id, name, total_seats, cinema_id) VALUES
                                                            (1, '테스트 극장 1', 100, 1),
                                                            (2, '테스트 극장 2', 100, 1);

INSERT INTO movies (id, title, genre_id, running_time, release_date, rating, thumbnail_url) VALUES
                                                                                                (1, '테스트 영화 1', 1, 120, '2024-01-01', 'ALL', 'http://example.com/thumbnail1.jpg'),
                                                                                                (2, '테스트 영화 2', 1, 150, '2024-01-01', 'ALL', 'http://example.com/thumbnail2.jpg');

INSERT INTO schedules (id, movie_id, theater_id, start_time, end_time) VALUES
                                                                           (1, 1, 1, CURRENT_TIMESTAMP + INTERVAL 1 DAY, CURRENT_TIMESTAMP + INTERVAL 1 DAY + INTERVAL 2 HOUR),
                                                                           (2, 2, 2, CURRENT_TIMESTAMP + INTERVAL 2 DAY, CURRENT_TIMESTAMP + INTERVAL 2 DAY + INTERVAL 2 HOUR);

INSERT INTO seats (id, theater_id, seat_row, column_num, seat_number) VALUES
                                                                          (1, 1, 'A', 1, 'A1'),
                                                                          (2, 1, 'A', 2, 'A2'),
                                                                          (3, 1, 'A', 3, 'A3'),
                                                                          (4, 1, 'A', 4, 'A4'),
                                                                          (5, 1, 'A', 5, 'A5'),
                                                                          (6, 2, 'A', 1, 'A1'),
                                                                          (7, 2, 'A', 2, 'A2'),
                                                                          (8, 2, 'A', 3, 'A3');