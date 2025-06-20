package ru.yandex.practicum.filmorate.storage.mappers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.RatingDbStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Component
@Slf4j
public class FilmRowMapper implements RowMapper<Film> {
    GenreDbStorage genreDbStorage;
    RatingDbStorage ratingDbStorage;

    public FilmRowMapper(GenreDbStorage genreDbStorage, RatingDbStorage ratingDbStorage) {
        this.genreDbStorage = genreDbStorage;
        this.ratingDbStorage = ratingDbStorage;
    }

    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        log.info("Method started (mapRow)");
        Rating rating = ratingDbStorage.findById(rs.getInt("rating_id"));
        List<Genre> genres = genreDbStorage.gettingFilmGenres(rs.getLong("film_id"));

        return Film.builder()
                .id(rs.getLong("film_id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .releaseDate(rs.getDate("release_date").toLocalDate())
                .duration(rs.getLong("duration"))
                .genres(genres)
                .mpa(rating)
                .build();
    }
}
