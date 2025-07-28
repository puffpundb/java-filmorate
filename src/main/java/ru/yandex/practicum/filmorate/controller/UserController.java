package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/users")
public class UserController {
    private final Map<Integer, User> userDataBase = new HashMap<>();
    private Integer currentMaxId = 0;

    @GetMapping
    public ArrayList<User> getAllUsers() {
        return new ArrayList<>(userDataBase.values());
    }

    @PostMapping
    public User createUser(@RequestBody User newUser) {
        validateUser(newUser);

        if (newUser.getName() == null) {
            log.info("Новый пользователь с пустым именем. Вместо имени подставлен логин");
            newUser.setName(newUser.getLogin());
        }
        newUser.setId(generateId());
        userDataBase.put(newUser.getId(), newUser);

        log.info("Новый пользователь добавлен. Имя: {}, логин: {}, id: {}, email: {}, дата рождения: {}",
                newUser.getName(),
                newUser.getLogin(),
                newUser.getId(),
                newUser.getEmail(),
                newUser.getBirthday()
        );
        return newUser;
    }

    @PutMapping
    public User updateuser(@RequestBody User newOldUser) {
        if (!userDataBase.containsKey(newOldUser.getId())) {
            log.warn("Попытка изменить пользователя с несуществующим id: {}", newOldUser.getId());
            throw new ValidationException("Пользователь с таким id не найден");
        }

        User currentUser = userDataBase.get(newOldUser.getId());

        if (newOldUser.getEmail() != null) {
            log.info("Изменение email-а пользователя. Старый: {}, новый: {}", currentUser.getEmail(), newOldUser.getEmail());
            currentUser.setEmail(newOldUser.getEmail());
        }
        if (newOldUser.getLogin() != null) {
            log.info("Изменение логина пользователя. Старый: {}, новый: {}", currentUser.getLogin(), newOldUser.getLogin());
            currentUser.setLogin(newOldUser.getLogin());
        }
        if (newOldUser.getName() != null) {
            log.info("Изменение имени пользователя. Старое: {}, новое: {}", currentUser.getName(), newOldUser.getName());
            currentUser.setName(newOldUser.getName());
        }
        if (newOldUser.getBirthday() != null) {
            log.info("Изменение даты рождения пользователя. Старая: {}, новая: {}", currentUser.getBirthday(), newOldUser.getBirthday());
            currentUser.setBirthday(newOldUser.getBirthday());
        }

        log.info("Пользователь обновлён");
        return currentUser;
    }

    private void validateUser(User currentUser) {
        if (currentUser.getEmail() == null) {
            log.warn("Ошибка валидации: Email null");
            throw new ValidationException("Email не должен быть пустым");
        }
        if (currentUser.getLogin() == null) {
            log.warn("Ошибка валидации: Логин null");
            throw new ValidationException("Логин не должен быть пустым");
        }
        if (currentUser.getBirthday() == null) {
            log.warn("Ошибка валидации: Дата рождения null");
            throw new ValidationException("Дата рождения не должна быть пустой");
        }

        if (currentUser.getEmail().isBlank() || !currentUser.getEmail().contains("@")) {
            log.warn("Ошибка валидации: Не корректный email");
            throw new ValidationException("Email не должен быть пустым и должен указывать на сервис электронной почты");
        }
        if (currentUser.getLogin().isBlank() || currentUser.getLogin().contains(" ")) {
            log.warn("Ошибка валидации: Логин пустой или содержит пробельные символы");
            throw new ValidationException("Логин не должен быть пустым или содержать пробелы");
        }
        if (currentUser.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Ошибка валидации: Дата рождения указана в будущем");
            throw new ValidationException("Дата рождения не может быть в будущем");
        }

    }

    private Integer generateId() {
        return ++currentMaxId;
    }
}
