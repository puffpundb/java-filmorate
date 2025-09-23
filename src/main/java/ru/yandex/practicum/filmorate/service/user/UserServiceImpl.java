package ru.yandex.practicum.filmorate.service.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Service
public class UserServiceImpl implements UserService {
	@Qualifier("userDbStorage")
	UserStorage userDataBase;

	@Autowired
	public UserServiceImpl(@Qualifier("userDbStorage") UserStorage userDataBase) {
		this.userDataBase = userDataBase;
	}

	@Override
	public List<User> getUserFriends(Long userId) {
		List<Long> userFriendsId = new ArrayList<>(userDataBase.getUserFriends(userId));

		return userFriendsId.stream()
				.map(userDataBase::getUser)
				.filter(Objects::nonNull)
				.toList();
	}

	@Override
	public List<User> addToFriendsList(Long userId, Long friendId) {
		if (userDataBase.isContain(userId) && userDataBase.isContain(friendId)) {
			userDataBase.addFriend(userId, friendId);

			return userDataBase.getAllFriends(userId);
		}

		throw new NotFoundException("Пользователь не найден");
	}

	@Override
	public List<User> deleteFromFriendsList(Long userId, Long friendId) {
		if (userDataBase.isContain(userId) && userDataBase.isContain(friendId)) {
			userDataBase.removeFriend(userId, friendId);

			return userDataBase.getAllFriends(userId);
		}

		throw new NotFoundException("Пользователь не найден");
	}

	@Override
	public List<User> getCommonFriends(Long userId, Long friendId) {
		if (userDataBase.isContain(userId) && userDataBase.isContain(friendId)) {
			User currentUser = userDataBase.getUser(userId);
			User currentFriend = userDataBase.getUser(friendId);

			List<Long> mutualSet = currentUser.getFriendsList().stream()
					.filter(currentFriend.getFriendsList()::contains)
					.toList();

			return mutualSet.stream()
					.map(userDataBase::getUser)
					.toList();
		}

		throw new NotFoundException("Пользователь не найден");
	}

	@Override
	public List<User> getAllUsers() {
		return userDataBase.getAllUsers();
	}

	@Override
	public User createUser(User newUser) {
		return userDataBase.createUser(newUser);
	}

	@Override
	public User updateUser(User newUserData) {
		return userDataBase.updateUser(newUserData);
	}
}
