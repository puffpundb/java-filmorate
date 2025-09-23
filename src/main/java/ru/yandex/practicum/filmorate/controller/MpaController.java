package ru.yandex.practicum.filmorate.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.db.MpaRatingDbStorage;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/mpa")
public class MpaController {
	private final MpaRatingDbStorage mpaRatingDbStorage;

	@GetMapping
	public List<MpaRating> getAllMpa() {
		return mpaRatingDbStorage.getAllMpaRating();
	}

	@GetMapping("/{id}")
	public MpaRating getMpa(@PathVariable int id) {
		return mpaRatingDbStorage.getMpaRatingById(id);
	}
}
