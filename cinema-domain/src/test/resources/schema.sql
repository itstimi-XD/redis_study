CREATE TABLE IF NOT EXISTS cinema (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_at DATETIME NOT NULL,
    updated_by VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS genres (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at DATETIME NOT NULL,
    created_by VARCHAR(255) NOT NULL, 
    updated_at DATETIME NOT NULL,
    updated_by VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS theaters (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    cinema_id BIGINT NOT NULL,
    total_seats INT NOT NULL,
    created_at DATETIME NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_at DATETIME NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    FOREIGN KEY (cinema_id) REFERENCES cinema(id)
);

CREATE TABLE IF NOT EXISTS movies (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    rating VARCHAR(10) NOT NULL,
    release_date DATE NOT NULL,
    thumbnail_url VARCHAR(255),
    running_time INT NOT NULL,
    genre_id BIGINT NOT NULL,
    created_at DATETIME NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_at DATETIME NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    FOREIGN KEY (genre_id) REFERENCES genres(id)
);

CREATE TABLE IF NOT EXISTS schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    movie_id BIGINT NOT NULL,
    theater_id BIGINT NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    created_at DATETIME NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_at DATETIME NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    FOREIGN KEY (movie_id) REFERENCES movies(id),
    FOREIGN KEY (theater_id) REFERENCES theaters(id)
);

CREATE TABLE IF NOT EXISTS seats (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    theater_id BIGINT NOT NULL,
    seat_number VARCHAR(10) NOT NULL,
    seat_row CHAR(1) NOT NULL,
    column_num INT NOT NULL,
    created_at DATETIME NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_at DATETIME NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    FOREIGN KEY (theater_id) REFERENCES theaters(id)
);

CREATE TABLE IF NOT EXISTS reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    schedule_id BIGINT NOT NULL,
    seat_id BIGINT NOT NULL,
    user_id VARCHAR(255) NOT NULL,
    version BIGINT DEFAULT 0,
    reserved_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_at DATETIME NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    FOREIGN KEY (schedule_id) REFERENCES schedules(id),
    FOREIGN KEY (seat_id) REFERENCES seats(id)
); 