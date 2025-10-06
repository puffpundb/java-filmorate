package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

public interface UserStorage {
	List<User> getAllUsers();

	User createUser(User newUser);

	Optional<User> updateUser(User newUserData);

	List<User> getUserFriends(Long id);

	Optional<User> getUser(Long id);

	void addFriend(Long id, Long friendId);

	int removeFriend(Long id, Long friendId);

	List<User> getAllFriends(Long id);

	List<User> getCommonFriends(Long userId, Long friendId);

	boolean userExist(Long userId);
}
