package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
public class InMemoryUserStorage implements  UserStorage {
	private final Map<Long, User> userDataBase = new HashMap<>();

	private Integer currentMaxId = 0;

	@Override
	public List<User> getAllUsers() {
		return new ArrayList<>(userDataBase.values());
	}

	@Override
	public User createUser(User newUser) {
		validateUserCreate(newUser);

		if (newUser.getName() == null) {
			log.info("Новый пользователь с пустым именем. Вместо имени подставлен логин");
			newUser.setName(newUser.getLogin());
		}
		newUser.setId(generateId());
		userDataBase.put(newUser.getId(), newUser);

		log.info("Новый пользователь добавлен. Имя: {}, логин: {}, id: {}, email: {}, дата рождения: {}",
				newUser.getName(),
				newUser.getLogin(),
				newUser.getId(),
				newUser.getEmail(),
				newUser.getBirthday()
		);
		return newUser;
	}

	@Override
	public User updateUser(User newUserData) {
		validateUserUpdate(newUserData);

		User currentUser = userDataBase.get(newUserData.getId());

		if (newUserData.getEmail() != null) {
			log.info("Изменение email-а пользователя. Старый: {}, новый: {}", currentUser.getEmail(), newUserData.getEmail());
			currentUser.setEmail(newUserData.getEmail());
		}
		if (newUserData.getLogin() != null) {
			log.info("Изменение логина пользователя. Старый: {}, новый: {}", currentUser.getLogin(), newUserData.getLogin());
			currentUser.setLogin(newUserData.getLogin());
		}
		if (newUserData.getName() != null) {
			log.info("Изменение имени пользователя. Старое: {}, новое: {}", currentUser.getName(), newUserData.getName());
			currentUser.setName(newUserData.getName());
		}
		if (newUserData.getBirthday() != null) {
			log.info("Изменение даты рождения пользователя. Старая: {}, новая: {}", currentUser.getBirthday(), newUserData.getBirthday());
			currentUser.setBirthday(newUserData.getBirthday());
		}

		log.info("Пользователь обновлён");
		return currentUser;
	}

	@Override
	public Set<Long> getUserFriends(Long id) {
		return userDataBase.get(id).getFriendsList();
	}


	@Override
	public User getUser(Long id) {
		if (userDataBase.containsKey(id)) {
			return userDataBase.get(id);
		}

		throw new NotFoundException("Пользователь не найден");
	}

	@Override
	public void addFriend(Long id, Long friendId) {

	}

	@Override
	public void removeFriend(Long id, Long friendId) {

	}

	@Override
	public List<User> getAllFriends(Long id) {
		return List.of();
	}

	@Override
	public boolean userExist(Long id) {
		return userDataBase.containsKey(id);
	}

	private void validateUserCreate(User currentUser) {
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
		if (!userDataBase.containsKey(newUserData.getId())) {
			log.warn("Попытка изменить пользователя с несуществующим id: {}", newUserData.getId());
			throw new NotFoundException("Пользователь с таким id не найден");
		}
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

	private Integer generateId() {
		return ++currentMaxId;
	}
}
