package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.mapper.GenreRowMapper;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class GenreDbStorage {
	private JdbcTemplate jdbcTemplate;

	public List<Genre> getAllGenre() {
		String sql = "SELECT id, genre_name FROM genre";

		return jdbcTemplate.query(sql, new GenreRowMapper());
	}

	public Genre getGenreById(int id) {
		if (isContain(id)) {
			String sql = "SELECT id, genre_name FROM genre WHERE id = ?";
			return jdbcTemplate.queryForObject(sql, new GenreRowMapper(), id);
		}

		throw new NotFoundException("Жанр не найден");
	}

	public List<Genre> getGenresByFilmId(Long filmId) {
		String sql = "SELECT g.id, g.name FROM genres g JOIN film_genres fg ON g.id = fg.genre_id WHERE fg.film_id = ?";

		return jdbcTemplate.query(sql, new GenreRowMapper(), filmId);
	}

	private boolean isContain(int id) {
		String sql = "SELECT COUNT(*) FROM genre WHERE id = ?";

		return jdbcTemplate.queryForObject(sql, Integer.class, id) > 0;
	}
}
