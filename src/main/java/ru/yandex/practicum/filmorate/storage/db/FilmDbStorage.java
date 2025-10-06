package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.mapper.FilmRowMapper;
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
	private final GenreDbStorage genreDbStorage;

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

		jdbcTemplate.query(filmsSql, rs -> {
			Film film = new FilmRowMapper().mapRow(rs, 0);
			filmMap.put(film.getId(), film);
		});

		if (filmMap.isEmpty()) {
			return new ArrayList<>();
		}

		Map<Long, Set<Genre>> genreMap = genreDbStorage.getGenresByFilmId(filmMap.keySet());
		Map<Long, Set<Long>> likesMap = getLikes(filmMap.keySet());
		for (Film currentFilm : filmMap.values()) {
			currentFilm.setGenres(genreMap.getOrDefault(currentFilm.getId(), Collections.emptySet()));
			currentFilm.setUsersLike(likesMap.getOrDefault(currentFilm.getId(), Collections.emptySet()));
		}

		return new ArrayList<>(filmMap.values());
	}

	@Override
	public Film createFilm(Film newFilm) throws DataIntegrityViolationException {
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
			genreDbStorage.updateGenresInDb(newFilm.getId(), newFilm.getGenres());
		}

		return newFilm;
	}

	@Override
	public Optional<Film> updateFilm(Film newFilmData) {
		Optional<Film> optionalDbFilm = getFilm(newFilmData.getId());
		if (optionalDbFilm.isEmpty()) {
			return optionalDbFilm;
		}

		Film currentDbFilm = optionalDbFilm.get();
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
			genreDbStorage.updateGenresInDb(currentDbFilm.getId(), newFilmData.getGenres());
		}

		String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? WHERE id = ?";
		jdbcTemplate.update(sql,
				currentDbFilm.getName(),
				currentDbFilm.getDescription(),
				currentDbFilm.getReleaseDate(),
				currentDbFilm.getDuration(),
				currentDbFilm.getMpa().getId(),
				currentDbFilm.getId());

		return Optional.of(currentDbFilm);
	}

	@Override
	public Optional<Film> getFilm(Long id) {
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

		Film currentFilm;
		try {
			currentFilm = jdbcTemplate.queryForObject(filmSql, new FilmRowMapper(), id);
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}

		currentFilm.setGenres(genreDbStorage.getGenresByFilmId(Collections.singleton(id)).get(id));
		currentFilm.setUsersLike(getLikes(new HashSet<>(Collections.singleton(id))).get(id));

		return Optional.of(currentFilm);
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
	public Map<Long, Set<Long>> getLikes(Set<Long> filmsId) throws DataIntegrityViolationException {
		if (filmsId.isEmpty()) {
			return new HashMap<>();
		}

		String idsStr = filmsId.stream().map(String::valueOf).collect(Collectors.joining(","));
		String filmLikesSql = """
				SELECT user_id,
					film_id
				FROM films_likes
				WHERE film_id IN (""" + idsStr + ")";

		Map<Long, Set<Long>> likesMap = new HashMap<>();
		jdbcTemplate.query(filmLikesSql, rs -> {
			Long filmId = rs.getLong("film_id");
			Long likeId = rs.getLong("user_id");

			if (!likesMap.containsKey(filmId)) {
				likesMap.put(filmId, new HashSet<>());
			}
			likesMap.get(filmId).add(likeId);
		});

		return likesMap;
	}

	@Override
	public List<Film> getPopularFilms(Integer count) {
		String filmsSql = """
        SELECT
            f.id,
            f.name,
            f.description,
            f.release_date,
            f.duration,
            f.mpa_rating_id,
            m.rating AS mpa_rating_name,
            COUNT(fl.user_id) as like_count
        FROM films f
        LEFT JOIN mpa_rating m ON f.mpa_rating_id = m.id
        LEFT JOIN films_likes fl ON f.id = fl.film_id
        GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, m.rating
        ORDER BY like_count DESC, f.id ASC
        LIMIT ?
        """;

		Map<Long, Film> filmMap = new LinkedHashMap<>();

		jdbcTemplate.query(filmsSql, rs -> {
			Film film = new FilmRowMapper().mapRow(rs, 0);
			filmMap.put(film.getId(), film);
		}, count);
		if (filmMap.isEmpty()) {
			return new ArrayList<>();
		}

		Map<Long, Set<Genre>> filmGenresMap = genreDbStorage.getGenresByFilmId(filmMap.keySet());
		Map<Long, Set<Long>> filmLikesMap = getLikes(filmMap.keySet());

		for (Film film : filmMap.values()) {
			Set<Genre> genres = filmGenresMap.getOrDefault(film.getId(), new LinkedHashSet<>());
			film.setGenres(genres);

			Set<Long> likes = filmLikesMap.getOrDefault(film.getId(), new HashSet<>());
			film.setUsersLike(likes);
		}

		return new ArrayList<>(filmMap.values());
	}

	@Override
	public boolean filmExist(Long id) {
		String sql = "SELECT EXISTS(SELECT 1 FROM films WHERE id = ?)";
		return jdbcTemplate.queryForObject(sql, Boolean.class, id);
	}
}
