package ru.yandex.practicum.filmorate.service.Genre;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.db.GenreDbStorage;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class GenreServiceImpl implements GenreService {
	private final GenreDbStorage genreDbStorage;

	@Override
	public List<Genre> getAllGenre() {
		return genreDbStorage.getAllGenre();
	}

	@Override
	public Genre getGenreById(int id) {
		Optional<Genre> genreOptional = genreDbStorage.getGenreById(id);
		if (genreOptional.isPresent()) {
			return genreOptional.get();
		}

		throw new NotFoundException(String.format("Жанр с id = %d не найден", id));
	}
}
