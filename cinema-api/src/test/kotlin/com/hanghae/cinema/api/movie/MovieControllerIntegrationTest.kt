package com.hanghae.cinema.api.movie

import com.hanghae.cinema.api.exception.RateLimitExceededException
import com.hanghae.cinema.application.movie.MovieFacade
import com.hanghae.cinema.application.movie.dto.MovieResponseDto
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDate
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
class MovieControllerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var movieFacade: MovieFacade

    @Test
    fun `영화 목록 조회 API가 정상적으로 동작한다`() {
        // given
        val movies = listOf(
            MovieResponseDto(
                id = 1L,
                title = "Test Movie",
                rating = "12세 이상",
                releaseDate = LocalDate.now(),
                thumbnailUrl = "http://example.com/image.jpg",
                runningTime = 120,
                genre = "액션",
                schedules = emptyList()
            )
        )
        `when`(movieFacade.getNowPlayingMovies(null, null)).thenReturn(movies)

        // when & then
        mockMvc.perform(get("/api/movies")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.status").value(200))
            .andExpect(jsonPath("$.code").value("SUCCESS"))
            .andExpect(jsonPath("$.data[0].title").value("Test Movie"))
    }

    @Test
    fun `잘못된 검색 파라미터로 요청하면 400 에러가 발생한다`() {
        // given
        val longTitle = "a".repeat(256)

        // when & then
        mockMvc.perform(get("/api/movies")
            .param("title", longTitle)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest)
    }
} 