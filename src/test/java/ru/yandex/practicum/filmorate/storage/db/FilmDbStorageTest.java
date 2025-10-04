package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, UserDbStorage.class, GenreDbStorage.class, MpaRatingDbStorage.class})
@Sql(scripts = "/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class FilmDbStorageTest {
	private final FilmStorage filmStorage;
	private final UserStorage userStorage;

	private Film createTestFilm() {
		Film film = new Film();
		film.setName("Test Film");
		film.setDescription("Description");
		film.setReleaseDate(LocalDate.of(2000, 1, 1));
		film.setDuration(120);

		MpaRating mpa = new MpaRating();
		mpa.setId(1);
		film.setMpa(mpa);

		Set<Genre> genres = new HashSet<>();

		Genre comedy = new Genre();
		comedy.setId(1);
		comedy.setName("Комедия");

		Genre drama = new Genre();
		drama.setId(2);
		drama.setName("Драма");

		genres.add(comedy);
		genres.add(drama);
		film.setGenres(genres);

		return film;
	}

	@Test
	public void shouldCreateAndReturnFilm() {
		Film film = createTestFilm();
		Film createdFilm = filmStorage.createFilm(film).get();
		Film fromDb = filmStorage.getFilm(createdFilm.getId()).get();

		assertThat(fromDb.getId()).isEqualTo(createdFilm.getId());
	}

	@Test
	public void shouldReturnRightFilm() {
		User user = new User();
		user.setEmail("test@user.com");
		user.setLogin("testuser");
		user.setBirthday(LocalDate.of(1990, 1, 1));
		userStorage.createUser(user);

		Film film = createTestFilm();
		Film createdFilm = filmStorage.createFilm(film).get();
		filmStorage.addLike(createdFilm.getId(), userStorage.getUser(user.getId()).get().getId());

		Film fromDb = filmStorage.getFilm(createdFilm.getId()).get();

		assertThat(fromDb.getName()).isEqualTo(createdFilm.getName());
		assertThat(fromDb.getDescription()).isEqualTo(createdFilm.getDescription());
		assertThat(fromDb.getDuration()).isEqualTo(createdFilm.getDuration());
		assertThat(fromDb.getReleaseDate()).isEqualTo(createdFilm.getReleaseDate());
		assertThat(fromDb.getGenres()).isEqualTo(createdFilm.getGenres());
		assertThat(fromDb.getMpa().getId()).isEqualTo(createdFilm.getMpa().getId());
		assertThat(fromDb.getUsersLike().contains(userStorage.getUser(user.getId()).get().getId()));
	}

	@Test
	public void shouldReturnAllFilmsList() {
		filmStorage.createFilm(createTestFilm());
		filmStorage.createFilm(createTestFilm());

		List<Film> allFilms = filmStorage.getAllFilms();

		assertThat(allFilms).hasSize(2);
	}

	@Test
	public void shouldRightUpdateFilm() {
		Film film = filmStorage.createFilm(createTestFilm()).get();

		Film updateData = new Film();
		updateData.setId(film.getId());
		updateData.setName("Updated Name");
		updateData.setMpa(new MpaRating(4, "R"));
		Set<Genre> newGenre = new HashSet<>();
		newGenre.add(new Genre(6, "Боевик"));
		updateData.setGenres(newGenre);
		updateData.setDescription("zxc");
		updateData.setDuration(1);
		updateData.setReleaseDate(LocalDate.of(2000, 1, 2));

		filmStorage.updateFilm(updateData);
		Film updated = filmStorage.getFilm(film.getId()).get();

		assertThat(updated.getName()).isEqualTo("Updated Name");
		assertThat(updated.getDescription()).isEqualTo("zxc");
		assertThat(updated.getReleaseDate()).isEqualTo(LocalDate.of(2000, 1, 2));
		assertThat(updated.getDuration()).isEqualTo(1);
		assertThat(updated.getMpa().getId()).isEqualTo(4);
		assertThat(updated.getGenres().size()).isEqualTo(1);
	}
}
