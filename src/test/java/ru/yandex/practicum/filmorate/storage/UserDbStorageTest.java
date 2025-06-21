package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.validation.Validator;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, Validator.class, UserRowMapper.class})
class UserDbStorageTest {
    private final UserDbStorage userDbStorage;
    private final JdbcTemplate jdbcTemplate;
    private final UserRowMapper userRowMapper;

    @BeforeEach
    public void setUp() {
        String sqlQuery = "INSERT INTO \"user\" (name, email, login, birthday) VALUES(?, ?, ?, ?)";

        User user1 = User.builder()
                .name("Vitaly")
                .email("ya1@mail.ru")
                .login("Vit")
                .birthday(LocalDate.of(1990, 6, 18))
                .build();

        User user2 = User.builder()
                .name("Maksim")
                .email("ya2@mail.ru")
                .login("Mak")
                .birthday(LocalDate.of(1998, 8, 18))
                .build();

        List<User> users = List.of(user1, user2);
        users.forEach(user -> jdbcTemplate.update(sqlQuery,
                user.getName(),
                user.getEmail(),
                user.getLogin(),
                Date.valueOf(user.getBirthday())));
    }

    @AfterEach
    public void shutDown() {
        jdbcTemplate.execute("ALTER TABLE \"user\" ALTER COLUMN user_id RESTART WITH 1");
    }

    @Test
    public void testFindUserById() {
        User user = userDbStorage.findById(1L);

        assertThat(user)
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", "Vitaly")
                .hasFieldOrPropertyWithValue("email", "ya1@mail.ru")
                .hasFieldOrPropertyWithValue("login", "Vit")
                .hasFieldOrPropertyWithValue("birthday", LocalDate.of(1990, 6, 18));
    }

    @Test
    public void testFindAllUsers() {
        List<User> users = userDbStorage.findAll();

        assertThat(users)
                .isNotNull()
                .extracting(User::getName, User::getEmail, User::getLogin, User::getBirthday)
                .contains(
                        tuple("Vitaly", "ya1@mail.ru", "Vit", LocalDate.of(1990, 6, 18)),
                        tuple("Maksim", "ya2@mail.ru", "Mak", LocalDate.of(1998, 8, 18))
                );
    }

    @Test
    public void testCreateUser() {
        User expectedUser = User.builder()
                .name("Vladimir")
                .email("ya3@mail.ru")
                .login("Vladim")
                .birthday(LocalDate.of(1996, 3, 10))
                .build();

        userDbStorage.create(expectedUser);

        String sqlQuery = "SELECT * FROM \"user\" WHERE user_id = ?";
        User actualUser = jdbcTemplate.queryForObject(sqlQuery, userRowMapper, 3);

        assertThat(actualUser)
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", "Vladimir")
                .hasFieldOrPropertyWithValue("email", "ya3@mail.ru")
                .hasFieldOrPropertyWithValue("login", "Vladim")
                .hasFieldOrPropertyWithValue("birthday", LocalDate.of(1996, 3, 10));
    }

    @Test
    public void testUpdateUser() {
        User expectedUser = User.builder()
                .id(2L)
                .name("Vladimir")
                .email("ya3@mail.ru")
                .login("Vladim")
                .birthday(LocalDate.of(1996, 3, 10))
                .build();

        userDbStorage.update(expectedUser);

        String sqlQuery = "SELECT * FROM \"user\" WHERE user_id = ?";
        User actualUser = jdbcTemplate.queryForObject(sqlQuery, userRowMapper, 2);

        assertThat(actualUser)
                .isNotNull()
                .hasFieldOrPropertyWithValue("name", "Vladimir")
                .hasFieldOrPropertyWithValue("email", "ya3@mail.ru")
                .hasFieldOrPropertyWithValue("login", "Vladim")
                .hasFieldOrPropertyWithValue("birthday", LocalDate.of(1996, 3, 10));
    }

    @Test
    public void testDeleteUser() {
        userDbStorage.delete(1L);
        userDbStorage.delete(2L);

        String sqlQuery = "SELECT * FROM \"user\"";
        List<User> users = jdbcTemplate.query(sqlQuery, userRowMapper);

        assertThat(users).isEmpty();
    }
}
