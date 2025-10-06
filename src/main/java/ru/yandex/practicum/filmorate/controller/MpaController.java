package ru.yandex.practicum.filmorate.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.service.Mpa.MpaService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/mpa")
public class MpaController {
	private final MpaService mpaService;

	@GetMapping
	public List<MpaRating> getAllMpa() {
		return mpaService.getAllMpaRating();
	}

	@GetMapping("/{id}")
	public MpaRating getMpa(@PathVariable int id) {
		System.out.println("1");
		return mpaService.getMpaRatingById(id);
	}
}
