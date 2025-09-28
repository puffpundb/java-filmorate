package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

@Repository
@Qualifier("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
	private final JdbcTemplate jdbcTemplate;
	private Integer currentMaxId = 0;

	@Override
	public List<Film> getAllFilm() {
		String sql = """
        SELECT
        	f.id,
        	f.name,
        	f.description,
        	f.release_date,
        	f.duration,
        	f.mpa_rating_id,
        	fg.genre_id
        FROM films f
        LEFT JOIN film_genre fg ON f.id = fg.film_id
        """;

		FilmRowMapper filmRowMapper = new FilmRowMapper();

		return jdbcTemplate.query(sql, (ResultSet rs) -> {
			Map<Long, Film> filmMap = new HashMap<>();

			while (rs.next()) {
				Long filmId = rs.getLong("id");
				Film film = filmMap.get(filmId);

				if (film == null) {
					film = filmRowMapper.mapRow(rs, 0);
					filmMap.put(filmId, film);
				}

				Integer genreId = rs.getObject("genre_id", Integer.class);
				if (genreId != null) {
					film.getGenres().add(genreId);
				}
			}

			return new ArrayList<>(filmMap.values());
		});
	}

	@Override
	public Film createFilm(Film newFilm) {
		GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
		String sql = "INSERT INTO films (name, description, release_date, duration, mpa_rating_id) VALUES (?, ?, ?, ?, ?)";
		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
			ps.setString(1, newFilm.getName());
			ps.setString(2, newFilm.getDescription());
			ps.setDate(3, Date.valueOf(newFilm.getReleaseDate()));
			ps.setInt(4, newFilm.getDuration());
			ps.setInt(5, newFilm.getMpaRating());
			return ps;
		}, keyHolder);

		newFilm.setId(keyHolder.getKeyAs(Long.class));

		String genreSql = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
		for (Integer genre : newFilm.getGenres()) {
			jdbcTemplate.update(genreSql, newFilm.getId(), genre);
		}

		return newFilm;
	}

	@Override
	public Film updateFilm(Film newFilmData) {
		Film currentDbFilm = getFilm(newFilmData.getId());
		if (newFilmData.getName() != null) {
			currentDbFilm.setName(newFilmData.getName());
		}
		if (newFilmData.getDescription() != null) {
			currentDbFilm.setDescription(newFilmData.getDescription());
		}
		if (newFilmData.getReleaseDate() != null) {
			currentDbFilm.setReleaseDate(newFilmData.getReleaseDate());
		}
		if (newFilmData.getDuration() != null) {
			currentDbFilm.setDuration(newFilmData.getDuration());
		}
		if (newFilmData.getMpaRating() != null) {
			currentDbFilm.setMpaRating(newFilmData.getMpaRating());
		}
		if (newFilmData.getGenres() != null) {
			currentDbFilm.setGenres(newFilmData.getGenres());
		}

		Long currentFilmId = currentDbFilm.getId();

		String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? WHERE id = ?";
		jdbcTemplate.update(sql,
				currentDbFilm.getName(),
				currentDbFilm.getDescription(),
				currentDbFilm.getReleaseDate(),
				currentDbFilm.getDuration(),
				currentDbFilm.getMpaRating(),
				currentFilmId);

		if (newFilmData.getGenres() != null) {
			String sqlDeleteGenres = "DELETE FROM film_genre WHERE film_id = ?";
			jdbcTemplate.update(sqlDeleteGenres, currentFilmId);

			String sqlInsertNewGenres = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
			for (Integer genre : currentDbFilm.getGenres()) {
				jdbcTemplate.update(sqlInsertNewGenres, currentFilmId, genre);
			}
		}

		return currentDbFilm;
	}

	@Override
	public boolean filmExist(Long id) {
		String sql = "SELECT EXISTS(SELECT 1 FROM films WHERE id = ?)";
		return jdbcTemplate.queryForObject(sql, Boolean.class, id);
	}

	@Override
	public Film getFilm(Long id) {
		String filmSql = """
				SELECT
					id,
					name,
					description,
					release_date,
					duration,
					mpa_rating_id
				FROM films
				WHERE id = ?
				""";
		List<Film> film = jdbcTemplate.query(filmSql, new FilmRowMapper(), id);
		if (film.isEmpty()) {
			throw new NotFoundException("Фильм не найден");
		}

		String genreSql = """
			SELECT genre_id
			FROM film_genre
			WHERE film_id = ?
			""";
		List<Integer> genres = jdbcTemplate.queryForList(genreSql, Integer.class, id);
		Film currentFilm = film.getFirst();
		currentFilm.setGenres(new HashSet<>(genres));

		Set<Long> likes = getLikes(id);
		currentFilm.setUsersLike(likes);

		return currentFilm;
	}

	@Override
	public void addLike(Long filmId, Long userId) {
		String sql = "INSERT INTO films_likes (film_id, user_id) VALUES (?, ?)";
		jdbcTemplate.update(sql, filmId, userId);
	}

	@Override
	public void removeLike(Long filmId, Long userId) {
		String sql = "DELETE FROM films_likes WHERE film_id = ? AND user_id = ?";
		jdbcTemplate.update(sql, filmId, userId);
	}

	@Override
	public Set<Long> getLikes(Long filmId) {
		if (filmExist(filmId)) {
			String sql = "SELECT user_id FROM films_likes WHERE film_id = ?";
			return new HashSet<>(jdbcTemplate.queryForList(sql, Long.class, filmId));
		}

		throw new NotFoundException("Фильм не найден");
	}
}
