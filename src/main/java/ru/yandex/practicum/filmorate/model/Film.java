package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.yandex.practicum.filmorate.interfaceAndAnnotation.Marker;
import ru.yandex.practicum.filmorate.interfaceAndAnnotation.ReleaseDateCheck;

import java.time.LocalDate;

@Data
public class Film {
    private int id;

    private String name;

    private String description;

    private LocalDate releaseDate;

    private Integer duration;
}
