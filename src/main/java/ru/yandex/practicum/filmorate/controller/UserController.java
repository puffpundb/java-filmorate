package ru.yandex.practicum.filmorate.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.user.UserService;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@RequestBody User newUser) {
        return userService.createUser(newUser);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public User updateUser(@RequestBody User newUserData) {
        return userService.updateUser(newUserData);
    }

    @GetMapping("/{id}/friends")
    @ResponseStatus(HttpStatus.OK)
    public List<User> getUserFriends(@PathVariable Long id) {
        return userService.getUserFriends(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.OK)
    public List<User> addToFriend(@PathVariable Long id, @PathVariable Long friendId) {
        return userService.addToFriendsList(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    @ResponseStatus(HttpStatus.OK)
    public List<User> deleteFromFriend(@PathVariable Long id, @PathVariable Long friendId) {
        return userService.deleteFromFriendsList(id, friendId);
    }

    @GetMapping("/{id}/friends/common/{friendId}")
    @ResponseStatus(HttpStatus.OK)
    public List<User> findCommonFriends(@PathVariable Long id, @PathVariable Long friendId) {
        return userService.getCommonFriends(id, friendId);
    }
}
