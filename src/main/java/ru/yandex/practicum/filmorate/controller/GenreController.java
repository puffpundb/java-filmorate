package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.db.GenreDbStorage;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/genres")
public class GenreController {
	private final GenreDbStorage genreDbStorage;

	@GetMapping
	public List<Genre> getAllGenre() {
		return genreDbStorage.getAllGenre();
	}

	@GetMapping("/{id}")
	public Genre getGenre(@PathVariable int id) {
		return genreDbStorage.getGenreById(id);
	}
}
