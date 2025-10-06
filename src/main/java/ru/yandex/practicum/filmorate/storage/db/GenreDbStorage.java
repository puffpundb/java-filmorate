package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.mapper.GenreRowMapper;

import java.util.*;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class GenreDbStorage {
	private final JdbcTemplate jdbcTemplate;

	public List<Genre> getAllGenre() {
		String sql = "SELECT id, genre_name FROM genre";

		return jdbcTemplate.query(sql, new GenreRowMapper());
	}

	public Optional<Genre> getGenreById(int id) {
		String sql = "SELECT id, genre_name FROM genre WHERE id = ?";
		try {
			Genre genre = jdbcTemplate.queryForObject(sql, new GenreRowMapper(), id);
			return Optional.of(genre);
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}

	public Map<Long, Set<Genre>> getGenresByFilmId(Set<Long> filmsId) {
		if (filmsId.isEmpty()) {
			return new HashMap<>();
		}

		String idsStr = filmsId.stream().map(String::valueOf).collect(Collectors.joining(","));
		String filmGenresSql = """
				SELECT
					fg.film_id,
					g.id,
					g.genre_name
				FROM genre g
				JOIN film_genre fg ON g.id = fg.genre_id
				WHERE fg.film_id IN (""" + idsStr + ")" +
				"ORDER BY g.id";
		Map<Long, Set<Genre>> genreMap = new HashMap<>();
		jdbcTemplate.query(filmGenresSql, rs -> {
			Long filmId = rs.getLong("film_id");
			Genre genre = new GenreRowMapper().mapRow(rs, 0);

			if (!genreMap.containsKey(filmId)) {
				genreMap.put(filmId, new HashSet<>());
			}
			genreMap.get(filmId).add(genre);
		});

		return genreMap;
	}

	public void updateGenresInDb(Long id, Set<Genre> genreList) throws DataIntegrityViolationException {
		String sqlDeleteGenres = "DELETE FROM film_genre WHERE film_id = ?";
		jdbcTemplate.update(sqlDeleteGenres, id);

		String genreSql = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
		List<Object[]> genreListId = new ArrayList<>();
		for (Genre genre : genreList) {
			genreListId.add(new Object[]{id, genre.getId()});
		}

		jdbcTemplate.batchUpdate(genreSql, genreListId);
	}

	public boolean genreExists(Set<Integer> genreIds) {
		String placeholders = String.join(",", Collections.nCopies(genreIds.size(), "?"));
		String sql = "SELECT COUNT(*) = " + genreIds.size() + " FROM genre WHERE id IN (" + placeholders + ")";

		return jdbcTemplate.queryForObject(sql, Boolean.class, genreIds.toArray());
	}
}
