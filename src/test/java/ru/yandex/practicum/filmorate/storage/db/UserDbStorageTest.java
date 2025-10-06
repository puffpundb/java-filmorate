package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class})
@Sql(scripts = "/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class UserDbStorageTest {
	private final UserDbStorage userStorage;

	private User createBaseUser() {
		User user = new User();
		user.setEmail("test@user.com");
		user.setLogin("testuser");
		user.setName("Test Name");
		user.setBirthday(LocalDate.of(1990, 1, 1));
		return user;
	}

	@Test
	public void shouldSaveAndGetUserToDb() {
		User user = createBaseUser();
		User created = userStorage.createUser(user);
		User fromDb = userStorage.getUser(created.getId()).get();

		assertThat(fromDb.getId()).isEqualTo(created.getId());
		assertThat(fromDb.getEmail()).isEqualTo(user.getEmail());
		assertThat(fromDb.getLogin()).isEqualTo(user.getLogin());
		assertThat(fromDb.getName()).isEqualTo(user.getName());
		assertThat(fromDb.getBirthday()).isEqualTo(user.getBirthday());
	}

	@Test
	public void shouldSetNameAsLoginWhenNameIsNull() {
		User user = createBaseUser();
		user.setName(null);
		User created = userStorage.createUser(user);

		assertThat(created.getName()).isEqualTo("testuser");
	}

	@Test
	public void shouldRightUpdateUserToDb() {
		User user = userStorage.createUser(createBaseUser());

		User updateData = new User();
		updateData.setId(user.getId());
		updateData.setEmail("updated@user.com");
		updateData.setLogin("newlogin");
		updateData.setName("New Name");
		updateData.setBirthday(LocalDate.of(1985, 5, 5));

		userStorage.updateUser(updateData);

		User updated = userStorage.getUser(user.getId()).get();

		assertThat(updated.getEmail()).isEqualTo("updated@user.com");
		assertThat(updated.getLogin()).isEqualTo("newlogin");
		assertThat(updated.getName()).isEqualTo("New Name");
		assertThat(updated.getBirthday()).isEqualTo(LocalDate.of(1985, 5, 5));
	}

	@Test
	void shouldAddFriend() {
		User user1 = userStorage.createUser(createBaseUser());
		User user2 = userStorage.createUser(createBaseUser());

		userStorage.addFriend(user1.getId(), user2.getId());

		List<User> friends = userStorage.getUserFriends(user1.getId());
		//assertThat(friends).contains(user2);
		assertThat(friends)
				.extracting(User::getId)
				.contains(user2.getId());
	}

	@Test
	void shouldRemoveFriend() {
		User user1 = userStorage.createUser(createBaseUser());
		User user2 = userStorage.createUser(createBaseUser());

		userStorage.addFriend(user1.getId(), user2.getId());
		userStorage.removeFriend(user1.getId(), user2.getId());

		List<User> friends = userStorage.getUserFriends(user1.getId());
		assertThat(friends).doesNotContain(user2);
	}

	@Test
	void shouldReturnAllFriendsList() {
		User user1 = userStorage.createUser(createBaseUser());
		User user2 = userStorage.createUser(createBaseUser());

		userStorage.addFriend(user1.getId(), user2.getId());

		List<User> friends = userStorage.getAllFriends(user1.getId());

		assertThat(friends).hasSize(1);
		assertThat(friends.get(0).getId()).isEqualTo(user2.getId());
	}


}
