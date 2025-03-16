package com.hanghae.cinema.api.movie

import com.hanghae.cinema.application.movie.MovieFacade
import com.hanghae.cinema.application.movie.dto.MovieResponseDto
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/movies")
class MovieController(private val movieFacade: MovieFacade) {
    
    @GetMapping("/now-playing")
    fun getNowPlayingMovies(): ResponseEntity<List<MovieResponseDto>> {
        val movies = movieFacade.getNowPlayingMovies()
        return ResponseEntity.ok(movies)
    }
} 