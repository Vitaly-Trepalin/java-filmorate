package ru.yandex.practicum.filmorate.validation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;

import java.time.LocalDate;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class Validator {
    private final JdbcTemplate jdbcTemplate;
    private final GenreDbStorage genreDbStorage;

    public void filmValidation(Film film) {
        log.info("Method started (filmValidation)");
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("The title of the film is not specified");
            throw new ValidationException("Не указано название фильма");
        }
        if (film.getDescription() == null || film.getDescription().isBlank()) {
            log.warn("No movie description provided");
            throw new ValidationException("Не указано описание фильма");
        }
        if (film.getDescription().length() > 200) {
            log.warn("The movie description contains more than 200 characters");
            throw new ValidationException("Описание фильма превышает 200 символов");
        }
        if (film.getReleaseDate() == null) {
            log.warn("The release date of the film is not specified");
            throw new ValidationException("Не указана дата выхода фильма");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("The release date of the film is incorrect");
            throw new ValidationException("Дата выхода фильма неверная");
        }
        if (film.getDuration() == null) {
            log.warn("The duration of the film is not specified");
            throw new ValidationException("Не указана продолжительность фильма");
        }
        if (film.getDuration() < 0) {
            log.warn("The length of a movie cannot be negative");
            throw new ValidationException("Продолжительность фильма не может быть отрицательной");
        }
    }

    public void userValidation(User user) {
        log.info("Method started (userValidation)");
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("User email is not specified");
            throw new ValidationException("Не указан имейл пользователя");
        }
        if (!user.getEmail().contains("@")) {
            log.warn("User's email must contain the @ symbol");
            throw new ValidationException("Имейл пользователя должен содержать символ @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.warn("User login not specified");
            throw new ValidationException("Не указан логин пользователя");
        }
        if (user.getLogin().contains(" ")) {
            log.warn("Login cannot contain spaces");
            throw new ValidationException("Логин не может содержать пробелы");
        }
        if (user.getBirthday() == null) {
            log.warn("User's date of birth is not specified");
            throw new ValidationException("Не указана дата рождения пользователя");
        }
        if (user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Date of birth cannot be in the future");
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }

    public boolean checkForFilmInDatabase(String name) {
        log.info("Method started (checkIfFilmIsInDatabase)");
        String sqlQuery = "SELECT name FROM film WHERE name = ?";

        try {
            jdbcTemplate.queryForObject(sqlQuery, String.class, name);
            return true;
        } catch (EmptyResultDataAccessException e) {
            log.info("No film with name={}", name);
            return false;
        }
    }

    public boolean checkForFilmInDatabase(Long filmId) {
        log.info("Method started (checkIfFilmIsInDatabase)");
        String sqlQuery = "SELECT name FROM film WHERE film_id = ?";

        try {
            jdbcTemplate.queryForObject(sqlQuery, String.class, filmId);
            return true;
        } catch (EmptyResultDataAccessException e) {
            log.info("No film with name={}", filmId);
            return false;
        }
    }

    public void checkForRatingInDatabase(Integer ratingId) {
        log.info("Method started (checkForRatingInDatabase)");
        String sqlQuery = "SELECT name FROM rating WHERE rating_id = ?";
        try {
            jdbcTemplate.queryForObject(sqlQuery, String.class, ratingId);
        } catch (EmptyResultDataAccessException e) {
            log.warn("No rating with id={}", ratingId);
            throw new NotFoundException("Нет рейтинга с id=" + ratingId);
        }
    }

    public void checkForGenreInDatabase(List<Genre> genres) {
        if (genres != null) {
            genres.stream().map(Genre::getId).forEach(genreDbStorage::findById);
        }
    }

    public void checkForUserInDatabase(Long userId) {
        log.info("Method started (checkForUserInDatabase)");
        String sqlQuery = "SELECT name FROM \"user\" WHERE user_id = ?";

        try {
            jdbcTemplate.queryForObject(sqlQuery, String.class, userId);
        } catch (EmptyResultDataAccessException e) {
            log.warn("No user with id={}", userId);
            throw new NotFoundException("Нет пользователя с id=" + userId);
        }
    }
}
