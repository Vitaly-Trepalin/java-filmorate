package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;
    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    public UserService(@Qualifier("UserDbStorage") UserStorage userStorage, JdbcTemplate jdbcTemplate,
                       UserRowMapper userRowMapper) {
        this.userStorage = userStorage;
        this.jdbcTemplate = jdbcTemplate;
        this.userRowMapper = userRowMapper;
    }

    public void addFriend(Long userId, Long friendId) {
        log.info("Method started (addFriend)");
        String sqlQuery = "INSERT INTO friend_status VALUES (?, ?)";

        userStorage.findById(userId); // проверка на наличие пользователя с заданным id
        userStorage.findById(friendId);

        jdbcTemplate.update(sqlQuery, userId, friendId);
        log.info("Friend added");
    }

    public void removeFriend(Long userId, Long friendId) {
        log.info("Method started (removeFriend)");
        String sqlQuery = "DELETE FROM friend_status WHERE user_id = ? AND friend_id = ?";

        userStorage.findById(userId);
        userStorage.findById(friendId);

        jdbcTemplate.update(sqlQuery, userId, friendId);
        log.info("Friend remove");
    }

    public List<User> getFriends(Long userId) {
        log.info("Method started (getFriends)");
        String sqlQuery = "SELECT *\n" +
                "FROM \"user\"\n" +
                "WHERE user_id IN (SELECT fs.FRIEND_ID \n" +
                "FROM friend_status AS fs\n" +
                "JOIN \"user\" AS u ON fs.USER_ID = u.USER_ID\n" +
                "WHERE u.user_id = ?)";

        userStorage.findById(userId);

        return jdbcTemplate.query(sqlQuery, userRowMapper, userId);
    }

    public List<User> mutualFriends(Long firstId, Long secondId) {
        log.info("Method started (mutualFriends)");
        String sqlQuery = "SELECT *\n" +
                "FROM \"user\"\n" +
                "WHERE user_id IN (\n" +
                "SELECT friend_id FROM friend_status WHERE user_id = ?\n" +
                "INTERSECT \n" +
                "SELECT friend_id FROM friend_status WHERE user_id = ?\n" +
                ")";

        userStorage.findById(firstId);
        userStorage.findById(secondId);

        return jdbcTemplate.query(sqlQuery, userRowMapper, firstId, secondId);
    }
}
