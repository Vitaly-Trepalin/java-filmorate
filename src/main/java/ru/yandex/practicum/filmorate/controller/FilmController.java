package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.*;

@RestController
@RequestMapping("/films")
@Slf4j
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;
    private final FilmStorage filmDbStorage;

    @GetMapping
    public List<Film> findAll() {
        log.info("Method started (findAll)");
        return filmDbStorage.findAll();
    }

    @GetMapping("/{id}")
    public Film findById(@PathVariable Long id) {
        log.info("Method started (findById)");
        return filmDbStorage.findById(id);
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        log.info("Method started (create)");
        return filmDbStorage.create(film);
    }

    @PutMapping
    public Film update(@RequestBody Film film) {
        log.info("Method started (update)");
        return filmDbStorage.update(film);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.info("Method started (delete)");
        filmDbStorage.delete(id);
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
