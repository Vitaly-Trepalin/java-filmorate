package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public List<User> mutualFriends(Long id, Long otherId) {
        log.info("Method started (mutualFriends)");
        Map<Long, User> users = userStorage.getUsers();
        if (!users.containsKey(id)) {
            log.warn("Mutual friends list not shown. No user with id={}", id);
            throw new NotFoundException("Список общих друзей не показан. Нет пользователя с id=" + id);
        }
        if (!users.containsKey(otherId)) {
            log.warn("Mutual friends list not shown. No user with id={}", otherId);
            throw new NotFoundException("Список общих друзей не показан. Нет пользователя с id=" + otherId);
        }
        User user1 = users.get(id);
        User user2 = users.get(otherId);
        log.info("A list of mutual friends is shown");
        return user1.getFriends().stream()
                .filter(friends -> user2.getFriends().contains(friends))
                .map(users::get)
                .toList();
    }

    public void addFriend(Long userId, Long friendId) {
        log.info("Method started (addFriend)");
        Map<Long, User> users = userStorage.getUsers();
        if (!users.containsKey(userId)) {
            log.warn("Friend not added. No user with id={}", userId);
            throw new NotFoundException("Друг не добавлен. Нет пользователя с id=" + userId);
        }
        if (!users.containsKey(friendId)) {
            log.warn("Friend not added. No user with id={}", friendId);
            throw new NotFoundException("Друг не добавлен. Нет пользователя с id=" + friendId);
        }
        log.info("Friend added");
        Set<Long> friends1 = users.get(userId).getFriends();
        friends1.add(friendId);

        Set<Long> friends2 = users.get(friendId).getFriends();
        friends2.add(userId);
    }

    public void removeFriend(Long userId, Long friendId) {
        log.info("Method started (removeFriend)");
        Map<Long, User> users = userStorage.getUsers();
        if (!users.containsKey(userId)) {
            log.warn("Friend not deleted. No user with id={}", userId);
            throw new NotFoundException("Друг не удален. Нет пользователя с id=" + userId);
        }
        if (!users.containsKey(friendId)) {
            log.warn("Friend not deleted. No user with id={}", friendId);
            throw new NotFoundException("Друг не удален. Нет пользователя с id=" + friendId);
        }
        log.info("Friend deleted");
        Set<Long> friends1 = users.get(userId).getFriends();
        friends1.remove(friendId);

        Set<Long> friends2 = users.get(friendId).getFriends();
        friends2.remove(userId);
    }

    public List<User> getFriends(Long id) {
        log.info("Method started (getFriends)");
        Map<Long, User> users = userStorage.getUsers();
        if (!users.containsKey(id)) {
            log.warn("Friends list is not available. No user with id={}", id);
            throw new NotFoundException("Список друзей недоступен. Нет пользователя с id=" + id);
        }
        List<Long> friends = new ArrayList<>(users.get(id).getFriends());
        log.info("Friends list received");
        return friends.stream()
                .map(users::get)
                .toList();
    }
}
