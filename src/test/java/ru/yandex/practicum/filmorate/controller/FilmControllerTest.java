package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {
    private FilmController filmController;

    @BeforeEach
    public void setUp() {
        FilmStorage filmStorage = new InMemoryFilmStorage();
        UserStorage userStorage = new InMemoryUserStorage();
        FilmService filmService = new FilmService(filmStorage, userStorage);

        filmController = new FilmController(filmService, filmStorage);

        Film film1 = Film.builder()
                .name("Первый фильм")
                .description("Описание первого фильма")
                .releaseDate(LocalDate.of(1965, 12, 1))
                .duration(3600L)
                .build();
        Film film2 = Film.builder()
                .name("Второй фильм")
                .description("Описание второго фильма")
                .releaseDate(LocalDate.of(1980, 6, 23))
                .duration(7200L)
                .build();

        filmController.create(film1);
        filmController.create(film2);
    }

    @AfterEach
    public void shutDown() {
        FilmStorage filmStorage = new InMemoryFilmStorage();
        UserStorage userStorage = new InMemoryUserStorage();
        FilmService filmService = new FilmService(filmStorage, userStorage);

        filmController = new FilmController(filmService, filmStorage);
    }

    @Test
    void checkFindAllFilms() throws IOException, InterruptedException {
        Film film1 = Film.builder()
                .id(1L)
                .name("Первый фильм")
                .description("Описание первого фильма")
                .releaseDate(LocalDate.of(1965, 12, 1))
                .duration(3600L)
                .likes(new HashSet<>())
                .build();

        Film film2 = Film.builder()
                .id(2L)
                .name("Второй фильм")
                .description("Описание второго фильма")
                .releaseDate(LocalDate.of(1980, 6, 23))
                .duration(7200L)
                .likes(new HashSet<>())
                .build();

        Collection<Film> expected = new ArrayList<>(List.of(film1, film2));

        Collection<Film> actual = new ArrayList<>(filmController.findAll());

        assertEquals(expected, actual, "Возвращен неверный список фильмов");
    }

    @Test
    void checkCreateFilm() {
        Film expected = Film.builder()
                .id(3L)
                .name("Третий фильм")
                .description("Описание третьего фильма")
                .releaseDate(LocalDate.of(1973, 11, 8))
                .duration(1800L)
                .likes(new HashSet<>())
                .build();

        Film film = Film.builder()
                .name("Третий фильм")
                .description("Описание третьего фильма")
                .releaseDate(LocalDate.of(1973, 11, 8))
                .duration(1800L)
                .likes(new HashSet<>())
                .build();

        filmController.create(film);
        Film actual = new ArrayList<>(filmController.findAll()).get(2);

        assertEquals(expected, actual, "Ошибка при добавлении фильма");
    }

    @Test
    void checkingForNameAvailabilityWhenCreatingAMovie() {
        var exception = assertThrows(ValidationException.class, () -> filmController.create(Film.builder()
                .id(3L)
                .description("Описание третьего фильма")
                .releaseDate(LocalDate.of(1973, 11, 8))
                .duration(1800L)
                .build()));
        assertTrue(exception.getMessage().equals("Не указано название фильма"));
    }

    @Test
    void checkForNegativeMovieDuration() {
        var exception = assertThrows(ValidationException.class, () -> filmController.create(Film.builder()
                .id(3L)
                .name("Третий фильм")
                .description("Описание третьего фильма")
                .releaseDate(LocalDate.of(1973, 11, 8))
                .duration(-1800L)
                .likes(new HashSet<>())
                .build()));
        assertTrue(exception.getMessage().equals("Продолжительность фильма не может быть отрицательной"));
    }

    @Test
    void checkUpdateFilm() {
        Film expected = Film.builder()
                .id(2L)
                .name("Обновлённый второй фильм")
                .description("Новое описание второго фильма")
                .releaseDate(LocalDate.of(1965, 12, 1))
                .duration(3600L)
                .build();

        filmController.update(expected);
        Film actual = new ArrayList<>(filmController.findAll()).get(1);

        assertEquals(expected, actual, "Ошибка при обновлении фильма");
    }

    @Test
    void checkingForFilmAvailability() {
        var exception = assertThrows(NotFoundException.class, () -> filmController.update(Film.builder()
                .id(3L)
                .name("Третий фильм")
                .description("Описание третьего фильма")
                .releaseDate(LocalDate.of(1998, 10, 13))
                .duration(2000L)
                .build()));
        assertTrue(exception.getMessage().equals("Фильма с id=3 нет"));
    }
}