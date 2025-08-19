package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.user.UserService;
import ru.yandex.practicum.filmorate.service.user.UserServiceImpl;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
    private UserController userController;

    @BeforeEach
    void beforeEach() {
        UserStorage userStore = new InMemoryUserStorage();
        UserService userService = new UserServiceImpl(userStore);
        userController = new UserController(userService);
    }

    @Test
    void shouldCreateUserWhenValid() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("testlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userController.createUser(user);

        assertEquals(user, created);
    }

    @Test
    void shouldSetNameAsLoginWhenNameIsNull() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setLogin("mylogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userController.createUser(user);

        assertEquals(user.getLogin(), created.getName());
    }

    @Test
    void shouldThrowExceptionWhenEmailBlank() {
        User user = new User();
        user.setEmail("");
        user.setLogin("validlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userController.createUser(user);
        });

        assertTrue(exception.getMessage().contains("Email не должен быть пустым и должен указывать на сервис электронной почты"));
    }

    @Test
    void shouldThrowExceptionWhenEmailDoesNotContainAt() {
        User user = new User();
        user.setEmail("invalid-email");
        user.setLogin("validlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userController.createUser(user);
        });

        assertTrue(exception.getMessage().contains("Email не должен быть пустым и должен указывать на сервис электронной почты"));
    }

    @Test
    void shouldThrowExceptionWhenLoginBlank() {
        User user = new User();
        user.setEmail("valid@example.com");
        user.setLogin("");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userController.createUser(user);
        });

        assertTrue(exception.getMessage().contains("Логин не должен быть пустым или содержать пробелы"));
    }

    @Test
    void shouldThrowExceptionWhenLoginContainsSpace() {
        User user = new User();
        user.setEmail("valid@example.com");
        user.setLogin("with space");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userController.createUser(user);
        });

        assertTrue(exception.getMessage().contains("Логин не должен быть пустым или содержать пробелы"));
    }

    @Test
    void shouldThrowExceptionWhenBirthdayInFuture() {
        User user = new User();
        user.setEmail("valid@example.com");
        user.setLogin("login");
        user.setBirthday(LocalDate.now().plusDays(1));

        ValidationException exception = assertThrows(ValidationException.class, () -> {
            userController.createUser(user);
        });

        assertTrue(exception.getMessage().contains("Дата рождения не может быть в будущем"));
    }

    @Test
    void shouldUpdateUserWhenValid() {
        User user = new User();
        user.setEmail("old@example.com");
        user.setLogin("oldlogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User created = userController.createUser(user);

        User update = new User();
        update.setId(created.getId());
        update.setLogin("oldlogin");
        update.setBirthday(LocalDate.of(1990, 1, 1));
        update.setEmail("updated@example.com");

        User result = userController.updateUser(update);

        assertEquals("updated@example.com", result.getEmail());
    }

    @Test
    void shouldThrowExceptionWhenUpdateNonExistingUser() {
        User user = new User();
        user.setId(999);
        user.setEmail("notexists@example.com");

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userController.updateUser(user);
        });

        assertTrue(exception.getMessage().contains("Пользователь с таким id не найден"));
    }

    @Test
    void shouldReturnAllUsers() {
        User user1 = new User();
        user1.setEmail("a@b.com");
        user1.setLogin("u1");
        user1.setBirthday(LocalDate.now());

        User user2 = new User();
        user2.setEmail("c@d.com");
        user2.setLogin("u2");
        user2.setBirthday(LocalDate.now());

        userController.createUser(user1);
        userController.createUser(user2);

        List<User> all = userController.getAllUsers();

        assertEquals(2, all.size());
    }
}