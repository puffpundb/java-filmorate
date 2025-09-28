package ru.yandex.practicum.filmorate.service.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class FilmServiceImpl implements FilmService {
	@Qualifier("filmDbStorage")
	private FilmStorage filmDataBase;
	@Qualifier("userDbStorage")
	private UserStorage userDataBase;

	private final String NOT_FOUND = "Фильм не найден";

	@Autowired
	public FilmServiceImpl(@Qualifier("filmDbStorage") FilmStorage filmDataBase, @Qualifier("userDbStorage") UserStorage userDataBase) {
		this.filmDataBase = filmDataBase;
		this.userDataBase = userDataBase;
	}

	@Override
	public void liked(Long filmId, Long userId) {
		if (filmDataBase.filmExist(filmId)) {
			throw new NotFoundException(NOT_FOUND);
		}
		if (userDataBase.userExist(userId)) {
			throw new NotFoundException("Пользователь не найден");
		}

		filmDataBase.addLike(filmId, userId);
	}

	@Override
	public void disliked(Long filmId, Long userId) {
		if (filmDataBase.filmExist(filmId)) {
			throw new NotFoundException(NOT_FOUND);
		}
		if (userDataBase.userExist(userId)) {
			throw new NotFoundException("Пользователь не найден");
		}

		filmDataBase.removeLike(filmId, userId);
	}

	@Override
	public List<Film> getMostRatedFilms(int count) {
		if (count < 0) {
			throw new ValidationException("Передан отрицательный параметр");
		}

		return filmDataBase.getAllFilm().stream()
				.sorted()
				.limit(count)
				.toList();
	}

	@Override
	public List<Film> getAllFilm() {
		return filmDataBase.getAllFilm();
	}

	@Override
	public Film createFilm(Film newFilm) {
		validateCreateFilm(newFilm);

		return filmDataBase.createFilm(newFilm);
	}

	@Override
	public Film updateFilm(Film newFilmData) {
		if (filmDataBase.filmExist(newFilmData.getId())) {
			validateUpdateFilm(newFilmData);
			return filmDataBase.updateFilm(newFilmData);
		}

		throw new NotFoundException(NOT_FOUND);
	}

	@Override
	public Set<Long> getLikes(Long id) {
		if (filmDataBase.filmExist(id)) {
			return filmDataBase.getLikes(id);
		}

		throw new NotFoundException(NOT_FOUND);
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
