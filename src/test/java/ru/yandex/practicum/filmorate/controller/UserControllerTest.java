package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
    private UserController userController;

    @BeforeEach
    public void setUp() {
        UserStorage userStorage = new InMemoryUserStorage();
        UserService userService = new UserService(userStorage);

        userController = new UserController(userStorage, userService);

        User user1 = User.builder()
                .name("Первый")
                .email("one@ya.ru")
                .login("user1")
                .birthday(LocalDate.of(1988, 6, 18))
                .friends(new HashSet<>())
                .build();

        User user2 = User.builder()
                .name("Второй")
                .email("two@ya.ru")
                .login("user2")
                .birthday(LocalDate.of(1989, 9, 13))
                .friends(new HashSet<>())
                .build();

        userController.create(user1);
        userController.create(user2);
    }

    @AfterEach
    public void shutDown() {
        UserStorage userStorage = new InMemoryUserStorage();
        UserService userService = new UserService(userStorage);

        userController = new UserController(userStorage, userService);
    }

    @Test
    void checkFindAllUsers() {
        User user1 = User.builder()
                .id(1L)
                .name("Первый")
                .email("one@ya.ru")
                .login("user1")
                .birthday(LocalDate.of(1988, 6, 18))
                .friends(new HashSet<>())
                .build();

        User user2 = User.builder()
                .id(2L)
                .name("Второй")
                .email("two@ya.ru")
                .login("user2")
                .birthday(LocalDate.of(1989, 9, 13))
                .friends(new HashSet<>())
                .build();

        Collection<User> expected = new ArrayList<>(List.of(user1, user2));

        Collection<User> actual = new ArrayList<>(userController.findAll());

        assertEquals(expected, actual, "Возвращен неверный список пользователей");
    }

    @Test
    void checkCreateUser() {
        User expected = User.builder()
                .id(3L)
                .name("Третий")
                .email("three@ya.ru")
                .login("user3")
                .birthday(LocalDate.of(1988, 6, 22))
                .friends(new HashSet<>())
                .build();

        User user = User.builder()
                .name("Третий")
                .email("three@ya.ru")
                .login("user3")
                .birthday(LocalDate.of(1988, 6, 22))
                .friends(new HashSet<>())
                .build();

        userController.create(user);
        User actual = new ArrayList<>(userController.findAll()).get(2);

        assertEquals(expected, actual, "Ошибка при добавлении пользователя");
    }

    @Test
    void checkingLoginWhenAddingUser() {
        var exception = assertThrows(ValidationException.class, () -> userController.create(User.builder()
                .id(3L)
                .name("Третий")
                .email("three@ya.ru")
                .birthday(LocalDate.of(1988, 6, 22))
                .build()));

        assertTrue(exception.getMessage().equals("Не указан логин пользователя"));
    }

    @Test
    void checkUpdateUser() {
        User expected = User.builder()
                .id(2L)
                .name("Новый второй пользователь")
                .email("newtwo@ya.ru")
                .login("newUser2")
                .birthday(LocalDate.of(1965, 10, 1))
                .build();

        userController.update(expected);
        User actual = new ArrayList<>(userController.findAll()).get(1);

        assertEquals(expected, actual, "Ошибка при обновлении пользователя");
    }

    @Test
    void checkingForUserAvailability() {
        var exception = assertThrows(NotFoundException.class, () -> userController.update(User.builder()
                .id(3L)
                .name("Новый второй пользователь")
                .email("newtwo@ya.ru")
                .login("newUser2")
                .birthday(LocalDate.of(1965, 10, 1))
                .build()));

        assertTrue(exception.getMessage().equals("Пользователь с id=3 не найден"));
    }
}