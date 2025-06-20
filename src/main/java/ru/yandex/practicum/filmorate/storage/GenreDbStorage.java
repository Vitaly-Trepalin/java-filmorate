package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.util.Comparator;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class GenreDbStorage {
    private final JdbcTemplate jdbcTemplate;
    private final GenreRowMapper genreRowMapper;

    public List<Genre> findAll() {
        log.info("Method started (gettingListOfGenres)");
        String sqlQuery = "SELECT * FROM genre";
        List<Genre> genres = jdbcTemplate.query(sqlQuery, genreRowMapper);

        return genres.stream()
                .sorted(Comparator.comparing(Genre::getId))
                .toList();
    }

    public Genre findById(Integer id) {
        log.info("Method started (gettingGenreById)");
        String sqlQuery = "SELECT * FROM genre WHERE genre_id = ?";
        try {
            return jdbcTemplate.queryForObject(sqlQuery, genreRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            log.warn("No genre with id={}", id);
            throw new NotFoundException("Нет жанра с id=" + id);
        }
    }

    public List<Genre> gettingFilmGenres(Long filmId) {
        log.info("Method started (gettingFilmGenres)");
        String sqlQuery = "SELECT g.genre_id, g.name\n" +
                "FROM film_genre AS fg\n" +
                "JOIN genre AS g ON fg.GENRE_ID = g.GENRE_ID\n" +
                "WHERE fg.film_id = ?";

        List<Genre> genres = jdbcTemplate.query(sqlQuery, genreRowMapper, filmId);

        return genres.stream()
                .sorted(Comparator.comparing(Genre::getId))
                .toList();
    }

    public void addingGenresToFilm(Long filmId, Integer genreId) {
        log.info("Method started (addingGenresToFilm)");
        String sqlQuery = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";

        log.info("Genre added {}", genreId);
        jdbcTemplate.update(sqlQuery, filmId, genreId);
    }

    public void removeFilmGenres(Long filmId) {
        log.info("Method started (removeGenresToFilm)");
        String sqlQuery = "DELETE FROM film_genre WHERE film_id = ?";

        log.info("Genres removed");
        jdbcTemplate.update(sqlQuery, filmId);
    }
}
