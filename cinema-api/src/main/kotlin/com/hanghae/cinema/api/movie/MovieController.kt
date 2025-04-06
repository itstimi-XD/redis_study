package com.hanghae.cinema.api.movie

import com.hanghae.cinema.api.annotation.RateLimit
import com.hanghae.cinema.api.annotation.RateLimitType
import com.hanghae.cinema.api.exception.InvalidRequestException
import com.hanghae.cinema.api.response.ApiResponse
import com.hanghae.cinema.application.movie.MovieFacade
import com.hanghae.cinema.application.movie.dto.MovieResponseDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/movies")
@Tag(name = "Movies", description = "영화 API")
class MovieController(
    private val movieFacade: MovieFacade
) {
    
    @GetMapping
    @RateLimit(type = RateLimitType.API_CALL)
    @Operation(summary = "현재 상영 중인 영화 목록 조회", description = "현재 상영 중인 영화 목록을 반환합니다.")
    fun getNowPlayingMovies(
        @RequestParam(required = false) title: String?,
        @RequestParam(required = false) genre: String?
    ): ApiResponse<List<MovieResponseDto>> {
        validateSearchParams(title, genre)
        val movies = movieFacade.getNowPlayingMovies(title, genre)
        return ApiResponse.success(movies)
    }
    
    private fun validateSearchParams(title: String?, genre: String?) {
        if (title != null && title.length > 255) {
            throw InvalidRequestException("영화 제목은 255자를 초과할 수 없습니다.")
        }
        if (genre != null && genre.length > 100) {
            throw InvalidRequestException("장르 이름은 100자를 초과할 수 없습니다.")
        }
    }
} 