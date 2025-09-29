package ru.yandex.practicum.filmorate.service.Mpa;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.db.MpaRatingDbStorage;

import java.util.List;

@Service
@AllArgsConstructor
public class MpaServiceImpl implements MpaService {
	private final MpaRatingDbStorage mpaRatingDbStorage;

	@Override
	public List<MpaRating> getAllMpaRating() {
		return mpaRatingDbStorage.getAllMpaRating();
	}

	@Override
	public MpaRating getMpaRatingById(int id) {
		if (mpaRatingDbStorage.mpaExist(id)) {
			return mpaRatingDbStorage.getMpaRatingById(id);
		}

		throw new NotFoundException("Жанр не найден");
	}
}
