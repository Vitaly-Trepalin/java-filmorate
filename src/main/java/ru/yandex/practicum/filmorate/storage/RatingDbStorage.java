package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.mappers.RatingRowMapper;

import java.util.Comparator;
import java.util.List;


@Repository
@RequiredArgsConstructor
@Slf4j
public class RatingDbStorage {
    private final JdbcTemplate jdbcTemplate;
    private final RatingRowMapper ratingRowMapper;

    public List<Rating> findAll() {
        log.info("Method started (gettingListOfRatings)");
        String sqlQuery = "SELECT * FROM rating";
        List<Rating> ratings = jdbcTemplate.query(sqlQuery, ratingRowMapper);

        return ratings.stream()
                .sorted(Comparator.comparing(Rating::getId))
                .toList();
    }

    public Rating findById(Integer id) {
        log.info("Method started (gettingRatingById)");
        String sqlQuery = "SELECT * FROM rating WHERE rating_id = ?";
        try {
            return jdbcTemplate.queryForObject(sqlQuery, ratingRowMapper, id);
        } catch (EmptyResultDataAccessException e) {
            log.warn("No rating with id={}", id);
            throw new NotFoundException("Нет рейтинга с id=" + id);
        }
    }
}