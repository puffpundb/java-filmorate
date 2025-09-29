package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.mapper.MpaRatingRowMapper;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class MpaRatingDbStorage {
	private final JdbcTemplate jdbcTemplate;

	public List<MpaRating> getAllMpaRating() {
		String sql = "SELECT id, rating FROM mpa_rating";
		return jdbcTemplate.query(sql, new MpaRatingRowMapper());
	}

	public MpaRating getMpaRatingById(int id) {
		String sql = "SELECT id, rating FROM mpa_rating WHERE id = ?";
		return jdbcTemplate.queryForObject(sql, new MpaRatingRowMapper(), id);
	}

	public boolean mpaExist(int id) {
		String sql = "SELECT EXISTS(SELECT 1 FROM mpa_rating WHERE id = ?)";
		return jdbcTemplate.queryForObject(sql, Boolean.class, id);
	}
}
