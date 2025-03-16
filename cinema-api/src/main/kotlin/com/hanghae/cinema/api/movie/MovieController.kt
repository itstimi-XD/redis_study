package com.hanghae.cinema.api.movie

import com.hanghae.cinema.application.movie.MovieFacade
import com.hanghae.cinema.application.movie.dto.MovieResponseDto
import com.hanghae.cinema.domain.genre.GenreRepository
import com.hanghae.cinema.domain.movie.MovieRepository
import com.hanghae.cinema.domain.theater.TheaterRepository
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate

@RestController
@RequestMapping("/api/movies")
@Tag(name = "Movies", description = "영화 API")
class MovieController(
    private val movieFacade: MovieFacade,
    private val movieRepository: MovieRepository,
    private val genreRepository: GenreRepository,
    private val theaterRepository: TheaterRepository
) {
    
    @GetMapping("/now-playing")
    @Operation(summary = "현재 상영 중인 영화 목록 조회 (Facade)", description = "현재 상영 중인 영화 목록을 반환합니다.")
    fun getNowPlayingMoviesFromFacade(): ResponseEntity<List<MovieResponseDto>> {
        val movies = movieFacade.getNowPlayingMovies()
        return ResponseEntity.ok(movies)
    }
    
    @GetMapping
    @Operation(summary = "현재 상영 중인 영화 목록 조회", description = "현재 상영 중인 영화 목록을 반환합니다.")
    fun getNowPlayingMovies() = movieRepository.findByReleaseDateLessThanEqualOrderByReleaseDateDesc(LocalDate.now())
        .map { movie ->
            mapOf(
                "id" to movie.id,
                "title" to movie.title,
                "rating" to movie.rating,
                "releaseDate" to movie.releaseDate,
                "thumbnailUrl" to movie.thumbnailUrl,
                "runningTime" to movie.runningTime,
                "genre" to mapOf(
                    "id" to movie.genre.id,
                    "name" to movie.genre.name
                )
            )
        }
    
    @GetMapping("/genres")
    @Operation(summary = "장르 목록 조회", description = "모든 장르 목록을 반환합니다.")
    fun getAllGenres() = genreRepository.findAll()
        .map { genre ->
            mapOf(
                "id" to genre.id,
                "name" to genre.name
            )
        }
    
    @GetMapping("/theaters")
    @Operation(summary = "상영관 목록 조회", description = "모든 상영관 목록을 반환합니다.")
    fun getAllTheaters() = theaterRepository.findAll()
        .map { theater ->
            mapOf(
                "id" to theater.id,
                "name" to theater.name,
                "totalSeats" to theater.totalSeats
            )
        }
} 