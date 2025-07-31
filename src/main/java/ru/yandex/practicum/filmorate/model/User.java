package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.yandex.practicum.filmorate.interfaceAndAnnotation.Marker;

import java.time.LocalDate;

@Data
public class User {
    private int id;

    private String email;

    private String login;

    private String name;

    private LocalDate birthday;
}
