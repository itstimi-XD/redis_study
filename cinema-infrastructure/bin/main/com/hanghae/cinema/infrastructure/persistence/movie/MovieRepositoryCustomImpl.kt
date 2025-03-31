package com.hanghae.cinema.infrastructure.persistence.movie

import com.hanghae.cinema.domain.movie.Movie
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import java.time.LocalDate
import com.querydsl.core.types.dsl.PathBuilder

@Repository
class MovieRepositoryCustomImpl(
    private val queryFactory: JPAQueryFactory
) {
    fun findNowPlayingMoviesWithFilters(title: String?, genre: String?): List<Movie> {
        val entityPath = PathBuilder(Movie::class.java, "movie")

        val query = queryFactory
            .selectFrom(entityPath)
            .where(entityPath.getString("releaseDate").loe(LocalDate.now().toString()))

        // 동적 조건 추가
        if (!title.isNullOrBlank()) {
            query.where(entityPath.getString("title").eq(title))
        }

        if (!genre.isNullOrBlank()) {
            query.where(entityPath.getString("genre").eq(genre))
        }

        return query.fetch()
    }
}