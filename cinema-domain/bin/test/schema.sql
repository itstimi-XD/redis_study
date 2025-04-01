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
    location VARCHAR(255) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

-- Create tables
CREATE TABLE genres (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE TABLE theaters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    total_seats INTEGER NOT NULL,
    cinema_id BIGINT,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    FOREIGN KEY (cinema_id) REFERENCES cinema(id)
);

CREATE TABLE movies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255),
    genre_id BIGINT,
    running_time INTEGER NOT NULL,
    release_date DATE,
    rating VARCHAR(255),
    thumbnail_url VARCHAR(255),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    FOREIGN KEY (genre_id) REFERENCES genres(id)
);

CREATE TABLE schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    movie_id BIGINT,
    theater_id BIGINT,
    start_time DATETIME(6),
    end_time DATETIME(6),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    FOREIGN KEY (movie_id) REFERENCES movies(id),
    FOREIGN KEY (theater_id) REFERENCES theaters(id)
);

CREATE TABLE seats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    theater_id BIGINT,
    seat_row CHAR(1),
    column_num INTEGER,
    seat_number VARCHAR(255),
    created_at DATETIME(6),
    updated_at DATETIME(6),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    FOREIGN KEY (theater_id) REFERENCES theaters(id)
);

CREATE TABLE reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    schedule_id BIGINT,
    seat_id BIGINT,
    user_id VARCHAR(255),
    reserved_at DATETIME(6),
    version BIGINT NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    FOREIGN KEY (schedule_id) REFERENCES schedules(id),
    FOREIGN KEY (seat_id) REFERENCES seats(id)
);

-- Insert test data
INSERT INTO cinema (id, name, location, created_at, created_by, updated_at, updated_by) 
VALUES (1, 'Test Cinema', 'Test Location', NOW(), 'system', NOW(), 'system');

INSERT INTO genres (id, name) VALUES (1, 'Action');

INSERT INTO theaters (id, name, total_seats, cinema_id) VALUES (1, 'Theater 1', 100, 1);

INSERT INTO movies (id, title, genre_id, running_time, release_date, rating) 
VALUES (1, 'Test Movie', 1, 120, '2024-01-01', 'ALL');

INSERT INTO schedules (id, movie_id, theater_id, start_time, end_time) 
VALUES (1, 1, 1, '2024-01-01 10:00:00', '2024-01-01 12:00:00');

-- Insert test seats (A1, A2, A3)
INSERT INTO seats (id, theater_id, seat_row, column_num, seat_number) VALUES 
(1, 1, 'A', 1, 'A1'),
(2, 1, 'A', 2, 'A2'),
(3, 1, 'A', 3, 'A3'),
(4, 1, 'A', 4, 'A4'),
(5, 1, 'A', 5, 'A5'),
(6, 1, 'A', 6, 'A6'); 