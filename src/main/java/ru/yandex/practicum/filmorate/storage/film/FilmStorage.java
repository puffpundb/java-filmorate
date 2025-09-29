package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Set;

public interface FilmStorage {
	List<Film> getAllFilms();

	Film createFilm(Film newFilm);

	Film updateFilm(Film newFilmData);

	boolean filmExist(Long id);

	Film getFilm(Long id);

	void addLike(Long filmId, Long userId);

	void removeLike(Long filmId, Long userId);

	Set<Long> getLikes(Long filmId);
}
