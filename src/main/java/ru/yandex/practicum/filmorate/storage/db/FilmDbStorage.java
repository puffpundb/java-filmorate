package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Repository
@Qualifier("filmDbStorage")
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
	private final JdbcTemplate jdbcTemplate;
	private Integer currentMaxId = 0;

	@Override
	public List<Film> getAllFilm() { // расскажут на вебинаре
		String sql = """
        SELECT
        	f.id,
        	f.name,
        	f.description,
        	f.release_date,
        	f.duration,
        	f.mpa_rating_id,
        FROM films f
        LEFT JOIN mpa_rating m ON f.mpa_rating_id = m.id
        """;

		List<Film> films = jdbcTemplate.query(sql, new FilmRowMapper());


		return jdbcTemplate.query(sql, new FilmRowMapper());
	}

	@Override
	public Film createFilm(Film newFilm) {
		validateCreateFilm(newFilm);

		newFilm.setId(generateId());

		String sql = "INSERT INTO films (id, name, description, release_date, duration, mpa_rating_id) VALUES (?, ?, ?, ?, ?, ?)";
		jdbcTemplate.update(sql,
				newFilm.getId(),
				newFilm.getName(),
				newFilm.getDescription(),
				newFilm.getReleaseDate(),
				newFilm.getDuration(),
				newFilm.getMpaRating());


		String genreSql = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
		for (Integer genre : newFilm.getGenres()) {
			jdbcTemplate.update(genreSql, newFilm.getId(), genre);
		}

		return newFilm;
	}

	@Override
	public Film updateFilm(Film newFilmData) {
		validateUpdateFilm(newFilmData);
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
	public boolean isContain(Long id) {
		String sql = "SELECT COUNT(*) FROM films WHERE id = ?";

		return jdbcTemplate.queryForObject(sql, Integer.class, id) > 0;
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
		if (isContain(filmId)) {
			String sql = "INSERT INTO films_likes (film_id, user_id) VALUES (?, ?)";
			jdbcTemplate.update(sql, filmId, userId);

		} else {
			throw new NotFoundException("Фильм не найден");
		}
	}

	@Override
	public void removeLike(Long filmId, Long userId) {
		if (isContain(filmId)) {
			String sql = "DELETE FROM films_likes WHERE film_id = ? AND user_id = ?";
			jdbcTemplate.update(sql, filmId, userId);

		} else {
			throw new NotFoundException("Фильм не найден");

		}
	}

	@Override
	public Set<Long> getLikes(Long filmId) {
		if (isContain(filmId)) {
			String sql = "SELECT user_id FROM films_likes WHERE film_id = ?";
			return new HashSet<>(jdbcTemplate.queryForList(sql, Long.class, filmId));
		}

		throw new NotFoundException("Фильм не найден");
	}

	private Integer generateId() {
		return ++currentMaxId;
	}

	private void validateCreateFilm(Film currentFilm) {
		if (currentFilm.getName() == null || currentFilm.getName().isBlank()) {
			log.warn("Ошибка валидации: Название фильма пустое или null");
			throw new ValidationException("Название не должно быть пустым или null");
		}
		if (currentFilm.getDescription() == null || currentFilm.getDescription().length() > 200) {
			log.warn("Ошибка валидации: Превышен лимит символов описания или описание null");
			throw new ValidationException("Максимальная длинна описания 200 символов или описание null");
		}
		if (currentFilm.getReleaseDate() == null || currentFilm.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
			log.warn("Ошибка валидации: Слишком ранняя дата релиза или дата null");
			throw new ValidationException("Дата релиза не должна быть раньше 28.12.1895 г. или null");
		}
		if (currentFilm.getDuration() == null || currentFilm.getDuration() < 0) {
			log.warn("Ошибка валидации: Отрицательная продолжительность или продолжительность null");
			throw new ValidationException("Продолжительность не может быть отрицательной или null");
		}
	}

	private void validateUpdateFilm(Film newFilmData) {
		if (!isContain(newFilmData.getId())) {
			throw new NotFoundException("Фильм не найден");
		}
		if (newFilmData.getName() != null && newFilmData.getName().isBlank()) {
			log.warn("Ошибка валидации: Название фильма пустое или null");
			throw new ValidationException("Название не должно быть пустым или null");
		}
		if (newFilmData.getDescription() != null && newFilmData.getDescription().length() > 200) {
			log.warn("Ошибка валидации: Превышен лимит символов описания или описание null");
			throw new ValidationException("Максимальная длинна описания 200 символов или описание null");
		}
		if (newFilmData.getReleaseDate() != null && newFilmData.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
			log.warn("Ошибка валидации: Слишком ранняя дата релиза или дата null");
			throw new ValidationException("Дата релиза не должна быть раньше 28.12.1895 г. или null");
		}
		if (newFilmData.getDuration() != null && newFilmData.getDuration() < 0) {
			log.warn("Ошибка валидации: Отрицательная продолжительность или продолжительность null");
			throw new ValidationException("Продолжительность не может быть отрицательной или null");
		}
	}
}
