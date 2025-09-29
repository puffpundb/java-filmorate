package ru.yandex.practicum.filmorate.service.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Set;

public interface FilmService {
	void liked(Long filmId, Long userId);

	void disliked(Long filmId, Long userId);

	List<Film> getMostRatedFilms(int count);

	List<Film> getAllFilm();

	Film createFilm(Film newFilm);

	Film updateFilm(Film newFilmData);

	Set<Long> getLikes(Long id);

	Film getFilm(Long id);
}
