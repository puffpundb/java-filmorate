package ru.yandex.practicum.filmorate.service.film;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@AllArgsConstructor
public class FilmServiceImpl implements FilmService {
	private FilmStorage filmDataBase;
	private UserStorage userDataBase;

	@Override
	public void liked(Integer filmId, Integer userId) {
		userDataBase.getUser(userId);

		filmDataBase.getFilm(filmId).getUsersLike().add(userId);
	}

	@Override
	public void disliked(Integer filmId, Integer userId) {
		userDataBase.getUser(userId);

		filmDataBase.getFilm(filmId).getUsersLike().remove(userId);
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
