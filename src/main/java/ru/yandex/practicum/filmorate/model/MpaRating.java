package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MpaRating {
	private Integer id;
	private String name;

	public MpaRating(Integer id, String name) {
		this.id = id;
		this.name = name;
	}
}
