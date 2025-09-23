package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
public class User {
    private long id;

    private String email;

    private String login;

    private String name;

    private LocalDate birthday;

    private Set<Long> friendsList = new HashSet<>();

}
