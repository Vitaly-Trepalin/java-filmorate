package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.validation.Validator;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository("UserDbStorage")
@Slf4j
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;
    private final Validator validator;
    private final UserRowMapper userRowMapper;

    @Override
    public List<User> findAll() {
        log.info("Method started (findAll)");
        String sqlQuery = "SELECT * FROM \"user\"";

        return jdbcTemplate.query(sqlQuery, userRowMapper);
    }

    @Override
    public User findById(Long id) {
        log.info("Method started (findById)");
        String sqlQuery = "SELECT * FROM \"user\" WHERE user_id = ?";

        try {
            return jdbcTemplate.queryForObject(sqlQuery, userRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            log.warn("No user with id={}", id);
            throw new NotFoundException("Нет пользователя с id=" + id);
        }
    }

    @Override
    public User create(User user) {
        log.info("Method started (create)");
        String sqlQuery = "INSERT INTO \"user\" (name, email, login, birthday) VALUES(?, ?, ?, ?)";

        validator.userValidation(user); // валидация полей email, login, birthday
        try { // проверка уникальности email
            String queryEmail = "SELECT email FROM \"user\" WHERE email = ?";
            jdbcTemplate.queryForObject(queryEmail, String.class, user.getEmail());

            log.warn("This email = {} already exists. User not created.", user.getEmail());
            throw new ValidationException("Email = " + user.getEmail() + " уже существует. Пользователь не создан");
        } catch (EmptyResultDataAccessException e) {
            log.info("Email validation passed");
        }
        try { // проверка уникальности login
            String queryLogin = "SELECT login FROM \"user\" WHERE login = ?";
            jdbcTemplate.queryForObject(queryLogin, String.class, user.getLogin());

            log.warn("This login = {} already exists. User not created.", user.getLogin());
            throw new ValidationException("Login = " + user.getLogin() + " уже существует. Пользователь не создан");
        } catch (EmptyResultDataAccessException e) {
            log.info("Login validation passed");
        }

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getLogin());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        Long userId = keyHolder.getKeyAs(Long.class);
        if (userId == null) {
            throw new InternalServerException("Не удалось сохранить данные");
        }

        user.setId(userId);
        log.info("Adding a new user id={}", user.getId());
        return user;
    }

    @Override
    public User update(User user) {
        log.info("Method started (update)");
        String sqlQuery = "UPDATE \"user\" SET name = ?, email = ?, login = ?, birthday = ? WHERE user_id = ?";

        findById(user.getId()); // проверка на наличие пользователя с заданным id
        validator.userValidation(user);

        jdbcTemplate.update(sqlQuery,
                user.getName(),
                user.getEmail(),
                user.getLogin(),
                Date.valueOf(user.getBirthday()),
                user.getId());
        log.info("User with id={} updated", user.getId());
        return user;
    }

    @Override
    public void delete(Long id) {
        log.info("Method started (delete)");
        String sqlQuery = "DELETE FROM \"user\" WHERE user_id = ?";

        findById(id); // проверка на наличие пользователя с заданным id

        log.info("User with id={} delete", id);
        jdbcTemplate.update(sqlQuery, id);
    }
}
