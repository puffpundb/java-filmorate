package ru.yandex.practicum.filmorate.model.mapper;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

public class FilmRowMapper implements RowMapper<Film> {
	@Override
	public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
		Film film = new Film();
		film.setId(rs.getLong("id"));
		film.setName(rs.getString("name"));
		film.setDescription(rs.getString("description"));
		film.setReleaseDate(rs.getDate("release_date").toLocalDate());
		film.setDuration(rs.getInt("duration"));
		film.setMpaRating(rs.getInt("mpa_rating_id"));
		film.setGenres(new HashSet<>());
		return film;
	}
}
