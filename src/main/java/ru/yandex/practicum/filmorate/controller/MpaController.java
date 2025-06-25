package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.RatingDbStorage;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
@Slf4j
public class MpaController {
    private final RatingDbStorage ratingDbStorage;

    @GetMapping
    public List<Rating> findAll() {
        log.info("Method started (findAll)");
        return ratingDbStorage.findAll();
    }

    @GetMapping("/{id}")
    public Rating findById(@PathVariable Integer id) {
        log.info("Method started (findById)");
        return ratingDbStorage.findById(id);
    }
}