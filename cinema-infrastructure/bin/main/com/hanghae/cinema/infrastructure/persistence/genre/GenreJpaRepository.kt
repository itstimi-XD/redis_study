package com.hanghae.cinema.infrastructure.persistence.genre

import com.hanghae.cinema.domain.genre.Genre
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface GenreJpaRepository : JpaRepository<Genre, Long> 