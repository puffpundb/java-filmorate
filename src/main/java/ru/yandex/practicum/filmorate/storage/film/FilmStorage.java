package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface FilmStorage {
	List<Film> getAllFilm();

	Film createFilm(Film film);

	Film updateFilm(Film film);

	boolean isContain(Integer id);

	Film getFilm(Integer id);
}
