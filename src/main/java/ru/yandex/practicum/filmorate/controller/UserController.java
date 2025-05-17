package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private Map<Long, User> users = new HashMap<>();

    @GetMapping
    public List<User> findAll() {
        log.info("Get all users");
        return new ArrayList<>(users.values());
    }

    @PostMapping
    public User create(@RequestBody User user) {
        validation(user);
        if (user.getName() == null || user.getName().isBlank()) {
            log.debug("Replacing name with login");
            user.setName(user.getLogin());
        }
        user.setId(getNextId());
        log.debug("Getting a new id id={}", user.getId());
        users.put(user.getId(), user);
        log.info("Adding a new user id={}", user.getId());
        return user;
    }

    @PutMapping
    public User update(@RequestBody User user) {
        if (users.containsKey(user.getId())) {
            validation(user);
            if (user.getName() == null || user.getName().isBlank()) {
                log.debug("Replacing name with login");
                user.setName(user.getLogin());
            }
            log.info("User with id={} updated", user.getId());
            users.put(user.getId(), user);
            return user;
        } else {
            log.warn("User with id={} not found", user.getId());
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден");
        }
    }

    private long getNextId() {
        long maxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++maxId;
    }

    private void validation(User user) {
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
}