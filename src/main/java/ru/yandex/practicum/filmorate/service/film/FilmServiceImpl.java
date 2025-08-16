package ru.yandex.practicum.filmorate.service.film;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.List;

@Service
@AllArgsConstructor
public class FilmServiceImpl implements FilmService {
	FilmStorage filmDataBase;

	@Override
	public void liked(Integer filmId, Integer userId) {
		if (filmDataBase.isContain(filmId)) {
			filmDataBase.getFilm(filmId).getUsersLike().add(userId);
			return;
		}

		throw new NotFoundException("Фильм не найден");
	}

	@Override
	public void disliked(Integer filmId, Integer userId) {
		if (filmDataBase.isContain(filmId)) {
			filmDataBase.getFilm(filmId).getUsersLike().remove(userId);
			return;
		}

		throw new NotFoundException("Фильм не найден");
	}

	@Override
	public List<Film> getMostRatedFilms(int count) {
		return filmDataBase.getAllFilm().stream()
				.sorted()
				.limit(count)
				.toList();
	}
}
