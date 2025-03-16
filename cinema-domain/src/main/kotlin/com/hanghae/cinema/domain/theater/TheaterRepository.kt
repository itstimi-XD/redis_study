package com.hanghae.cinema.domain.theater

interface TheaterRepository {
    fun save(theater: Theater): Theater
    fun saveAll(theaters: List<Theater>): List<Theater>
    fun findAll(): List<Theater>
    fun findById(id: Long): Theater?
} 