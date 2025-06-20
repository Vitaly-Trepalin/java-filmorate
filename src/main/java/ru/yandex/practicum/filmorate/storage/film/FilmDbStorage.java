package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.RatingDbStorage;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.validation.Validator;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;

@Repository("FilmDbStorage")
@Slf4j
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final Validator validator;
    private final FilmRowMapper filmRowMapper;
    private final GenreDbStorage genreDbStorage;
    private final RatingDbStorage ratingDbStorage;

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

        try { // проверка на наличия фильма с таким названием
            String requestName = "SELECT name FROM film WHERE name = ?";
            String name = jdbcTemplate.queryForObject(requestName, String.class, film.getName());

            log.info("A movie with this title already exists. Update the existing movie.");
            String requestId = "SELECT film_id FROM film WHERE name = ?";
            Long filmId = jdbcTemplate.queryForObject(requestId, Long.class, name);
            film.setId(filmId);
            return update(film);
        } catch (EmptyResultDataAccessException e) { // если фильма нет, создаём его

            validator.filmValidation(film); // валидация полей name, description, release_date, duration
            ratingDbStorage.findById(film.getMpa().getId()); // проверка на наличие такого рейтинга в БД
            if (film.getGenres() != null) { // проверка на наличие таких жанров в БД
                film.getGenres().stream().map(Genre::getId).forEach(genreDbStorage::findById);
            }

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
            if (filmId == null) {
                log.info("Failed to save movie {}", film.toString());
                throw new InternalServerException("Не удалось сохранить данные");
            }

            if (film.getGenres() != null) { // добавление в таблицу film_genre данных о жанрах фильма
                film.getGenres().stream()
                        .map(Genre::getId)
                        .forEach(genreId -> genreDbStorage.addingGenresToFilm(filmId, genreId));
            }

            log.info("Adding a film (name = {})", film.getName());
            return findById(filmId);
        }
    }

    @Override
    public Film update(Film film) {
        log.info("Method started (update)");
        String sqlQuery = "UPDATE film SET name = ?, description = ?, release_date = ?, duration = ?, rating_id = ? " +
                "WHERE film_id = ?";

        findById(film.getId()); // проверка наличия фильма в БД
        validator.filmValidation(film); // валидация полей name, description, release_date, duration
        ratingDbStorage.findById(film.getMpa().getId()); // проверка на наличие такого рейтинга в БД
        if (film.getGenres() != null) { // проверка на наличие таких жанров в БД
            film.getGenres().stream().map(Genre::getId).forEach(genreDbStorage::findById);
        }

        if (film.getGenres() != null) { // блок проверки старого и нового списка жанров
            List<Genre> oldListOfGenres = genreDbStorage.gettingFilmGenres(film.getId());
            List<Genre> newListOfGenres = film.getGenres();
            boolean isEqual = oldListOfGenres.size() == newListOfGenres.size()
                    && new HashSet<>(oldListOfGenres).containsAll(newListOfGenres)
                    && new HashSet<>(newListOfGenres).containsAll(oldListOfGenres);
            if (!isEqual) {
                genreDbStorage.removeFilmGenres(film.getId());
                film.getGenres().stream() // добавление в таблицу film_genre новых данных о жанрах фильма
                        .map(Genre::getId)
                        .forEach(genreId -> genreDbStorage.addingGenresToFilm(film.getId(), genreId));
            }
        }

        jdbcTemplate.update(sqlQuery,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );

        log.info("The film has been updated (name = {})", film.getName());
        return findById(film.getId());
    }

    @Override
    public void delete(Long id) {
        log.info("Method started (delete)");
        String sqlQuery = "DELETE FROM film WHERE film_id = ?";

        jdbcTemplate.update(sqlQuery, id);
    }
}
