package ru.yandex.practicum.filmorate.service.Genre;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

public interface GenreService {
	List<Genre> getAllGenre();

	Genre getGenreById(int id);
}
