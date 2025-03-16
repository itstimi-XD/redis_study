package com.hanghae.cinema.domain.genre

interface GenreRepository {
    fun save(genre: Genre): Genre
    fun saveAll(genres: List<Genre>): List<Genre>
    fun findAll(): List<Genre>
    fun findById(id: Long): Genre?
} 