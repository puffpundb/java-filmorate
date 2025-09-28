package ru.yandex.practicum.filmorate.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
public class UserServiceImpl implements UserService {
	@Qualifier("userDbStorage")
	private UserStorage userDataBase;

	private final String NOT_FOUND = "Пользователь не найден";

	@Autowired
	public UserServiceImpl(@Qualifier("userDbStorage") UserStorage userDataBase) {
		this.userDataBase = userDataBase;
	}

	@Override
	public List<User> getUserFriends(Long userId) {
		if (userDataBase.userExist(userId)) {
			List<Long> userFriendsId = new ArrayList<>(userDataBase.getUserFriends(userId));

			return userFriendsId.stream()
					.map(userDataBase::getUser)
					.filter(Objects::nonNull)
					.toList();
		}

		throw new NotFoundException(NOT_FOUND);
	}

	@Override
	public List<User> addToFriendsList(Long userId, Long friendId) {
		if (userDataBase.userExist(userId) && userDataBase.userExist(friendId)) {
			userDataBase.addFriend(userId, friendId);
			return userDataBase.getAllFriends(userId);
		}

		throw new NotFoundException(NOT_FOUND);
	}

	@Override
	public List<User> deleteFromFriendsList(Long userId, Long friendId) {
		if (userDataBase.userExist(userId) && userDataBase.userExist(friendId)) {
			userDataBase.removeFriend(userId, friendId);
			return userDataBase.getAllFriends(userId);
		}

		throw new NotFoundException(NOT_FOUND);
	}

	@Override
	public List<User> getCommonFriends(Long userId, Long friendId) {
		userDataBase.userExist(userId);
		User currentUser = userDataBase.getUser(userId);

		userDataBase.userExist(friendId);
		User currentFriend = userDataBase.getUser(friendId);

		List<Long> mutualSet = currentUser.getFriendsList().stream()
				.filter(currentFriend.getFriendsList()::contains)
				.toList();

		return mutualSet.stream()
				.map(userDataBase::getUser)
					.toList();
	}

	@Override
	public List<User> getAllUsers() {
		return userDataBase.getAllUsers();
	}

	@Override
	public User createUser(User newUser) {
		validateUserCreate(newUser);
		return userDataBase.createUser(newUser);
	}

	@Override
	public User updateUser(User newUserData) {
		if (userDataBase.userExist(newUserData.getId())) {
			validateUserUpdate(newUserData);
			return userDataBase.updateUser(newUserData);
		}

		throw new NotFoundException(NOT_FOUND);
	}

	private void validateUserCreate(User currentUser) {
		if (currentUser.getName() == null) {
			log.info("Новый пользователь с пустым именем. Вместо имени подставлен логин");
			currentUser.setName(currentUser.getLogin());
		}
		if (currentUser.getEmail() == null || currentUser.getEmail().isBlank() || !currentUser.getEmail().contains("@")) {
			log.warn("Ошибка валидации: Не корректный email");
			throw new ValidationException("Email не должен быть пустым и должен указывать на сервис электронной почты");
		}
		if (currentUser.getLogin() == null || currentUser.getLogin().isBlank() || currentUser.getLogin().contains(" ")) {
			log.warn("Ошибка валидации: Логин пустой или содержит пробельные символы");
			throw new ValidationException("Логин не должен быть пустым или содержать пробелы");
		}
		if (currentUser.getBirthday() == null || currentUser.getBirthday().isAfter(LocalDate.now())) {
			log.warn("Ошибка валидации: Дата рождения указана в будущем");
			throw new ValidationException("Дата рождения не может быть в будущем");
		}
	}

	private void validateUserUpdate(User newUserData) {
		if (newUserData.getEmail() != null && (newUserData.getEmail().isBlank() || !newUserData.getEmail().contains("@"))) {
			log.warn("Ошибка валидации: Не корректный email");
			throw new ValidationException("Email не должен быть пустым и должен указывать на сервис электронной почты");
		}
		if (newUserData.getLogin() != null && (newUserData.getLogin().isBlank() || newUserData.getLogin().contains(" "))) {
			log.warn("Ошибка валидации: Логин пустой или содержит пробельные символы");
			throw new ValidationException("Логин не должен быть пустым или содержать пробелы");
		}
		if (newUserData.getBirthday() != null && newUserData.getBirthday().isAfter(LocalDate.now())) {
			log.warn("Ошибка валидации: Дата рождения указана в будущем");
			throw new ValidationException("Дата рождения не может быть в будущем");
		}
	}
}
