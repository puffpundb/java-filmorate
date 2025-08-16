package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

public interface UserStorage {
	ArrayList<User> getAllUsers();

	User createUser(User newUser);

	User updateUser(User newUserData);

	Set<Integer> getUserFriends(Integer id);

	boolean isContain(Integer id);

	User getUser(Integer id);
}
