package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/films")
public class FilmController {
    private Integer currentMaxId = 0;
    private final Map<Integer, Film> filmStore = new HashMap<>();

    @GetMapping
    public ArrayList<Film> getAllFilm() {
        return new ArrayList<>(filmStore.values());
    }

    @PostMapping
    public Film createFilm(@RequestBody Film newFilm) {
        validateFilm(newFilm);

        newFilm.setId(generateId());
        filmStore.put(newFilm.getId(), newFilm);

        log.info("Новый фильм добавлен. Название: {}, описание: {}, id: {}, релиз: {}, продолжительность: {}",
                newFilm.getName(),
                newFilm.getDescription(),
                newFilm.getId(),
                newFilm.getReleaseDate(),
                newFilm.getDuration()
        );
        return newFilm;
    }

    @PutMapping
    public Film updateFilm(@RequestBody Film newFilmData) {
        if (!filmStore.containsKey(newFilmData.getId())) {
            log.warn("Попытка изменить фильм с несуществующим id: {}", newFilmData.getId());
            throw new ValidationException("Фильм с таким id не найден");
        }
        validateFilm(newFilmData);

        Film currentFilm = filmStore.get(newFilmData.getId());

        if (newFilmData.getName() != null) {
            log.info("Изменение названия фильма. Старое: {}, новое: {}", currentFilm.getName(), newFilmData.getName());
            currentFilm.setName(newFilmData.getName());
        }
        if (newFilmData.getDescription() != null) {
            log.info("Изменение описания фильма. Старое: {}, новое: {}", currentFilm.getDescription(), newFilmData.getDescription());
            currentFilm.setDescription(newFilmData.getDescription());
        }
        if (newFilmData.getReleaseDate() != null) {
            log.info("Изменение даты релиза фильма. Старое: {}, новое: {}", currentFilm.getReleaseDate(), newFilmData.getReleaseDate());
            currentFilm.setReleaseDate(newFilmData.getReleaseDate());
        }
        if (newFilmData.getDuration() != null) {
            log.info("Изменение продолжительности фильма. Старое: {}, новое: {}", currentFilm.getDuration(), newFilmData.getDuration());
            currentFilm.setDuration(newFilmData.getDuration());
        }

        log.info("Фильм обновлён");
        return currentFilm;
    }

    private void validateFilm(Film currentFilm) {
        if (currentFilm.getName() == null || currentFilm.getName().isBlank()) {
            log.warn("Ошибка валидации: Название фильма пустое");
            throw new ValidationException("Имя не должно быть пустым");
        }
        if (currentFilm.getDescription().length() > 200) {
            log.warn("Ошибка валидации: Превышен лимит символов описания");
            throw new ValidationException("Максимальная длинна описания 200 символов");
        }
        if (currentFilm.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("Ошибка валидации: Слишком ранняя дата релиза");
            throw new ValidationException("Дата релиза не должна быть раньше 28.12.1895 г.");
        }
        if (currentFilm.getDuration() < 0) {
            log.warn("Ошибка валидации: Отрицательная продолжительность");
            throw new ValidationException("Продолжительность не может быть отрицательной");
        }
    }

    private Integer generateId() {
        return ++currentMaxId;
    }
}
