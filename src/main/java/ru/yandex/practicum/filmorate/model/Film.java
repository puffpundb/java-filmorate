package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.yandex.practicum.filmorate.interfaceAndAnnotation.Marker;
import ru.yandex.practicum.filmorate.interfaceAndAnnotation.ReleaseDateCheck;

import java.time.LocalDate;

@Data
public class Film {
    @Null(groups = Marker.OnCreate.class)
    @NotNull(groups = Marker.OnUpdate.class)
    private int id;

    @NotBlank(groups = Marker.OnCreate.class)
    private String name;

    @NotBlank(groups = Marker.OnCreate.class)
    @Size(max = 200, message = "Максимальная длинна описания 200 символов")
    private String description;

    @NotNull(groups = Marker.OnCreate.class)
    @ReleaseDateCheck
    private LocalDate releaseDate;

    @NotNull(groups = Marker.OnCreate.class)
    @Positive
    private Integer duration;
}
