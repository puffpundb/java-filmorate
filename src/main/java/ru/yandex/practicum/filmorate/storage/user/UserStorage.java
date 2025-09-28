package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

public interface UserStorage {
	List<User> getAllUsers();

	User createUser(User newUser);

	User updateUser(User newUserData);

	Set<Long> getUserFriends(Long id);

	User getUser(Long id);

	void addFriend(Long id, Long friendId);

	void removeFriend(Long id, Long friendId);

	List<User> getAllFriends(Long id);

	boolean userExist(Long id);
}
