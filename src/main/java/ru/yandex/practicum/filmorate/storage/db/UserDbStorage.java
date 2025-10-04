package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

@Repository
@Qualifier("userDbStorage")
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
	private final JdbcTemplate jdbcTemplate;

	@Override
	public List<User> getAllUsers() {
		String sql = "SELECT * FROM users";
		return jdbcTemplate.query(sql, new UserRowMapper());
	}

	@Override
	public User createUser(User newUser) {
		GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
		String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
			ps.setString(1, newUser.getEmail());
			ps.setString(2, newUser.getLogin());
			if (newUser.getName() == null) {
				ps.setString(3, newUser.getLogin());
				newUser.setName(newUser.getLogin());
			}
			else ps.setString(3, newUser.getName());
			ps.setDate(4, Date.valueOf(newUser.getBirthday()));
			return ps;
		}, keyHolder);

		newUser.setId(keyHolder.getKeyAs(Long.class));

		return newUser;
	}

	@Override
	public Optional<User> updateUser(User newUserData) {
		Optional<User> optionalUser = getUser(newUserData.getId());
		if (optionalUser.isEmpty()) {
			return optionalUser;
		}

		User currentDbUser = optionalUser.get();
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

		return Optional.of(currentDbUser);
	}

	@Override
	public List<User> getUserFriends(Long userId) {
		String sql = """
        SELECT
            u.id,
            u.email,
            u.login,
            u.name,
            u.birthday
        FROM friendship_status fs
        JOIN users u ON fs.user_id2 = u.id
        WHERE fs.user_id1 = ?
        """;

		return jdbcTemplate.query(sql, new UserRowMapper(), userId);
	}

	@Override
	public Optional<User> getUser(Long id) {
		String sql = "SELECT id, email, login, name, birthday FROM users WHERE id = ?";
		String friendsSql = "SELECT user_id2 FROM friendship_status WHERE user_id1 = ?";

		User user;
		try {
			user = jdbcTemplate.queryForObject(sql, new UserRowMapper(), id);
			Set<Long> friendsId = new HashSet<>(jdbcTemplate.queryForList(friendsSql, Long.class, id));

			user.setFriendsList(friendsId);
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}

		return Optional.of(user);
	}

	@Override
	public void addFriend(Long id, Long friendId) throws DataIntegrityViolationException {
		String sql = "INSERT INTO friendship_status (user_id1, user_id2) VALUES (?, ?)";
		jdbcTemplate.update(sql, id, friendId);
	}

	@Override
	public int removeFriend(Long id, Long friendId) {
		String sql = "DELETE FROM friendship_status WHERE user_id1 = ? AND user_id2 = ?";
		return jdbcTemplate.update(sql, id, friendId);
	}

	@Override
	public List<User> getAllFriends(Long id) {
		getUser(id);
		String sql = """
				SELECT u.id, u.email, u.login, u.name, u.birthday
				FROM users u
				JOIN friendship_status fs ON u.id = fs.user_id2
				WHERE fs.user_id1 = ?
				""";

		return jdbcTemplate.query(sql, new UserRowMapper(), id);
	}

	@Override
	public Optional<List<User>> getCommonFriends(Long userId, Long friendId) {
		String commonFriendsSql = """
        SELECT
            u.id,
            u.email,
            u.login,
            u.name,
            u.birthday
        FROM users u
        JOIN friendship_status fs1 ON u.id = fs1.user_id2
        JOIN friendship_status fs2 ON u.id = fs2.user_id2
        WHERE fs1.user_id1 = ? AND fs2.user_id1 = ?
        ORDER BY u.id
        """;

		try {
			return Optional.of(jdbcTemplate.query(commonFriendsSql, new UserRowMapper(), userId, friendId));
		} catch (EmptyResultDataAccessException e) {
			return Optional.empty();
		}
	}
}
