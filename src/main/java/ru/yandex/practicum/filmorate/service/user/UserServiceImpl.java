package ru.yandex.practicum.filmorate.service.user;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
	UserStorage userDataBase;

	@Override
	public List<User> getUserFriends(Integer userId) {
		if (userDataBase.isContain(userId)) {
			List<Integer> userFriendsId = new ArrayList<>(userDataBase.getUser(userId).getFriendsList());

			return userFriendsId.stream()
					.map(userDataBase::getUser)
					.filter(Objects::nonNull)
					.toList();
		}

		throw new NotFoundException("Пользователь не найден");
	}

	@Override
	public List<User> addToFriendsList(Integer userId, Integer friendId) {
		if (userDataBase.isContain(userId) && userDataBase.isContain(friendId)) {
			User currentUser = userDataBase.getUser(userId);
			User currentFriend = userDataBase.getUser(friendId);

			currentUser.getFriendsList().add(friendId);
			currentFriend.getFriendsList().add(userId);

			List<Integer> friendsId = new ArrayList<>(currentUser.getFriendsList());
			return friendsId.stream()
					.map(id -> userDataBase.getUser(id))
					.toList();
		}

		throw new NotFoundException("Пользователь не найден");
	}

	@Override
	public List<User> deleteFromFriendsList(Integer userId, Integer friendId) {
		if (userDataBase.isContain(userId) && userDataBase.isContain(friendId)) {
			User currentUser = userDataBase.getUser(userId);
			User currentFriend = userDataBase.getUser(friendId);

			currentUser.getFriendsList().remove(friendId);
			currentFriend.getFriendsList().remove(userId);

			List<Integer> friendsId = new ArrayList<>(currentUser.getFriendsList());
			return friendsId.stream()
					.map(id -> userDataBase.getUser(id))
					.filter(Objects::nonNull)
					.toList();
		}

		throw new NotFoundException("Пользователь не найден");
	}

	@Override
	public List<User> getCommonFriends(Integer userId, Integer friendId) {
		if (userDataBase.isContain(userId) && userDataBase.isContain(friendId)) {
			User currentUser = userDataBase.getUser(userId);
			User currentFriend = userDataBase.getUser(friendId);

			List<Integer> mutualSet = currentUser.getFriendsList().stream()
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
