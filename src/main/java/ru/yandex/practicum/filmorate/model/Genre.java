package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Genre {
	private int id;
	private String genre;

	public Genre(int id, String genre) {
		this.id = id;
		this.genre = genre;
	}
}
