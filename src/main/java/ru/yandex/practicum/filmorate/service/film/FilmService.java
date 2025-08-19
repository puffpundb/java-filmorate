package ru.yandex.practicum.filmorate.service.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmService {
	void liked(Integer filmId, Integer userId);

	void disliked(Integer filmId, Integer userId);

	List<Film> getMostRatedFilms(int count);

	List<Film> getAllFilm();

	Film createFilm(Film newFilm);

	Film updateFilm(Film newFilmData);
}
