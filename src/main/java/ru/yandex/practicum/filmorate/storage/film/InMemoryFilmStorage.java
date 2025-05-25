package ru.yandex.practicum.filmorate.storage.film;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.*;

@Component
@Slf4j
@Getter
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public List<Film> findAll() {
        log.info("Method started (findAll)");
        return new ArrayList<>(films.values());
    }

    @Override
    public Film findById(Long id) {
        log.info("Method started (findById)");
        if (!films.containsKey(id)) {
            log.warn("No movie with id={}", id);
            throw new NotFoundException("Нет фильма с id=" + id);
        }
        log.info("Film received id={}", id);
        return films.values().stream()
                .filter(film -> Objects.equals(film.getId(), id))
                .findFirst().get();
    }

    @Override
    public Film create(Film film) {
        log.info("Method started (create)");
        validation(film);
        film.setId(getNextId());
        log.debug("Adding id (id={})", film.getId());
        film.setLikes(new HashSet<>());
        log.debug("Creating a friends collection");
        films.put(film.getId(), film);
        log.info("Adding a movie id={}", film.getId());
        return film;
    }

    @Override
    public Film update(Film film) {
        log.info("Method started (update)");
        if (film.getId() == null) {
            log.warn("Movie id not specified");
            throw new ValidationException("Не указан id фильма");
        }
        if (films.containsKey(film.getId())) {
            validation(film);
            if (film.getLikes() == null) {
                film.setLikes(films.get(film.getId()).getLikes());
            }
            log.info("The film id={} has been updated", film.getId());
            films.put(film.getId(), film);
            return film;
        } else {
            log.warn("There is no movie with id={}", film.getId());
            throw new NotFoundException("Фильма с id=" + film.getId() + " нет");
        }
    }

    @Override
    public void delete(Long id) {
        log.info("Method started (delete)");
        if (!films.containsKey(id)) {
            log.warn("There is no movie with id={}", id);
            throw new NotFoundException("Фильма с id=" + id + " нет");
        }
        log.info("The film id={} has been delete", id);
        films.remove(id);
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
            throw new ValidationException("Продолжительность фильма не может быть отрицательной");
        }
    }
}
