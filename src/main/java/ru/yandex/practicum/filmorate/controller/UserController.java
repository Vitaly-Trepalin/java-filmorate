package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {
    private final UserStorage userStorage;
    private final UserService userService;

    public UserController(@Qualifier("UserDbStorage") UserStorage userStorage, UserService userService) {
        this.userStorage = userStorage;
        this.userService = userService;
    }

    @GetMapping
    public List<User> findAll() {
        log.info("Method started (findAll)");
        return userStorage.findAll();
    }

    @GetMapping("/{id}")
    public User findById(@PathVariable Long id) {
        log.info("Method started (findById)");
        return userStorage.findById(id);
    }

    @PostMapping
    public User create(@RequestBody User user) {
        log.info("Method started (create)");
        return userStorage.create(user);
    }

    @PutMapping
    public User update(@RequestBody User user) {
        log.info("Method started (update)");
        return userStorage.update(user);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        log.info("Method started (delete)");
        userStorage.delete(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable Long id,
                          @PathVariable Long friendId) {
        log.info("Method started (addFriend)");
        userService.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable Long id,
                             @PathVariable Long friendId) {
        log.info("Method started (removeFriend)");
        userService.removeFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getFriends(@PathVariable Long id) {
        log.info("Method started (getFriends)");
        return userService.getFriends(id);
    }

    @GetMapping("/{firstId}/friends/common/{secondId}")
    public List<User> mutualFriends(@PathVariable Long firstId,
                                    @PathVariable Long secondId) {
        log.info("Method started (mutualFriends)");
        return userService.mutualFriends(firstId, secondId);
    }
}