package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MpaRating {
	private Integer id;
	private String rating;

	public MpaRating(Integer id, String rating) {
		this.id = id;
		this.rating = rating;
	}
}
