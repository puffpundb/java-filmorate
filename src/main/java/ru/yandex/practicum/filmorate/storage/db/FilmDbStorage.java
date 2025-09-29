package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.model.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@Qualifier("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
	private final JdbcTemplate jdbcTemplate;

	@Override
	public List<Film> getAllFilms() {
		String filmsSql = """
				SELECT
					f.id,
					f.name,
					f.description,
					f.release_date,
					f.duration,
					f.mpa_rating_id,
					m.rating AS mpa_rating_name
				FROM films f
				LEFT JOIN mpa_rating m ON f.mpa_rating_id = m.id
				""";

		Map<Long, Film> filmMap = new HashMap<>();

		jdbcTemplate.query(filmsSql, (rs, rowNum) -> {
			Film film = new FilmRowMapper().mapRow(rs, rowNum);
			filmMap.put(film.getId(), film);
			return null;
		});

		if (filmMap.isEmpty()) {
			return new ArrayList<>();
		}

		String filmIdsStr = filmMap.keySet().stream()
				.map(String::valueOf)
				.collect(Collectors.joining(","));
		String filmGenresSql = """
			SELECT
				g.id,
				g.genre_name,
				fg.film_id
			FROM genre g
			JOIN film_genre fg ON g.id = fg.genre_id
			WHERE fg.film_id IN (""" + filmIdsStr + ") ORDER BY g.id";

		Map<Long, Set<Integer>> filmGenreIdsMap = new HashMap<>();
		jdbcTemplate.query(filmGenresSql, (rs, rowNum) -> {
			Long filmId = rs.getLong("film_id");
			Integer genreId = rs.getInt("id");

			if (!filmGenreIdsMap.containsKey(filmId)) {
				filmGenreIdsMap.put(filmId, new HashSet<>());
			}
			filmGenreIdsMap.get(filmId).add(genreId);

			return null;
		});

		Set<Integer> genreIds = new HashSet<>();
		for (Set<Integer> ids : filmGenreIdsMap.values()) {
			genreIds.addAll(ids);
		}

		Map<Integer, Genre> genreMap = new HashMap<>();
		if (!genreIds.isEmpty()) {
			String genreIdsStr = genreIds.stream()
					.map(String::valueOf)
					.collect(Collectors.joining(","));
			String genresSql = "SELECT id, genre_name FROM genre WHERE id IN (" + genreIdsStr + ")";

			jdbcTemplate.query(genresSql, (rs, rowNum) -> {
				Genre genre = new GenreRowMapper().mapRow(rs, rowNum);
				genreMap.put(genre.getId(), genre);
				return null;
			});
		}

		for (Film film : filmMap.values()) {
			Set<Integer> genreIdSet = filmGenreIdsMap.get(film.getId());
			if (genreIdSet != null) {
				Set<Genre> genres = new LinkedHashSet<>();
				for (Integer genreId : genreIdSet) {
					Genre genre = genreMap.get(genreId);
					if (genre != null) {
						genres.add(genre);
					}
				}
				film.setGenres(genres);
			}

			Set<Long> likes = getLikes(film.getId());
			film.setUsersLike(likes);
		}

		return new ArrayList<>(filmMap.values());
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
			if (newFilm.getMpa() != null) ps.setObject(5, newFilm.getMpa().getId());
			else ps.setObject(5, null);
			return ps;
		}, keyHolder);

		newFilm.setId(keyHolder.getKeyAs(Long.class));

		if (newFilm.getGenres() != null && !newFilm.getGenres().isEmpty()) {
			String genreSql = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
			for (Genre genre : newFilm.getGenres()) {
				jdbcTemplate.update(genreSql, newFilm.getId(), genre.getId());
			}
		}

		return getFilm(newFilm.getId());
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
		if (newFilmData.getMpa() != null) {
			currentDbFilm.setMpa(newFilmData.getMpa());
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
				currentDbFilm.getMpa().getId(),
				currentFilmId);

		if (newFilmData.getGenres() != null) {
			String sqlDeleteGenres = "DELETE FROM film_genre WHERE film_id = ?";
			jdbcTemplate.update(sqlDeleteGenres, currentFilmId);

			String sqlInsertNewGenres = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
			for (Genre genre : currentDbFilm.getGenres()) {
				jdbcTemplate.update(sqlInsertNewGenres, currentFilmId, genre.getId());
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
				f.id,
				f.name,
				f.description,
				f.release_date,
				f.duration,
				f.mpa_rating_id,
				m.rating AS mpa_rating_name
			FROM films f
			LEFT JOIN mpa_rating m ON f.mpa_rating_id = m.id
			WHERE f.id = ?
			""";
		List<Film> film = jdbcTemplate.query(filmSql, new FilmRowMapper(), id);
		Film currentFilm = film.getFirst();

		String genreSql = """
			SELECT
				g.id,
				g.genre_name
			FROM genre g
			JOIN film_genre fg ON g.id = fg.genre_id
			WHERE fg.film_id = ?
			ORDER BY g.id
			""";
		List<Genre> genres = jdbcTemplate.query(genreSql, new GenreRowMapper(), id);
		currentFilm.setGenres(new LinkedHashSet<>(genres));

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
		String sql = "SELECT user_id FROM films_likes WHERE film_id = ?";
		return new HashSet<>(jdbcTemplate.queryForList(sql, Long.class, filmId));
	}
}
