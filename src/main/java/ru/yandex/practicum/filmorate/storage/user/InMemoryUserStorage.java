package ru.yandex.practicum.filmorate.storage.user;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;

@Component("InMemoryUserStorage")
@Slf4j
@Getter
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public List<User> findAll() {
        log.info("Method started (findAll)");
        return new ArrayList<>(users.values());
    }

    @Override
    public User findById(Long id) {
        log.info("Method started (findById)");
        if (!users.containsKey(id)) {
            log.warn("No user with id={}", id);
            throw new NotFoundException("Нет пользователя с id=" + id);
        }
        log.info("User with id={} found", id);
        return users.values().stream()
                .filter(user -> Objects.equals(user.getId(), id))
                .findFirst().get();
    }

    @Override
    public User create(User user) {
        log.info("Method started (create)");
        validation(user);
        if (user.getName() == null || user.getName().isBlank()) {
            log.debug("Replacing name with login");
            user.setName(user.getLogin());
        }
        user.setId(getNextId());
        log.debug("Getting a new id id={}", user.getId());
//        user.setFriends(new HashSet<>());
        log.debug("Getting a new id id={}", user.getId());
        users.put(user.getId(), user);
        log.info("Adding a new user id={}", user.getId());
        return user;
    }

    @Override
    public User update(User user) {
        log.info("Method started (update)");
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

    @Override
    public void delete(Long id) {
        log.info("Method started (delete)");
        if (!users.containsKey(id)) {
            log.warn("User with id={} not found", id);
            throw new NotFoundException("Пользователь с id=" + id + " не найден");
        }
        log.info("User with id={} delete", id);
        users.remove(id);
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
