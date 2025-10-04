package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.Genre.GenreService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/genres")
public class GenreController {
	private final GenreService genreService;

	@GetMapping
	public List<Genre> getAllGenre() {
		return genreService.getAllGenre();
	}

	@GetMapping("/{id}")
	public Genre getGenre(@PathVariable int id) {
		return genreService.getGenreById(id);
	}
}
