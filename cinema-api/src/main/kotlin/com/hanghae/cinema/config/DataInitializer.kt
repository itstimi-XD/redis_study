package com.hanghae.cinema.config

import com.hanghae.cinema.domain.genre.Genre
import com.hanghae.cinema.domain.genre.GenreRepository
import com.hanghae.cinema.domain.movie.Movie
import com.hanghae.cinema.domain.movie.MovieRepository
import com.hanghae.cinema.domain.theater.Theater
import com.hanghae.cinema.domain.theater.TheaterRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import java.time.LocalDate

@Configuration
class DataInitializer {

    @Bean
    @Profile("test")
    fun initData(
        genreRepository: GenreRepository,
        movieRepository: MovieRepository,
        theaterRepository: TheaterRepository
    ): CommandLineRunner {
        return CommandLineRunner {
            // 장르 초기화
            val genres = listOf(
                Genre(name = "액션"),
                Genre(name = "코미디"),
                Genre(name = "드라마"),
                Genre(name = "SF")
            )
            val savedGenres = genreRepository.saveAll(genres)
            
            // 영화 초기화
            val movies = listOf(
                Movie(
                    title = "어벤져스: 엔드게임",
                    releaseDate = LocalDate.of(2019, 4, 24),
                    runningTime = 181,
                    rating = "12세 이상",
                    thumbnailUrl = "https://example.com/avengers.jpg",
                    genre = savedGenres[0] // 액션
                ),
                Movie(
                    title = "기생충",
                    releaseDate = LocalDate.of(2019, 5, 30),
                    runningTime = 132,
                    rating = "15세 이상",
                    thumbnailUrl = "https://example.com/parasite.jpg",
                    genre = savedGenres[2] // 드라마
                ),
                Movie(
                    title = "인터스텔라",
                    releaseDate = LocalDate.of(2014, 11, 6),
                    runningTime = 169,
                    rating = "12세 이상",
                    thumbnailUrl = "https://example.com/interstellar.jpg",
                    genre = savedGenres[3] // SF
                )
            )
            movieRepository.saveAll(movies)
            
            // 상영관 초기화
            val theaters = listOf(
                Theater(name = "1관", totalSeats = 100),
                Theater(name = "2관", totalSeats = 120),
                Theater(name = "3관", totalSeats = 80)
            )
            theaterRepository.saveAll(theaters)
            
            println("초기 데이터가 성공적으로 삽입되었습니다.")
        }
    }
} 