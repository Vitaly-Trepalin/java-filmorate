package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.validation.Validator;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userDbStorage;
    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;
    private final Validator validator;

    public void addFriend(Long userId, Long friendId) {
        log.info("Method started (addFriend)");
        String sqlQuery = "INSERT INTO friend_status VALUES (?, ?)";

        validator.checkForUserInDatabase(userId);
        validator.checkForUserInDatabase(friendId);

        jdbcTemplate.update(sqlQuery, userId, friendId);
        log.info("Friend added");
    }

    public void removeFriend(Long userId, Long friendId) {
        log.info("Method started (removeFriend)");
        String sqlQuery = "DELETE FROM friend_status WHERE user_id = ? AND friend_id = ?";

        validator.checkForUserInDatabase(userId);
        validator.checkForUserInDatabase(friendId);

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

        validator.checkForUserInDatabase(userId);

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

        validator.checkForUserInDatabase(firstId);
        validator.checkForUserInDatabase(secondId);

        return jdbcTemplate.query(sqlQuery, userRowMapper, firstId, secondId);
    }
}
