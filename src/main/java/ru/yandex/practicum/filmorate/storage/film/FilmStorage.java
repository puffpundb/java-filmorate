package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface FilmStorage {
	List<Film> getAllFilms();

	Optional<Film> createFilm(Film newFilm);

	Optional<Film> updateFilm(Film newFilmData);

	Optional<Film> getFilm(Long id);

	void addLike(Long filmId, Long userId) throws org.springframework.dao.DataAccessException;

	void removeLike(Long filmId, Long userId);

	Map<Long, Set<Long>> getLikes(Set<Long> filmsId);

	List<Film> getPopularFilms(Integer count);
}
