package ru.yandex.practicum.filmorate.controller;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.film.FilmService;
import ru.yandex.practicum.filmorate.service.film.FilmServiceImpl;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {
    private FilmController filmController;

    @BeforeEach
    void beforeEach() {
        FilmStorage filmStore = new InMemoryFilmStorage();
        UserStorage userStore = new InMemoryUserStorage();
        FilmService filmService = new FilmServiceImpl(filmStore, userStore);
        filmController = new FilmController(filmService);
    }

    @Test
    void shouldCreateFilmWhenValid() {
        Film film = new Film();
        film.setName("Inception");
        film.setDescription("Dream within a dream");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148);

        Film created = filmController.createFilm(film);

        assertEquals(film, created);
    }

    @Test
    void shouldThrowExceptionWhenNameBlank() {
        Film film = new Film();
        film.setName("");
        film.setDescription("Dream within a dream");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(148);

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            filmController.createFilm(film);
        });
        System.out.println(exception.getMessage());
        assertTrue(exception.getMessage().contains("Название не должно быть пустым"));
    }

    @Test
    void shouldThrowExceptionWhenDescriptionTooLong() {
        Film film = new Film();
        film.setName("Invalid");
        film.setDescription("A".repeat(201));
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(148);

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            filmController.createFilm(film);
        });
        assertTrue(exception.getMessage().contains("Максимальная длинна описания 200 символов"));
    }

    @Test
    void shouldThrowExceptionWhenReleaseDateTooEarly() {
        Film film = new Film();
        film.setName("Old Film");
        film.setDescription("Dream within a dream");
        film.setReleaseDate(LocalDate.of(1800, 1, 1));
        film.setDuration(148);

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            filmController.createFilm(film);
        });

        assertTrue(exception.getMessage().contains("Дата релиза не должна быть раньше 28.12.1895 г."));
    }

    @Test
    void shouldThrowExceptionWhenDurationNegative() {
        Film film = new Film();
        film.setName("Short");
        film.setDescription("Dream within a dream");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(-10);

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            filmController.createFilm(film);
        });

        assertTrue(exception.getMessage().contains("Продолжительность не может быть отрицательной"));
    }

    @Test
    void shouldUpdateFilmWhenValid() {
        Film film = new Film();
        film.setName("Old Title");
        film.setDescription("Old desc");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(100);
        Film created = filmController.createFilm(film);

        Film update = new Film();
        update.setId(created.getId());
        update.setName("New Title");
        update.setDescription("Old desc");
        update.setReleaseDate(LocalDate.of(2020, 1, 1));
        update.setDuration(100);

        Film result = filmController.updateFilm(update);

        assertEquals("New Title", result.getName());
    }

    @Test
    void shouldThrowExceptionWhenUpdateNonExistingFilm() {
        Film film = new Film();
        film.setId(999);
        film.setName("Not exists");

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            filmController.updateFilm(film);
        });

        assertTrue(exception.getMessage().contains("Фильм с таким id не найден"));
    }

    @Test
    void shouldReturnAllFilms() {
        Film film1 = new Film();
        film1.setName("Film 1");
        film1.setDescription("Old desc");
        film1.setDuration(100);
        film1.setReleaseDate(LocalDate.now());

        Film film2 = new Film();
        film2.setName("Film 2");
        film2.setDescription("Old desc");
        film2.setDuration(100);
        film2.setReleaseDate(LocalDate.now());

        filmController.createFilm(film1);
        filmController.createFilm(film2);

        List<Film> all = filmController.getAllFilm();

        assertEquals(2, all.size());
    }
}