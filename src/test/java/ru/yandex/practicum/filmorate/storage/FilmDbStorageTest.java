package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.RatingRowMapper;
import ru.yandex.practicum.filmorate.validation.Validator;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, Validator.class, FilmRowMapper.class, GenreDbStorage.class, GenreRowMapper.class,
        RatingDbStorage.class, RatingRowMapper.class})
public class FilmDbStorageTest {
    private final FilmDbStorage filmDbStorage;
    private final FilmRowMapper filmRowMapper;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp() {
        String sqlQueryFilm = "INSERT INTO film (name, description, release_date, duration, rating_id) " +
                "VALUES(?, ?, ?, ?, ?)";

        Rating rating = Rating.builder()
                .id(1)
                .name("G")
                .build();

        Film film1 = Film.builder()
                .name("Первый фильм")
                .description("Описание первого фильма")
                .releaseDate(LocalDate.of(1965, 12, 1))
                .duration(3600L)
                .mpa(rating)
                .build();
        Film film2 = Film.builder()
                .name("Второй фильм")
                .description("Описание второго фильма")
                .releaseDate(LocalDate.of(1980, 6, 23))
                .duration(7200L)
                .mpa(rating)
                .build();

        List<Film> films = List.of(film1, film2);
        films.forEach(film -> jdbcTemplate.update(sqlQueryFilm,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId())
        );

        String sqlQueryGenre = "INSERT INTO film_genre (film_id, genre_id) " +
                "VALUES(?, ?)";

        jdbcTemplate.update(sqlQueryGenre, 1, 1);
        jdbcTemplate.update(sqlQueryGenre, 1, 2);
        jdbcTemplate.update(sqlQueryGenre, 2, 1);
    }

    @AfterEach
    public void shutDown() {
        jdbcTemplate.execute("ALTER TABLE film ALTER COLUMN film_id RESTART WITH 1");
    }

    @Test
    public void testFindFilmById() {
        Genre genre1 = Genre.builder()
                .id(1)
                .name("Комедия")
                .build();
        Genre genre2 = Genre.builder()
                .id(2)
                .name("Драма")
                .build();
        Rating rating = Rating.builder()
                .id(1)
                .name("G")
                .build();

        Film film = filmDbStorage.findById(1L);

        assertThat(film)
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", "Первый фильм")
                .hasFieldOrPropertyWithValue("description", "Описание первого фильма")
                .hasFieldOrPropertyWithValue("releaseDate", LocalDate.of(1965, 12, 1))
                .hasFieldOrPropertyWithValue("duration", 3600L)
                .hasFieldOrPropertyWithValue("genres", List.of(genre1, genre2))
                .hasFieldOrPropertyWithValue("mpa", rating);
    }

    @Test
    public void testFindAllFilms() {
        Rating rating = Rating.builder()
                .id(1)
                .name("G")
                .build();

        List<Film> films = filmDbStorage.findAll();

        assertThat(films)
                .isNotNull()
                .extracting(Film::getName, Film::getDescription, Film::getReleaseDate, Film::getDuration, Film::getMpa)
                .contains(
                        tuple("Первый фильм",
                                "Описание первого фильма",
                                LocalDate.of(1965, 12, 1),
                                3600L,
                                rating),
                        tuple("Второй фильм",
                                "Описание второго фильма",
                                LocalDate.of(1980, 6, 23),
                                7200L,
                                rating)
                );
    }

    @Test
    public void testCreateFilm() {
        Rating rating = Rating.builder()
                .id(1)
                .name("G")
                .build();

        Film expectedFilm = Film.builder()
                .name("Третий фильм")
                .description("Описание третьего фильма")
                .releaseDate(LocalDate.of(1975, 12, 1))
                .duration(3000L)
                .mpa(rating)
                .build();

        filmDbStorage.create(expectedFilm);

        String sqlQuery = "SELECT * FROM film WHERE film_id = ?";
        Film actualUser = jdbcTemplate.queryForObject(sqlQuery, filmRowMapper, 3);

        assertThat(actualUser)
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", "Третий фильм")
                .hasFieldOrPropertyWithValue("description", "Описание третьего фильма")
                .hasFieldOrPropertyWithValue("releaseDate", LocalDate.of(1975, 12, 1))
                .hasFieldOrPropertyWithValue("duration", 3000L)
                .hasFieldOrPropertyWithValue("mpa", rating);
    }

    @Test
    public void testUpdateFilm() {
        Rating rating = Rating.builder()
                .id(1)
                .name("G")
                .build();

        Film expectedFilm = Film.builder()
                .id(2L)
                .name("Обновленный второй фильм")
                .description("Описание обновленного фильма")
                .releaseDate(LocalDate.of(1988, 11, 1))
                .duration(1500L)
                .mpa(rating)
                .build();

        filmDbStorage.update(expectedFilm);

        String sqlQuery = "SELECT * FROM film WHERE film_id = ?";
        Film actualUser = jdbcTemplate.queryForObject(sqlQuery, filmRowMapper, 2);

        assertThat(actualUser)
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", "Обновленный второй фильм")
                .hasFieldOrPropertyWithValue("description", "Описание обновленного фильма")
                .hasFieldOrPropertyWithValue("releaseDate", LocalDate.of(1988, 11, 1))
                .hasFieldOrPropertyWithValue("duration", 1500L)
                .hasFieldOrPropertyWithValue("mpa", rating);
    }

    @Test
    public void testDeleteFilm() {
        filmDbStorage.delete(1L);
        filmDbStorage.delete(2L);

        String sqlQuery = "SELECT * FROM film";
        List<Film> films = jdbcTemplate.query(sqlQuery, filmRowMapper);

        assertThat(films).isEmpty();
    }
}
