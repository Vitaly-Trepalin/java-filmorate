package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({GenreDbStorage.class, GenreRowMapper.class})
public class GenreDbStorageTest {
    private final GenreDbStorage genreDbStorage;

    @Test
    public void testFindGenreById() {
        Genre genre = genreDbStorage.findById(1);

        assertThat(genre)
                .isNotNull()
                .hasFieldOrPropertyWithValue("id", 1)
                .hasFieldOrPropertyWithValue("name", "Комедия");
    }

    @Test
    public void testFindAllGenres() {
        List<Genre> ratings = genreDbStorage.findAll();

        assertThat(ratings)
                .isNotNull()
                .extracting(Genre::getId, Genre::getName)
                .contains(
                        tuple(1, "Комедия"),
                        tuple(2, "Драма"),
                        tuple(3, "Мультфильм"),
                        tuple(4, "Триллер"),
                        tuple(5, "Документальный"),
                        tuple(6, "Боевик")
                );
    }
}