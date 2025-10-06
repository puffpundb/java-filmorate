package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
public class User {
    private Long id;

    private String email;

    private String login;

    private String name;

    private LocalDate birthday;

    private Set<Long> friendsList;

}
