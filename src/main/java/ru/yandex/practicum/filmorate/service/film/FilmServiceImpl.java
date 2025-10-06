package ru.yandex.practicum.filmorate.service.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.db.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.db.MpaRatingDbStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmServiceImpl implements FilmService {
	private final FilmStorage filmDataBase;
	private final UserStorage userStorage;
	private final GenreDbStorage genreDbStorage;
	private final MpaRatingDbStorage mpaRatingDbStorage;

	@Autowired
	public FilmServiceImpl(@Qualifier("filmDbStorage") FilmStorage filmDataBase, @Qualifier("userDbStorage") UserStorage userStorage, GenreDbStorage genreDbStorage, MpaRatingDbStorage mpaRatingDbStorage) {
		this.filmDataBase = filmDataBase;
		this.userStorage = userStorage;
		this.genreDbStorage = genreDbStorage;
		this.mpaRatingDbStorage = mpaRatingDbStorage;
	}

	@Override
	public void liked(Long filmId, Long userId) {
		if (!filmDataBase.filmExist(filmId)) {
			throw new NotFoundException(String.format("Фильм с id = %d не найден", filmId));
		}
		if (!userStorage.userExist(userId)) {
			throw new NotFoundException(String.format("Пользователь с id = %d не найден", userId));
		}

		filmDataBase.addLike(filmId, userId);
	}

	@Override
	public void disliked(Long filmId, Long userId) {
		if (!filmDataBase.filmExist(filmId)) {
			throw new NotFoundException(String.format("Фильм с id = %d не найден", filmId));
		}
		if (!userStorage.userExist(userId)) {
			throw new NotFoundException(String.format("Пользователь с id = %d не найден", userId));
		}

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

		if (newFilm.getGenres() != null) {
			Set<Integer> genresId = newFilm.getGenres().stream().map(Genre::getId).collect(Collectors.toSet());
			if (!genreDbStorage.genreExists(genresId)) {
				throw new NotFoundException("Переданы неверные id жанров");
			}
		}

		if (newFilm.getMpa() != null) {
			if (!mpaRatingDbStorage.ratingExist(newFilm.getMpa().getId())) {
				throw new NotFoundException(String.format("Рейтинг с id = %d не найден", newFilm.getMpa().getId()));
			}
		}

		return filmDataBase.createFilm(newFilm);
	}

	@Override
	public Film updateFilm(Film newFilmData) {
		validateUpdateFilm(newFilmData);

		if (newFilmData.getGenres() != null) {
			Set<Integer> genresId = newFilmData.getGenres().stream().map(Genre::getId).collect(Collectors.toSet());
			if (!genreDbStorage.genreExists(genresId)) {
				throw new NotFoundException("Переданы неверные id жанров");
			}
		}
		if (newFilmData.getMpa() != null) {
			if (!mpaRatingDbStorage.ratingExist(newFilmData.getMpa().getId())) {
				throw new NotFoundException(String.format("Рейтинг с id = %d не найден", newFilmData.getMpa().getId()));
			}
		}

		Optional<Film> film = filmDataBase.updateFilm(newFilmData);
		if (film.isEmpty()) {
			throw new NotFoundException(String.format("Фильм с id = %d не найден", newFilmData.getId()));
		}

		return film.get();
	}

	@Override
	public List<Long> getLikes(Long id) {
		if (!filmDataBase.filmExist(id)) {
			throw new NotFoundException(String.format("Фильм с id = %d не найден", id));
		}

		Map<Long, Set<Long>> filmLikes = filmDataBase.getLikes(Collections.singleton(id));
		return new ArrayList<>(filmLikes.get(id));
	}

	@Override
	public Film getFilm(Long id) {
		Optional<Film> film = filmDataBase.getFilm(id);
		if (film.isEmpty()) {
			throw new NotFoundException(String.format("Фильм с id = %d не найден", id));
		}

		return film.get();
	}


	private void validateCreateFilm(Film currentFilm) {
		if (currentFilm.getName() == null || currentFilm.getName().isBlank()) {
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
		if (newFilmData.getId() == null) {
			log.warn("Ошибка валидации: id фильма пустое");
			throw new ValidationException("Id фильма не должно быть пустым");
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
