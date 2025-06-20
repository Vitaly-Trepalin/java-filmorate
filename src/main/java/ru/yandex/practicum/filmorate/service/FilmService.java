package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final JdbcTemplate jdbcTemplate;
    private final FilmRowMapper filmRowMapper;

    public FilmService(@Qualifier("FilmDbStorage") FilmStorage filmStorage,
                       @Qualifier("UserDbStorage") UserStorage userStorage,
                       JdbcTemplate jdbcTemplate, FilmRowMapper filmRowMapper) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.jdbcTemplate = jdbcTemplate;
        this.filmRowMapper = filmRowMapper;
    }

    public void addLike(Long filmId, Long userId) {
        log.info("Method started (addLike)");
        String sqlQuery = "INSERT INTO \"like\" VALUES (?, ?)";

        filmStorage.findById(filmId); // проверка на наличия фильма с заданным id
        userStorage.findById(userId); // проверка на наличия пользователя с заданным id

        jdbcTemplate.update(sqlQuery, filmId, userId);
        log.info("like added filmId={}", filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        log.info("Method started (removeLike)");
        String sqlQuery = "DELETE FROM \"like\" WHERE film_id = ? AND user_id = ?";

        filmStorage.findById(filmId); // проверка на наличия фильма с заданным id
        userStorage.findById(userId); // проверка на наличия пользователя с заданным id

        jdbcTemplate.update(sqlQuery, filmId, userId);
        log.info("like remove filmId={}", filmId);
    }

    public List<Film> showPopularFilms() {
        log.info("Method started (showPopularFilms)");
        String sqlQuery = "SELECT f.film_id, name, description, release_date, duration, rating_id\n" +
                "FROM film AS f\n" +
                "JOIN \"like\" AS l ON f.film_id = l.film_id\n" +
                "GROUP BY f.film_id\n" +
                "ORDER BY COUNT(l.user_id) DESC";

        return jdbcTemplate.query(sqlQuery, filmRowMapper);
    }

    public List<Film> showPopularFilms(Long count) {
        log.info("Method started (showPopularFilms(Long count))");
        String sqlQuery = "SELECT f.film_id, name, description, release_date, duration, rating_id\n" +
                "FROM film AS f\n" +
                "JOIN \"like\" AS l ON f.film_id = l.film_id\n" +
                "GROUP BY f.film_id\n" +
                "ORDER BY COUNT(l.user_id) DESC\n" +
                "LIMIT ?";

        return jdbcTemplate.query(sqlQuery, filmRowMapper, count);
    }
}
