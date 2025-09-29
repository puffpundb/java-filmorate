package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
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
		String sql = """
        SELECT
            u.id, u.email,
            u.login, u.name,
            u.birthday,
            fs.user_id2 AS friend_id
        FROM users u
        LEFT JOIN friendship_status fs ON fs.user_id1 = u.id
        """;

		UserRowMapper userRowMapper = new UserRowMapper();

		return jdbcTemplate.query(sql, (ResultSet rs) -> {
			Map<Long, User> userMap = new HashMap<>();

			while (rs.next()) {
				Long userId = rs.getLong("id");
				User user = userMap.get(userId);

				if (user == null) {
					user = userRowMapper.mapRow(rs, 0);
					userMap.put(userId, user);
				}

				Long friendId = rs.getObject("friend_id", Long.class);
				if (friendId != null) {
					user.getFriendsList().add(friendId);
				}
			}

			return new ArrayList<>(userMap.values());
		});
	}

	@Override
	public User createUser(User newUser) {
		GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
		String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
			ps.setString(1, newUser.getEmail());
			ps.setString(2, newUser.getLogin());
			if (newUser.getName() == null) ps.setString(3, newUser.getLogin());
			else ps.setString(3, newUser.getName());
			ps.setDate(4, Date.valueOf(newUser.getBirthday()));
			return ps;
		}, keyHolder);

		newUser.setId(keyHolder.getKeyAs(Long.class));

		return getUser(newUser.getId());
	}

	@Override
	public User updateUser(User newUserData) {
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
		String sql = "SELECT user_id2 FROM friendship_status WHERE user_id1 = ?";
		return new HashSet<>(jdbcTemplate.queryForList(sql, Long.class, id));
	}

	@Override
	public User getUser(Long id) {
		String sql = "SELECT id, email, login, name, birthday FROM users WHERE id = ?";
		List<User> user = jdbcTemplate.query(sql, new UserRowMapper(), id);

		User currentUser = user.getFirst();
		currentUser.setFriendsList(getUserFriends(id));

		return currentUser;
	}

	@Override
	public void addFriend(Long id, Long friendId) {
		String sql = "INSERT INTO friendship_status (user_id1, user_id2) VALUES (?, ?)";
		jdbcTemplate.update(sql, id, friendId);
	}

	@Override
	public void removeFriend(Long id, Long friendId) {
		String sql = "DELETE FROM friendship_status WHERE user_id1 = ? AND user_id2 = ?";
		jdbcTemplate.update(sql, id, friendId);
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
	public boolean userExist(Long id) {
		String sql = "SELECT EXISTS(SELECT 1 FROM users WHERE id = ?)";
		return jdbcTemplate.queryForObject(sql, Boolean.class, id);
	}
}
