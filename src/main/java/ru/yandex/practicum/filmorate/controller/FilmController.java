package ru.yandex.practicum.filmorate.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.film.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/films")
public class FilmController {
    private final FilmStorage filmStore;
    private final FilmService filmService;
    private final UserStorage userStore;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Film> getAllFilm() {
        return filmStore.getAllFilm();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film createFilm(@RequestBody Film newFilm) {
        return filmStore.createFilm(newFilm);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public Film updateFilm(@RequestBody Film newFilmData) {
        return filmStore.updateFilm(newFilmData);
    }

    @PutMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void likedFilm(@PathVariable Integer id, @PathVariable Integer userId) {
        if (!userStore.isContain(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        filmService.liked(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void dislikedFilm(@PathVariable Integer id, @PathVariable Integer userId) {
        if (!userStore.isContain(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        filmService.disliked(id, userId);
    }

    @GetMapping("/popular")
    @ResponseStatus(HttpStatus.OK)
    public List<Film> getMostRatedFilms(@RequestParam(defaultValue = "10") int count) {
        return filmService.getMostRatedFilms(count);
    }
}
