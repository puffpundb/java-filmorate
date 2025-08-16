package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film implements Comparable<Film> {
    private int id;

    private String name;

    private String description;

    private LocalDate releaseDate;

    private Integer duration;

    private Set<Integer> usersLike = new HashSet<>();

    @Override
    public int compareTo(Film o) {
        return Integer.compare(o.getUsersLike().size(), this.usersLike.size());
    }
}
