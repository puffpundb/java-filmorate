package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
public class Film implements Comparable<Film> {
    private long id;

    private String name;

    private String description;

    private LocalDate releaseDate;

    private Integer duration;

    private Set<Long> usersLike = new HashSet<>();

    private Integer mpaRating;

    private Set<Integer> genres = new HashSet<>();

    @Override
    public int compareTo(Film o) {
        return Long.compare(o.getUsersLike().size(), this.usersLike.size());
    }
}
