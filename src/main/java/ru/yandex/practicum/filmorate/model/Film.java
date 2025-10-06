package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.*;

@Data
@NoArgsConstructor
public class Film implements Comparable<Film> {
    private Long id;

    private String name;

    private String description;

    private LocalDate releaseDate;

    private Integer duration;

    private Set<Long> usersLike = new HashSet<>();

    private MpaRating mpa;

    private Set<Genre> genres = new HashSet<>();

    @Override
    public int compareTo(Film o) {
        return Long.compare(o.getUsersLike().size(), this.usersLike.size());
    }
}
