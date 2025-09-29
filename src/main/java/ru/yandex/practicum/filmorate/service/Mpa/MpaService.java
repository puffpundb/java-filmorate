package ru.yandex.practicum.filmorate.service.Mpa;

import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;

public interface MpaService {
	List<MpaRating> getAllMpaRating();

	MpaRating getMpaRatingById(int id);
}
