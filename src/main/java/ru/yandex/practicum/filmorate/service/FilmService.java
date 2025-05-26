package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public void addLike(Long id, Long userId) {
        log.info("Method started (addLike)");
        Map<Long, Film> films = filmStorage.getFilms();
        Map<Long, User> users = userStorage.getUsers();
        if (!films.containsKey(id)) {
            log.warn("Like not added. No movie id={}", id);
            throw new NotFoundException("Лайк не поставлен. Нет фильма id=" + id);
        }
        if (!users.containsKey(userId)) {
            log.warn("Like not added. No user id={}", id);
            throw new NotFoundException("Лайк не поставлен. Нет пользователя id=" + userId);
        }
        Set<Long> likes = films.get(id).getLikes();
        log.info("like added id={}", id);
        likes.add(userId);
    }

    public void removeLike(Long id, Long userId) {
        log.info("Method started (removeLike)");
        Map<Long, Film> films = filmStorage.getFilms();
        Map<Long, User> users = userStorage.getUsers();
        if (!films.containsKey(id)) {
            log.warn("Like not deleted. No film id={}", id);
            throw new NotFoundException("Лайк не удален. Нет фильма id=" + id);
        }
        if (!users.containsKey(userId)) {
            log.warn("Like not deleted. No user id={}", userId);
            throw new NotFoundException("Лайк не удален. Нет пользователя id=" + userId);
        }
        Set<Long> likes = films.get(id).getLikes();
        if (!likes.contains(userId)) {
            log.warn("User with id={} did not like", userId);
            throw new NotFoundException("Пользователь id=" + id + " не ставил лайк");
        }
        log.info("like id={} remove", id);
        likes.remove(userId);
    }

    public List<Film> showPopularFilms() {
        Map<Long, Film> films = filmStorage.getFilms();
        log.info("Method started (showPopularFilms)");
        return films.values().stream()
                .sorted((x, y) -> y.getLikes().size() - x.getLikes().size())
                .limit(10)
                .toList();
    }

    public List<Film> showPopularFilms(Long count) {
        Map<Long, Film> films = filmStorage.getFilms();
        log.info("Method started (showPopularFilms)");
        return films.values().stream()
                .sorted((x, y) -> y.getLikes().size() - x.getLikes().size())
                .limit(count)
                .toList();
    }
}
