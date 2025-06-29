package ru.yandex.practicum.filmorate.storage.film;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.validation.Validator;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
@Slf4j
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final Validator validator;
    private final FilmRowMapper filmRowMapper;
    private final GenreDbStorage genreDbStorage;

    @Override
    public List<Film> findAll() {
        log.info("Method started (findAll)");
        String sqlQuery = "SELECT * FROM film";

        return jdbcTemplate.query(sqlQuery, filmRowMapper);
    }

    @Override
    public Film findById(Long id) {
        log.info("Method started (findById)");
        String sqlQuery = "SELECT * FROM film WHERE film_id = ?";

        try {
            return jdbcTemplate.queryForObject(sqlQuery, filmRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            log.warn("No film with id={}", id);
            throw new NotFoundException("Нет фильма с id=" + id);
        }
    }

    @Override
    public Film create(Film film) {
        log.info("Method started (create)");
        String sqlQuery = "INSERT INTO film (name, description, release_date, duration, rating_id) " +
                "VALUES(?, ?, ?, ?, ?)";

        if (!validator.checkForFilmInDatabase(film.getName())) {
            validator.filmValidation(film);
            validator.checkForRatingInDatabase(film.getMpa().getId());
            validator.checkForGenreInDatabase(film.getGenres());

            GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, film.getName());
                ps.setString(2, film.getDescription());
                ps.setDate(3, Date.valueOf(film.getReleaseDate()));
                ps.setLong(4, film.getDuration());
                ps.setObject(5, film.getMpa().getId());
                return ps;
            }, keyHolder);
            Long filmId = keyHolder.getKeyAs(Long.class);

            if (film.getGenres() != null) { // добавление в таблицу film_genre данных о жанрах добавляемого фильма
                film.getGenres().stream()
                        .map(Genre::getId)
                        .forEach(genreId -> genreDbStorage.addingGenresToFilm(filmId, genreId));
            }

            log.info("Adding a film (name = {})", film.getName());
            return findById(filmId);
        } else {
            Long filmId = findIdByName(film.getName());
            film.setId(filmId);
            return update(film);
        }
    }

    @Transactional
    @Override
    public Film update(Film film) {
        log.info("Method started (update)");
        String sqlQuery = "UPDATE film SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ? " +
                "WHERE film_id = ?";

        validator.filmValidation(film);

        try {
            jdbcTemplate.update(sqlQuery,
                    film.getName(),
                    film.getDescription(),
                    film.getReleaseDate(),
                    film.getDuration(),
                    film.getMpa().getId(),
                    film.getId());
        } catch (RuntimeException e) {
            log.warn("The film is not updated. Reason: {}", e.getMessage());
            throw new ValidationException("Произошла ошибка обновления фильма. Причина: " + e.getMessage());
        }

        if (film.getGenres() == null) { // блок обновления жанров связанных с этим фильмом
            genreDbStorage.removeFilmGenres(film.getId());
        } else {
            genreDbStorage.removeFilmGenres(film.getId());
            film.getGenres().stream()
                    .map(Genre::getId)
                    .forEach(genreId -> genreDbStorage.addingGenresToFilm(film.getId(), genreId));
        }

        log.info("The film has been updated (name = {})", film.getName());
        return findById(film.getId());
    }

    @Override
    public void delete(Long id) {
        log.info("Method started (delete)");
        String sqlQuery = "DELETE FROM film WHERE film_id = ?";

        jdbcTemplate.update(sqlQuery, id);
    }

    public Long findIdByName(String name) {
        log.info("Method started (findByName)");
        String sqlQuery = "SELECT film_id FROM film WHERE name = ?";

        try {
            return jdbcTemplate.queryForObject(sqlQuery, Long.class, name);
        } catch (EmptyResultDataAccessException e) {
            log.info("No film with name={}", name);
            return null;
        }
    }
}
