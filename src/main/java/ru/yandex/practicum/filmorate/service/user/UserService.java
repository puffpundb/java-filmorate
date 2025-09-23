package ru.yandex.practicum.filmorate.service.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserService {
	List<User> getUserFriends(Long userId);

	List<User> addToFriendsList(Long userId, Long friendId);

	List<User> deleteFromFriendsList(Long userId, Long friendId);

	List<User> getCommonFriends(Long userId, Long friendId);

	List<User> getAllUsers();

	User createUser(User newUser);

	User updateUser(User newUserData);
}
