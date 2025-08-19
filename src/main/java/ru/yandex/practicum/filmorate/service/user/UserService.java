package ru.yandex.practicum.filmorate.service.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

public interface UserService {
	List<User> getUserFriends(Integer userId);

	List<User> addToFriendsList(Integer userId, Integer friendId);

	List<User> deleteFromFriendsList(Integer userId, Integer friendId);

	List<User> getCommonFriends(Integer userId, Integer friendId);

	List<User> getAllUsers();

	User createUser(User newUser);

	User updateUser(User newUserData);
}
