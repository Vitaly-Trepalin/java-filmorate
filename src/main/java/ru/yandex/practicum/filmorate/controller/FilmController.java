package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Get all films");
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        validation(film);
        film.setId(getNextId());
        log.debug("Adding id (id={})", film.getId());
        films.put(film.getId(), film);
        log.info("Adding a movie id={}", film.getId());
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        if (film.getId() == null) {
            log.warn("Movie id not specified");
            throw new ValidationException("Не указан id фильма");
        }
        if (films.containsKey(film.getId())) {
            validation(film);
            log.info("The film id={} has been updated", film.getId());
            films.put(film.getId(), film);
            return film;
        } else {
            log.warn("There is no movie with id={}", film.getId());
            throw new NotFoundException("Фильма с id=" + film.getId() + " нет");
        }
    }

    private long getNextId() {
        long maxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++maxId;
    }

    private void validation(Film film) {
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
            throw new ValidationException("Продолжительность фильма не может быть отртцательной");
        }
    }
}
