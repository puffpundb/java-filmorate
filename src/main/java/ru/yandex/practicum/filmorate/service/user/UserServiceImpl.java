package ru.yandex.practicum.filmorate.service.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
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

	@Autowired
	public UserServiceImpl(@Qualifier("userDbStorage") UserStorage userDataBase) {
		this.userDataBase = userDataBase;
	}

	@Override
	public List<User> getUserFriends(Long userId) {
		Optional<User> checkUser = userDataBase.getUser(userId);
		if (checkUser.isEmpty()) {
			throw new NotFoundException(String.format("Пользователь с id = %d не найден", userId));
		}

		return userDataBase.getUserFriends(userId);
	}

	@Override
	public List<User> addToFriendsList(Long userId, Long friendId) {
		try {
			userDataBase.addFriend(userId, friendId);
		} catch (DataIntegrityViolationException e) {
			throw new NotFoundException(String.format("Один из id не найден %d, %d", userId, friendId));
		}
		return userDataBase.getUserFriends(userId);
	}

	@Override
	public void deleteFromFriendsList(Long userId, Long friendId) {
		Optional<User> userOptional = userDataBase.getUser(userId);
		if (userOptional.isEmpty()) {
			throw new NotFoundException("");
		}
		Optional<User> friendOptional = userDataBase.getUser(friendId);
		if (friendOptional.isEmpty()) {
			throw new NotFoundException("");
		}

		userDataBase.removeFriend(userId, friendId);
	}

	@Override
	public List<User> getCommonFriends(Long userId, Long friendId) {
		Optional<List<User>> optionalUsers = userDataBase.getCommonFriends(userId, friendId);
		if (optionalUsers.isPresent()) {
			return optionalUsers.get();
		}

		throw new NotFoundException(String.format("Один из id не найден %d, %d", userId, friendId));
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
		validateUserUpdate(newUserData);

		Optional<User> optionalUser = userDataBase.updateUser(newUserData);
		if (optionalUser.isPresent()) {
			return optionalUser.get();
		}

		throw new NotFoundException(String.format("Пользователь с id = %d не найден", newUserData.getId()));
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
