package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Map;

public interface UserStorage {
    User create(User user);

    User update(User user);

    void delete(Long id);

    List<User> findAll();

    User findById(Long id);

    Map<Long, User> getUsers();
}
