package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.*;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final FilmService filmService;
    private final FilmStorage filmStorage;

    public FilmController(FilmService filmService, @Qualifier("FilmDbStorage") FilmStorage filmStorage) {
        this.filmService = filmService;
        this.filmStorage = filmStorage;
    }

    @GetMapping
    public List<Film> findAll() {
        log.info("Method started (findAll)");
        return filmStorage.findAll();
    }

    @GetMapping("/{id}")
    public Film findById(@PathVariable Long id) {
        log.info("Method started (findById)");
        return filmStorage.findById(id);
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        log.info("Method started (create)");
        return filmStorage.create(film);
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        log.info("Method started (update)");
        return filmStorage.update(film);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.info("Method started (delete)");
        filmStorage.delete(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable Long id,
                        @PathVariable Long userId) {
        log.info("Method started (addLike)");
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable Long id,
                           @PathVariable Long userId) {
        log.info("Method started (removeLike)");
        filmService.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public List<Film> showPopularFilms(@RequestParam(required = false) Long count) {
        log.info("Method started (showPopularFilms)");
        if (count == null) {
            return filmService.showPopularFilms();
        } else {
            return filmService.showPopularFilms(count);
        }
    }
}
