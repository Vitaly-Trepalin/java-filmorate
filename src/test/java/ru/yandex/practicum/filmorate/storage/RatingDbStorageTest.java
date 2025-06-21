package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.mappers.RatingRowMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({RatingDbStorage.class, RatingRowMapper.class})
public class RatingDbStorageTest {
    private final RatingDbStorage ratingDbStorage;

    @Test
    public void testFindRatingById() {
        Rating rating = ratingDbStorage.findById(1);

        assertThat(rating)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", 1)
                .hasFieldOrPropertyWithValue("name", "G");
    }

    @Test
    public void testFindAllRatings() {
        List<Rating> ratings = ratingDbStorage.findAll();

        assertThat(ratings)
                .isNotNull()
                .extracting(Rating::getId, Rating::getName)
                .contains(
                        tuple(1, "G"),
                        tuple(2, "PG"),
                        tuple(3, "PG-13"),
                        tuple(4, "R"),
                        tuple(5, "NC-17")
                );
    }
}
