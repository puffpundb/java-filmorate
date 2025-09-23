package ru.yandex.practicum.filmorate.service.film;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
public class FilmServiceImpl implements FilmService {
	@Qualifier("filmDbStorage")
	private FilmStorage filmDataBase;
	@Qualifier("userDbStorage")
	private UserStorage userDataBase;

	@Autowired
	public FilmServiceImpl(@Qualifier("filmDbStorage") FilmStorage filmDataBase, @Qualifier("userDbStorage") UserStorage userDataBase) {
		this.filmDataBase = filmDataBase;
		this.userDataBase = userDataBase;
	}

	@Override
	public void liked(Long filmId, Long userId) {
		userDataBase.getUser(userId);

		filmDataBase.addLike(filmId, userId);
	}

	@Override
	public void disliked(Long filmId, Long userId) {
		userDataBase.getUser(userId);

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
		return filmDataBase.createFilm(newFilm);
	}

	@Override
	public Film updateFilm(Film newFilmData) {
		return filmDataBase.updateFilm(newFilmData);
	}
}
