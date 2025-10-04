package ru.yandex.practicum.filmorate.service.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
public class FilmServiceImpl implements FilmService {
	private final FilmStorage filmDataBase;

	@Autowired
	public FilmServiceImpl(@Qualifier("filmDbStorage") FilmStorage filmDataBase) {
		this.filmDataBase = filmDataBase;
	}

	@Override
	public void liked(Long filmId, Long userId) {
		try {
			filmDataBase.addLike(filmId, userId);
		} catch (DataIntegrityViolationException e) {
			throw new NotFoundException(String.format("Не найден пользователь с id = %d или фильм с id = %d", userId, filmId));
		}
	}

	@Override
	public void disliked(Long filmId, Long userId) {
		filmDataBase.removeLike(filmId, userId);
	}

	@Override
	public List<Film> getMostRatedFilms(int count) {
		if (count < 0) {
			throw new ValidationException("Передан отрицательный параметр");
		}

		return filmDataBase.getPopularFilms(count);
	}

	@Override
	public List<Film> getAllFilm() {
		return filmDataBase.getAllFilms();
	}

	@Override
	public Film createFilm(Film newFilm) {
		validateCreateFilm(newFilm);

		try {
			Optional<Film> optionalFilm = filmDataBase.createFilm(newFilm);
			if (optionalFilm.isEmpty()) {
				throw new NotFoundException(String.format("Рейтинг с id = %d не найден", newFilm.getMpa().getId()));
			}

			return optionalFilm.get();

		} catch (DataIntegrityViolationException e) {
			throw new NotFoundException("В жанрах передан неверный id");
		}
	}

	@Override
	public Film updateFilm(Film newFilmData) { // Не придумал как разделить разные несовпадения полей... Пришло в голову только через исключение ловить жанры, а через Optional ловить рейтинг и id фильма
		validateUpdateFilm(newFilmData);

		try {
			Optional<Film> film = filmDataBase.updateFilm(newFilmData);
			if (film.isEmpty()) {
				throw new NotFoundException(String.format("Фильм с id = %d не найден или рейтинг с id = %d",
						newFilmData.getId(), newFilmData.getMpa().getId()));
			}

			return film.get();

		} catch (DataIntegrityViolationException e) {
			throw new NotFoundException("В жанрах передан неверный id");
		}
	}

	@Override
	public Set<Long> getLikes(Long id) {
		try {
			Map<Long, Set<Long>> filmLikes = filmDataBase.getLikes(Collections.singleton(id));
			return filmLikes.get(id);

		} catch (DataIntegrityViolationException e) {
			throw new NotFoundException(String.format("Фильм с id = %d не найден", id));
		}
	}

	@Override
	public Film getFilm(Long id) {
		Optional<Film> film = filmDataBase.getFilm(id);
		if (film.isPresent()) {
			return film.get();
		}

		throw new NotFoundException(String.format("Фильм с id = %d не найден", id));
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
