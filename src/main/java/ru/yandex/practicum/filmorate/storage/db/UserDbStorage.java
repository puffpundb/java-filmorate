package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Repository
@Qualifier("userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
	private final JdbcTemplate jdbcTemplate;
	private Integer currentMaxId = 0;

	@Override
	public List<User> getAllUsers() {
		String sql = "SELECT id, email, login, name, birthday FROM users";

		return jdbcTemplate.query(sql, new UserRowMapper());
	}

	@Override
	public User createUser(User newUser) {
		validateUserCreate(newUser);

		newUser.setId(generateId());

		String sql = "INSERT INTO users (id, email, login, name, birthday) VALUES (?, ?, ?, ?, ?)";
		jdbcTemplate.update(sql,
				newUser.getId(),
				newUser.getEmail(),
				newUser.getLogin(),
				newUser.getName(),
				newUser.getBirthday());

		return newUser;
	}

	@Override
	public User updateUser(User newUserData) {
		if (!isContain(newUserData.getId())) {
			throw new NotFoundException("Пользователь не найден");
		}

		validateUserUpdate(newUserData);
		User currentDbUser = getUser(newUserData.getId());

		if (newUserData.getEmail() != null) {
			currentDbUser.setEmail(newUserData.getEmail());
		}
		if (newUserData.getLogin() != null) {
			currentDbUser.setLogin(newUserData.getLogin());
		}
		if (newUserData.getName() != null) {
			currentDbUser.setName(newUserData.getName());
		}
		if (newUserData.getBirthday() != null) {
			currentDbUser.setBirthday(newUserData.getBirthday());
		}

		String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
		jdbcTemplate.update(sql,
				currentDbUser.getEmail(),
				currentDbUser.getLogin(),
				currentDbUser.getName(),
				currentDbUser.getBirthday(),
				currentDbUser.getId());

		return currentDbUser;
	}

	@Override
	public Set<Long> getUserFriends(Long id) {
		if (isContain(id)) {
			String sql = "SELECT user_id2 FROM friendship_status WHERE user_id1 = ?";

			return new HashSet<>(jdbcTemplate.queryForList(sql, Long.class, id));
		}

		throw new NotFoundException("Пользователь не найден");
	}

	@Override
	public boolean isContain(Long id) {
		String sql = "SELECT COUNT(*) FROM users WHERE id = ?";

		return jdbcTemplate.queryForObject(sql, Integer.class, id) > 0;
	}

	@Override
	public User getUser(Long id) {
		String sql = "SELECT id, email, login, name, birthday FROM users WHERE id = ?";
		List<User> user = jdbcTemplate.query(sql, new UserRowMapper(), id);

		if (user.isEmpty()) {
			throw new NotFoundException("Пользователь не найден");
		}

		User currentUser = user.getFirst();
		currentUser.setFriendsList(getUserFriends(id));

		return currentUser;
	}

	@Override
	public void addFriend(Long id, Long friendId) {
		if (isContain(id) && isContain(friendId)) {
			String sql = "INSERT INTO friendship_status (user_id1, user_id2) VALUES (?, ?)";
			jdbcTemplate.update(sql, id, friendId);

		} else {
			throw new NotFoundException("Пользователь не найден");
		}
	}

	@Override
	public void removeFriend(Long id, Long friendId) {
		if (isContain(id) && isContain(friendId)) {
			String sql = "DELETE FROM friendship_status WHERE user_id1 = ? AND user_id2 = ?";
			jdbcTemplate.update(sql, id, friendId);

		} else {
			throw new NotFoundException("Пользователь не найден");
		}
	}

	@Override
	public List<User> getAllFriends(Long id) {
		if (isContain(id)) {
			String sql = """
					SELECT u.id, u.email, u.login, u.name, u.birthday
					FROM users u
					JOIN friendship_status fs ON u.id = fs.user_id2
					WHERE fs.user_id1 = ?
					""";

			return jdbcTemplate.query(sql, new UserRowMapper(), id);
		}

		throw new NotFoundException("Пользователь не найден");
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
		if (!isContain(newUserData.getId())) {
			throw new NotFoundException("Пользователь не найден");
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
