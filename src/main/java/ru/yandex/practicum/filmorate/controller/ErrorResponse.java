package ru.yandex.practicum.filmorate.controller;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ErrorResponse {
	String error;
	String description;
}
